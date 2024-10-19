
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe


def q_resolve_support_ticket(request_json):

    # Make sure user session token and ticket id and response_subject and response_body are not empty
    if ("session_token" not in request_json
            or "ticket_id" not in request_json)\
            or "response_subject" not in request_json\
            or "response_body" not in request_json:
        return {"status": "No session id or ticket id provided."}

    # Get user session id and ticket id from request
    session_token = request_json["session_token"]
    ticket_id = request_json["ticket_id"]
    response_subject = request_json["response_subject"]
    response_body = request_json["response_body"]

    # Parse session id and ticket id to be safe for Firestore
    session_token = firestore_safe(session_token)
    ticket_id = firestore_safe(ticket_id)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Get user type
    user_type = result[0].to_dict()["user_type"]

    # Making sure user type is admin
    if user_type != "admin":
        return {"status": "Access Denied."}

    # Get support ticket with matching ticket id
    result = (db.collection("support_tickets").document(ticket_id).get())

    # If ticket is not found then set status accordingly
    if not result.exists:
        return {"status": "Invalid ticket id."}

    # Get ticket document ID
    ticket_doc_id = result.id

    # Update ticket status to resolved
    db.collection("support_tickets").document(ticket_doc_id).update({"issue_status": "resolved"})

    # Get user ID from ticket
    user_id = result.to_dict()["user_id"]

    # Get user from user_id
    result = (db.collection("users").document(user_id).get())

    # If user is not found then set status accordingly
    if not result.exists:
        return {"status": "User not found."}

    # Get user email
    user_email = result.to_dict()["email"]

    # Send email to user with response
    # To be implemented

    # Return status
    return {"status": "Success"}
