
from db_access import DBAccess

def q_get_asset_transaction_log(request_json):

        # Making sure session token, market, client_id, and ticker symbol are not empty
        if "session_token" not in request_json or "market" not in request_json or "client_id" not in request_json or "ticker" not in request_json:
            return {"status": "No session token, market, client id, or ticker symbol provided."}

        # Get user session id, market, client id, and ticker symbol from request
        session_token = request_json["session_token"]
        market = request_json["market"]
        client_id = request_json["client_id"]
        ticker_symbol = request_json["ticker"]

        # Get database reference
        db = DBAccess.get_db()

        # Find user in database with matching session id
        result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

        # If user is not found then set status accordingly
        if len(result) == 0:
            return {"status": "Invalid session id."}

        # User id
        user_id = result[0].id

        # Checking client exists
        result = (db.collection("clients").document(client_id).get())

        if not result.exists:
            return {"status": "Client does not exist."}

        # Getting all transaction logs for the user where client id matches where market matches and ticker_symbol matches
        transaction_log = (db.collection("users").document(user_id).collection("transaction_log").where(field_path="client_id", op_string="==", value=client_id).stream())

        # Transactions belonging to a ticker symbol
        transaction_logs = []

        for transaction in transaction_log:
            if transaction.to_dict()["market"] == market and transaction.to_dict()["ticker_symbol"] == ticker_symbol:
                transaction_logs.append(transaction.to_dict())

        # ordering transaction logs by unix_timestamp
        transaction_logs = sorted(transaction_logs, key=lambda x: x["unix_timestamp"], reverse=True)

        # Return unique ticker symbols
        return {"transaction_logs": transaction_logs, "status": "Success"}