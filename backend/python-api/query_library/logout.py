from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe

def q_logout(request_json):

    # Make sure session token is in request
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

    # Get document id
    doc_id = result[0].id

    # Update user's session token in database to an empty string
    db.collection("users").document(doc_id).update({"session_token": ""})

    # Return success
    return {"status": "Success"}
