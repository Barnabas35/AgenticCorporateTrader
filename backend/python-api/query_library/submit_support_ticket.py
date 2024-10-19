
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import time


def q_submit_support_ticket(request_json):

    # Make sure user session id, issue subject, and issue description are not empty
    if ("session_token" not in request_json
            or "issue_subject" not in request_json
            or "issue_description" not in request_json):
        return {"status": "No session id or issue subject or issue description provided."}

    # Get user session id, issue subject, and issue description from request
    session_token = request_json["session_token"]
    issue_subject = request_json["issue_subject"]
    issue_description = request_json["issue_description"]

    # Parse issue subject and issue description to be safe for Firestore
    issue_subject = firestore_safe(issue_subject)
    issue_description = firestore_safe(issue_description)
    session_token = firestore_safe(session_token)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Get user id
    user_id = result[0].id

    # Get unix timestamp
    unix_timestamp = int(time.time())

    # Create new support ticket
    db.collection("support_tickets").add({"user_id": user_id,
                                          "issue_subject": issue_subject,
                                          "issue_description": issue_description,
                                          "issue_status": "open",
                                          "unix_timestamp": unix_timestamp})

    # Return status
    return {"status": "Success"}
