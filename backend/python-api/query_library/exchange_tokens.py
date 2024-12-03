
from firebase_admin import auth
from db_access import DBAccess
from function_library.string_generator import new_session_token


def q_exchange_tokens(request_json):

    # Make sure auth token is not empty
    if "auth_token" not in request_json:
        return {"status": "No auth token provided."}

    # Get auth token from request
    auth_token = request_json["auth_token"]

    # Verify the auth token
    try:
        decoded_token = auth.verify_id_token(auth_token)
    except:
        return {"status": "Invalid auth token."}

    # Retrieve the user ID from the decoded token
    uid = decoded_token["uid"]
    email = decoded_token["email"]

    # Connect to the database
    db = DBAccess.get_db()

    # Finding user with matching email
    result = db.collection("users").where(field_path="email", op_string="==", value=email).get()

    # If a record is found, add the user ID to the record
    if len(result) != 0:
        db.collection("users").document(result[0].id).update({"uid": uid})

    # Finding the user in the database with the matching user ID
    result = db.collection("users").where(field_path="uid", op_string="==", value=uid).get()

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Success: Register User"}

    # Create session token
    session_token = new_session_token()

    # Update user's session token in database
    db.collection("users").document(result[0].id).update({"session_token": session_token})

    # Return session token
    return {"session_token": session_token, "status": "Success"}
