
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe, firestore_unsafe


def q_get_user_list(request_json):

    # Making sure session token is not empty
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

    # Getting user type
    user_type = result[0].to_dict()["user_type"]

    # Making sure user type is admin
    if user_type != "admin":
        return {"status": "Access Denied."}

    # Get all users
    users = db.collection("users").get()

    # List of users
    user_list = []

    # Loop through all users
    for user in users:
        user_data = user.to_dict()

        user_data["id"] = user.id
        user_data["email"] = firestore_unsafe(user_data["email"])
        
        user_list.append(user_data)

    # Return user list
    return {"user_list": user_list, "status": "Success"}
