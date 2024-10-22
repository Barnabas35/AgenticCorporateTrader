
from db_access import DBAccess
from stock_api_access import StockAPIAccess
from function_library.security_string_parsing import firestore_safe


def api_get_ticker_info(request_json):

    # Check if ticker and session token are in the request
    if "ticker" not in request_json:
        return {"status": "No ticker provided."}

    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Get ticker and session token from request
    ticker = request_json["ticker"]
    session_token = request_json["session_token"]

    # Make vars safe for processing
    ticker = firestore_safe(ticker)
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

    # Get ticker details
    ticker_details = client.get_ticker_details(ticker)

    # Get ticker snapshot
    ticker_snapshot = client.get_snapshot_ticker("stocks", ticker)

    # Compiling response
    ticker_info = {
        "symbol": ticker,
        "company_name": ticker_details.name,
        "company_description": ticker_details.description,
        "homepage": ticker_details.homepage_url,
        "employee_count": ticker_details.total_employees,
        "currency": ticker_details.currency_name,
        "open_price": ticker_snapshot.day.open,
        "close_price": ticker_snapshot.day.close,
        "high_price": ticker_snapshot.day.high,
        "low_price": ticker_snapshot.day.low,
        "volume": ticker_snapshot.day.volume,
        "change_percentage": ticker_snapshot.todays_change_percent,
    }

    return {"ticker_info": ticker_info, "status": "Success"}
