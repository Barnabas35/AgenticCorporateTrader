
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from query_library.get_balance import q_get_balance
from stock_api_access import StockAPIAccess
import yfinance as yf
import time

def q_purchase_asset(request_json):

    # Making sure session token, usd_quantity, market, client_id, and ticker_symbol are not empty
    if "session_token" not in request_json or "usd_quantity" not in request_json or "market" not in request_json or "ticker" not in request_json or "client_id" not in request_json:
        return {"status": "No session token, usd quantity, market, or ticker symbol provided."}

    # Get user session id, usd quantity, market, and ticker symbol from request
    session_token = request_json["session_token"]
    usd_quantity = str(request_json["usd_quantity"])
    market = request_json["market"]
    ticker = request_json["ticker"]
    client_id = request_json["client_id"]

    # Parse session id, market, and ticker symbol to be safe for Firestore
    session_token = firestore_safe(session_token)
    market = firestore_safe(market)
    ticker = firestore_safe(ticker)
    client_id = firestore_safe(client_id)

    # Parse asset quantity to be safe for Firestore
    try:
        usd_quantity = float(usd_quantity)
    except ValueError:
        return {"status": "Invalid usd quantity."}

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Getting user balance
    balance = q_get_balance(request_json)["balance"]

    # Checking if user has enough balance
    if balance < usd_quantity:
        return {"status": "Insufficient balance."}

    # Getting asset unit price
    asset_price = 0

    if market == "stocks":

        # Get API stock client
        client = StockAPIAccess.get_client()

        snapshot = client.get_snapshot_ticker("stocks", ticker)

        asset_price = snapshot.day.close

    elif market == "crypto":

        crypto = ticker + "-USD"

        try:
            ticker_info = yf.Ticker(crypto)
        except (ValueError, KeyError):
            return {"status": "Ticker not supported by yfinance."}

        # Get ticker history
        asset_price = ticker_info.history(period="1d", interval="1m").tail(1)['Close'].iloc[0]
    else:
        return {"status": "Invalid market."}

    # Getting user ID
    user_id = result[0].id

    # Getting user type
    user_type = result[0].to_dict()["user_type"]

    # Validating client id
    if user_id == client_id:

        if user_type != "fa":
            return {"status": "Fund Manager cannot purchase assets for themselves."}

        log_id = user_id

    else:

        if user_type != "fm":
            return {"status": "Fund Administrator cannot purchase assets for clients."}

        # Finding client in database
        result = (db.collection("clients").document(client_id).get())

        if not result.exists:
            return {"status": "Invalid client id."}

        log_id = client_id

    # Adding transaction to transaction log
    db.collection("users").document(user_id).collection("transaction_log").add({"market": market,
                                                                                "transaction_type": "purchase",
                                                                                "asset_quantity": usd_quantity / asset_price,
                                                                                "asset_value": asset_price,
                                                                                "client_id": log_id,
                                                                                "usd_quantity": usd_quantity,
                                                                                "ticker_symbol": ticker,
                                                                                "unix_timestamp": int(time.time())})

    # Return status
    return {"status": "Success"}
