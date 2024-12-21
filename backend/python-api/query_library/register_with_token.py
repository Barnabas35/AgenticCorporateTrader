
from firebase_admin import auth
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import requests


def q_register_with_token(request_json):

    # Making sure auth token and user type are not empty
    if "auth_token" not in request_json or "user_type" not in request_json:
        return {"status": "No auth token or user type provided."}

    # Get auth token and user type from request
    auth_token = request_json["auth_token"]
    user_type = request_json["user_type"]

    # make user type safe for Firestore
    user_type = firestore_safe(user_type)

    # Validate user type
    if user_type != "admin" and user_type != "fa" and user_type != "fm":
        return {"status": "Invalid user type."}

    # Connect to the database
    db = DBAccess.get_db()

    # Verify the auth token
    try:
        decoded_token = auth.verify_id_token(auth_token)
    except:
        return {"status": "Invalid auth token."}

    # Retrieve email, uid, and name from the decoded token
    email = decoded_token["email"]
    uid = decoded_token["uid"]
    username = decoded_token["name"]
    profile_pic = decoded_token["picture"]

    #Make email safe for Firestore
    email = firestore_safe(email)

    # Finding matching email in database
    result = db.collection("users").where(field_path="email", op_string="==", value=email).get()

    # Deny registration if email is already in use
    if len(result) != 0:
        return {"status": "Email already exists."}

    # Create new user
    db.collection("users").add({"username": username,
                                "email": email,
                                "user_type": user_type,
                                "session_token": "",
                                "uid": uid})

    # Get user id
    result = db.collection("users").where(field_path="email", op_string="==", value=email).get()
    user_id = result[0].id

    # Fetching profile picture from URL
    response = requests.get(profile_pic, stream=True)

    # If response is successful, upload profile picture to Firebase Storage
    if response.status_code == 200:

        # Get bucket reference
        bucket = DBAccess.get_bucket()

        # Creating new user profile picture
        path = f"user_profiles/{user_id}.png"
        blob = bucket.blob(path)

        # Upload profile picture
        blob.upload_from_string(response.content, content_type="image/png")

    # Return status
    return {"status": "Success"}
