
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import yfinance as yf

def api_get_crypto_aggregates(request_json):

    # Check if crypto, session token, start date, end date and interval are in the request
    if "crypto" not in request_json:
        return {"status": "No crypto provided."}

    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    if "start_date" not in request_json:
        return {"status": "No start date provided."}

    if "end_date" not in request_json:
        return {"status": "No end date provided."}

    if "interval" not in request_json:
        return {"status": "No interval provided."}

    # Get vars from request
    crypto = request_json["crypto"] + "-USD"
    session_token = request_json["session_token"]
    start_date = request_json["start_date"]
    end_date = request_json["end_date"]
    interval = request_json["interval"]

    # Make vars safe for processing
    crypto = firestore_safe(crypto)
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

    # Get crypto aggregate
    try:
        crypto_info = yf.Ticker(crypto)
    except (ValueError, KeyError):
        return {"status": "Crypto not supported by yfinance."}

    # Get crypto history
    crypto_history = crypto_info.history(start=start_date, end=end_date, interval=interval)

    # Looping through crypto history
    crypto_aggregates = []
    for index, row in crypto_history.iterrows():
        crypto_aggregates.append({
            "date": index.strftime("%Y-%m-%d %H:%M:%S"),
            "open": row["Open"],
            "high": row["High"],
            "low": row["Low"],
            "close": row["Close"],
            "volume": row["Volume"],
        })

    # Flipping the list
    crypto_aggregates = crypto_aggregates[::-1]

    return {"crypto_aggregates": crypto_aggregates, "status": "Success"}
