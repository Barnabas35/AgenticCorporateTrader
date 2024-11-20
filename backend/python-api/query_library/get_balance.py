
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe

def q_get_balance(request_json):

    # Make sure user session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session id provided."}

    # Get user session id from request
    session_token = request_json["session_token"]

    # Parse session id to be safe for Firestore
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Getting all records from users(matching session token)>>transaction_log(market is currency)
    transaction_log = db.collection("users").document(result[0].id).collection("transaction_log").where(field_path="market", op_string="==", value="currency").stream()

    balance = 0
    # Looping through transaction_log and checking if transaction_type is purchase or sell
    for transaction in transaction_log:
        if transaction.to_dict()["transaction_type"] == "purchase":
            balance += transaction.to_dict()["asset_quantity"]

        elif transaction.to_dict()["transaction_type"] == "sell":
            balance -= transaction.to_dict()["asset_quantity"]

    # Return balance
    return {"balance": balance, "status": "Success"}