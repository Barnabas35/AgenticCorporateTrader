
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe, firestore_unsafe

def q_admin_delete_user(request_json):

    # Making sure session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Making sure id is not empty
    if "id" not in request_json:
        return {"status": "No user id provided."}

    # Get session token and id from request
    session_token = request_json["session_token"]
    user_id = request_json["id"]

    # Parse session token and id to be safe for Firestore
    session_token = firestore_safe(session_token)
    user_id = firestore_safe(user_id)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session token."}

    # Get user type
    user_type = result[0].to_dict()["user_type"]

    # Making sure user type is admin
    if user_type != "admin":
        return {"status": "Access Denied."}

    # Get user from user_id
    result = (db.collection("users").document(user_id).get())

    # If user is not found then set status accordingly
    if not result.exists:
        return {"status": "Invalid user id."}

    # Delete user
    db.collection("users").document(user_id).delete()

    # Return status
    return {"status": "Success"}
