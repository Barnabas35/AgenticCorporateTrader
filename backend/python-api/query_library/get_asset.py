
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from math import floor

def q_get_asset(request_json):

    # Making sure session token, market, client_id are not empty
    if "session_token" not in request_json or "market" not in request_json or "client_id" not in request_json or "ticker" not in request_json:
        return {"status": "No session token, market, ticker symbol or client id provided."}

    # Get user session id, usd quantity, market, and ticker symbol from request
    session_token = request_json["session_token"]
    market = request_json["market"]
    client_id = request_json["client_id"]
    ticker_symbol = request_json["ticker"]

    # Parse session id, market, and ticker symbol to be safe for Firestore
    session_token = firestore_safe(session_token)
    market = firestore_safe(market)
    client_id = firestore_safe(client_id)
    ticker_symbol = firestore_safe(ticker_symbol)

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

    # Transactions belonging to a ticker symbol
    asset_quantity = 0

    for transaction in transaction_log:
        if transaction.to_dict()["market"] == market and transaction.to_dict()["ticker_symbol"] != "USD" and transaction.to_dict()["ticker_symbol"] == ticker_symbol:
            # Check transaction type
            if transaction.to_dict()["transaction_type"] == "purchase":
                asset_quantity += transaction.to_dict()["asset_quantity"]
            elif transaction.to_dict()["transaction_type"] == "sell":
                asset_quantity -= transaction.to_dict()["asset_quantity"]

    # Return unique ticker symbols
    return {"total_asset_quantity": floor(asset_quantity * 10000)/10000, "status": "Success"}
