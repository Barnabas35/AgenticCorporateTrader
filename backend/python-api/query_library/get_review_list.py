
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from function_library.security_string_parsing import firestore_unsafe


def q_get_review_list(request_json):

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

    # Make sure user type is admin
    user_type = result[0].to_dict()["user_type"]

    if user_type != "admin":
        return {"status": "Access Denied."}

    # Get all reviews
    reviews = db.collection("reviews").stream()

    # Loop through reviews to get review details and add them to a json list
    review_list = []
    for review in reviews:
        review_list.append({"user_id": review.to_dict()["user_id"],
                            "score": review.to_dict()["score"],
                            "comment": firestore_unsafe(review.to_dict()["comment"]),
                            "review_id": review.id})

    # Check if list is empty
    if len(review_list) == 0:
        return {"status": "No reviews found."}

    # Return list of reviews
    return {"reviews": review_list, "status": "Success"}
