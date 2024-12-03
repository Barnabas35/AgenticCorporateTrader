
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
from function_library.security_string_parsing import firestore_unsafe


def q_get_client_list(request_json):

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

    # Get document ID
    doc_id = result[0].id

    # Get user type
    user_type = result[0].to_dict()["user_type"]

    # Check if user is a fund administrator
    if user_type == "fa":
        return {"clients": [{"client_name": "Fund Administrator", "client_id": doc_id}], "status": "Success"}

    # Get clients for user
    clients = db.collection("clients").where(field_path="user_id", op_string="==", value=doc_id).stream()

    # Looping through clients to get client names and IDs and adding them to a json list
    client_list = []
    for client in clients:
        client_list.append({"client_name": firestore_unsafe(client.to_dict()["client_name"]), "client_id": client.id})

    # Check if list is empty
    if len(client_list) == 0:
        return {"status": "No clients found."}

    # Return list of clients
    return {"clients": client_list, "status": "Success"}
