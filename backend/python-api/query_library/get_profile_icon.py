
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from datetime import timedelta


def q_get_profile_icon(request_json):

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

    # Get document ID
    doc_id = result[0].id

    # Get bucket reference
    bucket = DBAccess.get_bucket()

    # Construct path to profile icon
    path = f"user_profiles/{doc_id}.jpg"

    # Get profile icon from storage
    blob = bucket.blob(path)

    # If profile icon does not exist retrieve default icon
    if not blob.exists():
        blob = bucket.blob("user_profiles/default.jpg")

    # Get URL of profile icon that is valid for 3 minutes
    url = blob.generate_signed_url(timedelta(minutes=3))

    # Return URL
    return {"url": url, "status": "Success"}
