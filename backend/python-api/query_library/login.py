from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from hashlib import sha256
from function_library.string_generator import new_session_token


def q_login(request_json):

    # Make sure username and password are not empty
    if "email" not in request_json or "password" not in request_json:
        return {"session_token": None, "status": "No email or password provided."}

    # Get username and password from request
    email = request_json["email"]
    password = request_json["password"]

    # Parse username and password to be safe for Firestore
    email = firestore_safe(email)
    password = firestore_safe(password)

    # Hash password
    password_hashed = sha256(password.encode("utf-8")).hexdigest()

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching username and password
    result = (db.collection("users").where(field_path="email", op_string="==", value=email)
              .where(field_path="password", op_string="==", value=password_hashed).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"session_token": None, "status": "Email or password is incorrect."}

    # Else create session token
    session_token = new_session_token()

    # Update user's session token in database
    db.collection("users").document(result[0].id).update({"session_token": session_token})

    # Return session token
    return {"session_token": session_token, "status": "Success"}
