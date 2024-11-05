
from db_access import DBAccess
from stock_api_access import StockAPIAccess
from function_library.security_string_parsing import firestore_safe
import yfinance as yf


def api_text_search_crypto(request_json):

    # Check if search query and session token and limit and show price is in request
    if "search_query" not in request_json:
        return {"status": "No search query provided."}

    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    if "show_price" not in request_json:
        show_price = False
    else:
        show_price = request_json["show_price"]

        # Make sure show_price is a boolean
        if not isinstance(show_price, bool):
            return {"status": "Show price must be a boolean."}

    if "limit" not in request_json:
        limit = 5
    else:
        # Get limit from request arguments
        limit = str(request_json["limit"])

        # Make sure limit is an integer
        if not limit.isnumeric():
            return {"status": "Limit must be an integer."}

        try:
            limit = min(max(int(limit), 1), 50)
        except ValueError:
            return {"status": "Limit must be an integer."}

    # Get search query and session token from request
    search_query = request_json["search_query"]
    session_token = request_json["session_token"]

    # Make search query and session token safe for Firestore
    search_query = firestore_safe(search_query)
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Search for tickers
    req_url = f"https://api.polygon.io/v3/reference/tickers?market=crypto&search={search_query}&active=true&limit={limit}"
    response = StockAPIAccess.request(req_url)
    results = response["results"]

    # Get crypto symbols and crypto names out of the results
    crypto_symbols = {}
    for r in results:
        crypto_symbols[r["base_currency_symbol"]] = r["base_currency_name"]

    # Reformat crypto_symbols to comply with json
    crypto_symbols = [{"symbol": k, "name": v} for k, v in crypto_symbols.items()]

    # Construct response if show_price is False
    if not show_price:
        return {"crypto_details": crypto_symbols, "status": "Success"}
    else:
        # Get crypto prices
        crypto_prices = []
        for symbol in crypto_symbols:
            try:
                ticker = yf.Ticker(f"{symbol['symbol']}-USD")
                price = ticker.history(period="1d", interval="1m").tail(1)['Close'].iloc[0]
                crypto_prices.append({"symbol": symbol["symbol"], "name": symbol["name"], "price": price})
            except (IndexError, KeyError):
                continue

        return {"crypto_details": crypto_prices, "status": "Success"}
