
from datetime import timedelta
import time
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from query_library.get_client_list import q_get_client_list
from query_library.get_user_assets import q_get_user_assets
from query_library.get_asset_report import q_get_asset_report

def q_download_asset_reports(request_json):

    # Make sure session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Get session token from request
    session_token = request_json["session_token"]

    # Make session token safe for Firestore
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get bucket reference
    bucket = DBAccess.get_bucket()

    # Get current time in the format hhmmss_ddmmyy
    current_time = time.strftime("%H%M%S_%d%m%y")

    # Get username
    username = result[0].to_dict()["username"]

    # Create a file in the bucket
    blob = bucket.blob(f"user_reports/{username}_{current_time}.csv")

    # Creating csv
    csv = f"Username:,{username},,\n"
    csv += "Client Name,Market,Ticker Symbol,Total Invested,Profit\n"

    # Get all clients
    client_list = q_get_client_list(request_json)["clients"]

    for client in client_list:
        # Get all assets
        for market in ["crypto", "stocks"]:
            assets = q_get_user_assets({"session_token": session_token, "market": market, "client_id": client["client_id"]})["ticker_symbols"]

            for asset in assets:
                # Get asset report
                asset_report = q_get_asset_report({"session_token": session_token, "market": market, "client_id": client["client_id"], "ticker": asset})
                csv += f"{client['client_name']},{market},{asset},${asset_report['total_usd_invested']},${asset_report['profit']}\n"

    # Upload the file
    blob.upload_from_string(csv)

    # Get url of the file for download
    url = blob.generate_signed_url(timedelta(minutes=15))

    # Return URL
    return {"url": url, "status": "success"}