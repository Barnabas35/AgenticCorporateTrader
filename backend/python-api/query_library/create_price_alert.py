
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import yfinance as yf
from stock_api_access import StockAPIAccess

def q_create_price_alert(request_json):

    # Make sure user session token, market, ticker, and price are not empty
    if ("session_token" not in request_json
            or "market" not in request_json
            or "ticker" not in request_json
            or "price" not in request_json):
        return {"status": "No session token or market or ticker or price provided."}

    # Get user session token, market, ticker, and price from request
    session_token = request_json["session_token"]
    market = request_json["market"]
    ticker = request_json["ticker"]
    price = request_json["price"]

    # Parse session token, market, ticker to be safe for Firestore
    session_token = firestore_safe(session_token)
    market = firestore_safe(market)
    ticker = firestore_safe(ticker)

    # Validating price
    try:
        price = float(price)
    except ValueError:
        return {"status": "Price is not a float."}

    # Validate market type
    if market != "stocks" and market != "crypto":
        return {"status": "Invalid market type."}

    # Validate ticker
    try:
        if market == "crypto":
            ticker_usd = ticker + "-USD"
            ticker_yf = yf.Ticker(ticker_usd)
            ticker_info = ticker_yf.info
            if ticker_info["symbol"] != ticker_usd:
                return {"status": "Invalid crypto ticker."}
        elif market == "stocks":
            # Get polygon client
            client = StockAPIAccess.get_client()
            data = client.get_ticker_details(ticker)
            if data.ticker != ticker:
                return {"status": "Invalid stock ticker."}
    except:
        return {"status": "Invalid ticker."}

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session token."}

    # Get user ID
    user_id = result[0].id

    # Create new price alert
    db.collection("price_alerts").add({"user_id": user_id,
                                       "market": market,
                                       "ticker": ticker,
                                       "price": price})

    # Return status
    return {"status": "Success"}
