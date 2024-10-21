
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe


def q_submit_review(request_json):

    # Make sure user session id, review score, and review comment are not empty
    if ("session_token" not in request_json
            or "review_score" not in request_json
            or "review_comment" not in request_json):
        return {"status": "No session token or review score or review comment provided."}

    # Get user session id, review subject, and review description from request
    session_token = request_json["session_token"]
    review_score = request_json["review_score"]
    review_comment = request_json["review_comment"]

    # Parse session token and review comment to be safe for Firestore
    review_comment = firestore_safe(review_comment)
    session_token = firestore_safe(session_token)

    # Make sure review score is a number between 1 and 5
    try:
        if not review_score.isnumeric() or int(review_score) < 1 or int(review_score) > 5:
            return {"status": "Invalid review score."}
        else:
            review_score = int(review_score)
    except ValueError:
        return {"status": "The provided review score is not an int."}

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Get user id
    user_id = result[0].id

    # Find existing review by user
    existing_review = db.collection("reviews").where(field_path="user_id", op_string="==", value=user_id).get()

    # If user already has a review then set status accordingly
    if len(existing_review) > 0:
        return {"status": "User already has a review."}

    # Create new review
    db.collection("reviews").add({"user_id": user_id,
                                  "score": review_score,
                                  "comment": review_comment})

    # Return status
    return {"status": "Success"}
