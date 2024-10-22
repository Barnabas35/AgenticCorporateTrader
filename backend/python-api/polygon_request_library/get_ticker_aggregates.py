
from db_access import DBAccess
from stock_api_access import StockAPIAccess
from function_library.security_string_parsing import firestore_safe


def api_get_ticker_aggregates(request_json):

    # Check if ticker, session token, start date, end date, limit and interval are in the request
    if "ticker" not in request_json:
        return {"status": "No ticker provided."}

    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    if "start_date" not in request_json:
        return {"status": "No start date provided."}

    if "end_date" not in request_json:
        return {"status": "No end date provided."}

    if "interval" not in request_json:
        return {"status": "No interval provided."}

    if "limit" not in request_json:
        limit = 100
    else:
        # Get limit from request arguments
        limit = str(request_json["limit"])

        # Make sure limit is an integer
        if not limit.isnumeric():
            return {"status": "Limit must be an integer."}

        try:
            limit = min(max(int(limit), 10), 1000)
        except ValueError:
            return {"status": "Limit must be an integer."}

    # Get vars from request
    ticker = request_json["ticker"]
    session_token = request_json["session_token"]
    start_date = request_json["start_date"]
    end_date = request_json["end_date"]
    interval = request_json["interval"]

    # Make vars safe for processing
    ticker = firestore_safe(ticker)
    session_token = firestore_safe(session_token)
    start_date = firestore_safe(start_date)
    end_date = firestore_safe(end_date)
    interval = firestore_safe(interval)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get API stock client
    client = StockAPIAccess.get_client()

    # Get ticker aggregates
    ticker_agg = []
    for ta in client.list_aggs(ticker=ticker,
                               multiplier=1,
                               from_=start_date,
                               to=end_date,
                               timespan=interval,
                               limit=limit,
                               adjusted=True,
                               sort="desc"):
        ticker_agg.append(ta)

    return {"ticker_info": ticker_agg, "status": "Success"}
