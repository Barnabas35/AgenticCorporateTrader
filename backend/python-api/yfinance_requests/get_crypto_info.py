
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import yfinance as yf

def api_get_crypto_info(request_json):

    # Making sure session token and ticker are in request
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    if "crypto" not in request_json:
        return {"status": "No crypto provided."}

    # Get session token and ticker from request
    session_token = request_json["session_token"]
    crypto = request_json["crypto"] + "-USD"

    # Making vars safe for firestore
    session_token = firestore_safe(session_token)
    crypto = firestore_safe(crypto)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get ticker info
    try:
        ticker_info = yf.Ticker(crypto)
    except (ValueError, KeyError):
        return {"status": "Ticker not supported by yfinance."}

    # Get ticker history
    latest_price = ticker_info.history(period="1d", interval="1m").tail(1)['Close'].iloc[0]

    # Getting details
    ticker_details = ticker_info.info

    # Compiling response
    crypto_info = {
        "symbol": crypto[:-4],
        "name": ticker_details["longName"][0:-4],
        "latest_price": latest_price,
        "description": ticker_details["description"],
        "previous_close": ticker_details["previousClose"],
        "open": ticker_details["open"],
        "high": ticker_details["dayHigh"],
        "low": ticker_details["dayLow"],
        "volume": ticker_details["volume"],
    }

    return {"crypto_info": crypto_info, "status": "Success"}
