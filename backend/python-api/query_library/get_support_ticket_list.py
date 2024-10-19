
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from function_library.security_string_parsing import firestore_unsafe


def q_get_support_ticket_list(request_json):

    # Make sure user session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session id provided."}

    # Get user session id from request
    session_token = request_json["session_token"]

    # Parse session id to be safe for Firestore
    session_token = firestore_safe(session_token)

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

    # Get support ticket list
    support_tickets = db.collection("support_tickets").stream()

    # Looping through support tickets to get ticket details and adding them to a json list
    support_ticket_list = []
    for support_ticket in support_tickets:
        support_ticket_list.append({"issue_subject": firestore_unsafe(support_ticket.to_dict()["issue_subject"]),
                                    "issue_description": firestore_unsafe(support_ticket.to_dict()["issue_description"]),
                                    "issue_status": support_ticket.to_dict()["issue_status"],
                                    "unix_timestamp": support_ticket.to_dict()["unix_timestamp"],
                                    "user_id": support_ticket.to_dict()["user_id"],
                                    "ticket_id": support_ticket.id})

    # Check if list is empty
    if len(support_ticket_list) == 0:
        return {"status": "No support tickets found."}

    # Return list of support tickets
    return {"support_tickets": support_ticket_list, "status": "Success"}
