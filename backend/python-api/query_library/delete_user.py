from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe, firestore_unsafe


def q_delete_user(request_json):

    # Make sure username and password are not empty
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Get session token from request
    session_token = request_json["session_token"]

    # Parse session token to be safe for Firestore
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get user id
    user_id = result[0].id

    # Delete user
    db.collection("users").document(user_id).delete()

    # Return email
    return {"status": "Success"}
