#get asset report
#-session token
#-market
#-ticker
#-client id
#+profit = (total usd value of asset quantity) - (total_usd_quantity_invested)
#+total_usd_quantity_invested = (sum of usd_quantity in purchase) - (sum of usd_quantity in sell)

from db_access import DBAccess
import yfinance as yf
from stock_api_access import StockAPIAccess
from query_library.get_asset import q_get_asset

def q_get_asset_report(request_json):
    if "session_token" not in request_json or "market" not in request_json or "ticker" not in request_json or "client_id" not in request_json:
        return {"status": "No session token, market, ticker symbol or client id provided."}

    session_token = request_json["session_token"]
    market = request_json["market"]
    ticker = request_json["ticker"]
    client_id = request_json["client_id"]

    db = DBAccess.get_db()

    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    if len(result) == 0:
        return {"status": "Invalid session id."}

    user_id = result[0].id

    # Check if client is fa
    if user_id != client_id:

        result = (db.collection("clients").document(client_id).get())

        if not result.exists:
            return {"status": "Client does not exist."}

    # Used in calculating
    total_usd_quantity_invested = 0

    # Getting user asset quantity
    asset_quantity = q_get_asset(request_json)["total_asset_quantity"]

    # Getting current asset value
    if market == "stocks":
        client = StockAPIAccess.get_client()
        snapshot = client.get_snapshot_ticker("stocks", ticker)
        asset_usd_unit_price_current = snapshot.day.close
    elif market == "crypto":
        asset_usd_unit_price_current = yf.Ticker(ticker).history(period="1d")["Close"].iloc[0]
    else:
        return {"status": "Invalid market."}

    # Getting all transaction logs for the user where client id matches where market matches and ticker_symbol is not USD and ticker symbol is unique
    transaction_log = (db.collection("users").document(user_id).collection("transaction_log").where(field_path="client_id", op_string="==", value=client_id).stream())

    for transaction in transaction_log:
        if transaction.to_dict()["market"] == market and transaction.to_dict()["ticker_symbol"] != "USD" and transaction.to_dict()["ticker_symbol"] == ticker:
            if transaction.to_dict()["transaction_type"] == "purchase":
                total_usd_quantity_invested += transaction.to_dict()["usd_quantity"]
            elif transaction.to_dict()["transaction_type"] == "sell":
                total_usd_quantity_invested -= transaction.to_dict()["usd_quantity"]

    # Calculation
    profit = (asset_quantity * asset_usd_unit_price_current) - total_usd_quantity_invested

    return {"profit": profit, "total_usd_invested": max(total_usd_quantity_invested, 0), "status": "Success"}
