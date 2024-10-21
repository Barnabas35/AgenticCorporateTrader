
from db_access import DBAccess
from stock_api_access import StockAPIAccess
from function_library.security_string_parsing import firestore_safe


def api_text_search_market(request_json):

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

    # Get API stock client
    client = StockAPIAccess.get_client()

    # Search for tickers
    req_url = f"https://api.polygon.io/v3/reference/tickers?market=stocks&search={search_query}&active=true&limit={limit}"
    response = StockAPIAccess.request(req_url)
    results = response["results"]

    # Get ticker details
    ticker_details = []
    for r in results:
        if show_price:
            prev_close = client.get_previous_close_agg(r["ticker"])
            ticker_details.append({"symbol": r["ticker"],
                                   "company_name": r["name"],
                                   "currency": r["currency_name"],
                                   "price": prev_close[0].close})
        else:
            ticker_details.append({"symbol": r["ticker"],
                                   "company_name": r["name"]})

    return {"ticker_details": ticker_details, "status": "Success"}
