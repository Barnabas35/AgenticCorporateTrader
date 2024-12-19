
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from query_library.activate_subscription import q_activate_subscription
import time

def q_get_subscription(request_json):

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

    # Check if any data has been found
    if subscription_start == "" or renew_subscription == "":
        return {"status": "success", "subscription_start": 0, "subscription_end": 0, "subscription_active": False, "renew_subscription": False}

    # Calculate subscription end from start (end= start + 30 days UNIX time formats)
    subscription_end = subscription_start + 2592000

    # Get current time
    current_time = int(time.time())

    # Check if subscription is active
    subscription_active = False
    if current_time < subscription_end:
        subscription_active = True
    elif renew_subscription:
        if q_activate_subscription(request_json)["status"] == "success":
            subscription_active = True

    # Return subscription data
    return {"status": "success", "subscription_start": subscription_start, "subscription_end": subscription_end, "subscription_active": subscription_active, "renew_subscription": renew_subscription}
