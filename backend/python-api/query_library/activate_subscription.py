
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from query_library.get_balance import q_get_balance
import time


def q_activate_subscription(request_json):

    SUB_COST = 30

    # Make sure session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Get session token from request
    session_token = request_json["session_token"]

    # Make session token safe for Firestore
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get user's subscription
    try:
        subscription_start = result[0].to_dict()["subscription_start"]
        renew_subscription = result[0].to_dict()["renew_subscription"]
    except KeyError:
        subscription_start = ""
        renew_subscription = ""

    # Getting users balance
    user_balance = q_get_balance(request_json)["balance"]

    # Check for a subscription
    if subscription_start != "" and renew_subscription != "":
        # Subscription found
        # Check subscription is active
        subscription_end = subscription_start + 2592000
        current_time = int(time.time())

        if current_time < subscription_end:
            db.collection("users").document(result[0].id).update({"renew_subscription": True})
            return {"status": "success"}

    # Check if user has enough balance
    if user_balance < SUB_COST:
        return {"status": "Insufficient balance."}

    # Activate subscription
    db.collection("users").document(result[0].id).update({"subscription_start": int(time.time()), "renew_subscription": True})

    # Getting user ID
    user_id = result[0].id

    # Adding transaction to transaction log
    db.collection("users").document(user_id).collection("transaction_log").add({"market": "currency",
                                                                                "transaction_type": "sell",
                                                                                "asset_quantity": SUB_COST,
                                                                                "asset_value": 1,
                                                                                "client_id": user_id,
                                                                                "usd_quantity": SUB_COST,
                                                                                "ticker_symbol": "USD",
                                                                                "unix_timestamp": int(time.time())})

    return {"status": "success"}
