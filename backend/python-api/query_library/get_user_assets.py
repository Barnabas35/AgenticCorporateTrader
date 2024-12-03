
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe

def q_get_user_assets(request_json):

    # Making sure session token, market, client_id are not empty
    if "session_token" not in request_json or "market" not in request_json or "client_id" not in request_json:
        return {"status": "No session token, market, or client id provided."}

    # Get user session id, usd quantity, market, and ticker symbol from request
    session_token = request_json["session_token"]
    market = request_json["market"]
    client_id = request_json["client_id"]

    # Parse session id, market, and ticker symbol to be safe for Firestore
    session_token = firestore_safe(session_token)
    market = firestore_safe(market)
    client_id = firestore_safe(client_id)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # User id
    user_id = result[0].id

    # If user id == client id then skip checking client exists
    if user_id != client_id:
        # Checking client exists
        result = (db.collection("clients").document(client_id).get())

        if not result.exists:
            return {"status": "Client does not exist."}

    # Getting all transaction logs for the user where client id matches where market matches and ticker_symbol is not USD and ticker symbol is unique
    transaction_log = (db.collection("users").document(user_id).collection("transaction_log").where(field_path="client_id", op_string="==", value=client_id).stream())

    # Finding unique ticker symbols
    ticker_symbols = set()

    for transaction in transaction_log:
        if transaction.to_dict()["market"] == market and transaction.to_dict()["ticker_symbol"] != "USD":
            ticker_symbols.add(transaction.to_dict()["ticker_symbol"])

    # Return unique ticker symbols
    return {"ticker_symbols": list(ticker_symbols), "status": "Success"}
