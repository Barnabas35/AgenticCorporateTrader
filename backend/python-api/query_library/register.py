from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from hashlib import sha256
from function_library.string_generator import new_session_token


def q_register(request_json):

    # Make sure username and password are not empty
    if "username" not in request_json or "password" not in request_json or "email" not in request_json:
        return {"status": "No username or password or email provided."}

    # Get username and password from request
    username = request_json["username"]
    password = request_json["password"]
    email = request_json["email"]

    # Parse username and password to be safe for Firestore
    username = firestore_safe(username)
    password = firestore_safe(password)
    email = firestore_safe(email)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching username
    result = (db.collection("users").where(field_path="username", op_string="==", value=username).get())

    # If user is found then set status accordingly
    if len(result) != 0:
        return {"status": "Username already exists."}

    # Find user in database with matching email
    result = (db.collection("users").where(field_path="email", op_string="==", value=email).get())

    # If user is found then set status accordingly
    if len(result) != 0:
        return {"status": "Email already exists."}

    # Hash password
    password_hashed = sha256(password.encode("utf-8")).hexdigest()

    # Create new user
    db.collection("users").add({"username": username, "password": password_hashed, "email": email, "session_token": ""})

    # Return status
    return {"status": "Success"}
