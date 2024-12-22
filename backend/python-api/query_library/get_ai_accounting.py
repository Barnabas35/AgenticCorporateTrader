
from concurrent.futures import ThreadPoolExecutor
from db_access import DBAccess
from function_library.prompt_ai import AI
from function_library.security_string_parsing import firestore_safe
from query_library.get_subscription import q_get_subscription
from query_library.get_asset import q_get_asset
from query_library.get_asset_report import q_get_asset_report
from query_library.get_asset_transaction_log import q_get_asset_transaction_log
from polygon_request_library.get_ticker_info import api_get_ticker_info
from yfinance_requests.get_crypto_info import api_get_crypto_info

def q_get_ai_accounting(request_json):

    # Make sure user session token and ticker is not empty
    if "session_token" not in request_json or "ticker" not in request_json or "market" not in request_json or "client_id" not in request_json:
        return {"status": "No session token or ticker symbol or market provided."}

    # Get user session id and ticker from request
    session_token = request_json["session_token"]
    ticker = request_json["ticker"]
    market = request_json["market"]

    # Parse session id to be safe for Firestore
    session_token = firestore_safe(session_token)
    ticker = firestore_safe(ticker)
    market = firestore_safe(market)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Checking if user has a subscription
    subscription_active = q_get_subscription(request_json)["subscription_active"]

    if not subscription_active:
        return {"status": "No active subscription."}

    # Getting total assets of the user under the market and ticker
    total_assets = q_get_asset(request_json)["total_asset_quantity"]
    asset_report = q_get_asset_report(request_json)["profit"]
    transact_log = q_get_asset_transaction_log(request_json)["transaction_logs"]

    ticker_info = None

    if market == "stock":
        ticker_info = api_get_ticker_info(request_json)["ticker_info"]
    elif market == "crypto":
        ticker_info = api_get_crypto_info({"session_token": session_token, "crypto": ticker})["crypto_info"]

    # Defining data for accounting
    liquidity_data = f"Ticker Info: {ticker_info}\nTotal Assets: {total_assets}\nAsset Report: {asset_report}"
    profitability_data = f"Ticker Info: {ticker_info}\nTotal Assets: {total_assets}\nAsset Report: {asset_report}\nTransaction Log: {transact_log}"
    growth_data = f"Transaction Log: {transact_log}"

    # AI formatting enforcer text
    formatting = "Only show the final result. No calculations or explanations needed."

    # Warming up the AI
    AI.prompt_ai("Warm-up", "Invalid Model")

    # Prompting AI using threading
    with ThreadPoolExecutor(max_workers=3) as executor:
        future1 = executor.submit(AI.prompt_ai, f"Calculate the asset liquidity of the {market} {ticker} based on the following data:\n{liquidity_data}\n{formatting}", "openai", f"You are a {market} accountant AI assistant.")
        future2 = executor.submit(AI.prompt_ai, f"Calculate the asset profitability of the {market} {ticker} based on the following data:\n{profitability_data}\n{formatting}", "openai", f"You are a {market} accountant AI assistant.")
        future3 = executor.submit(AI.prompt_ai, f"Calculate the asset growth of the {market} {ticker} based on the following data:\n{growth_data}\n{formatting}\nShow in percentage.", "openai", f"You are a {market} accountant AI assistant.")

    asset_liquidity = future1.result()
    asset_profitability = future2.result()
    asset_growth = future3.result()

    # Creating the accounting report
    return {
        "asset_liquidity": asset_liquidity,
        "asset_profitability": asset_profitability,
        "asset_growth": asset_growth
    }
