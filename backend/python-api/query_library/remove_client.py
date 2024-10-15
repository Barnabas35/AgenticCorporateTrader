
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe


def q_remove_client(request_json):
    # Make sure session token is not empty
    if "session_token" not in request_json:
        return {"status": "No session token provided."}

    # Make sure client name is not empty
    if "client_name" not in request_json:
        return {"status": "No client name provided."}

    # Get session token and client name from request
    session_token = request_json["session_token"]
    client_name = request_json["client_name"]

    # Make session token and client name safe for Firestore
    session_token = firestore_safe(session_token)
    client_name = firestore_safe(client_name)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session token
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Incorrect session token."}

    # Get document ID
    doc_id = result[0].id

    # Get client
    result = (db.collection("clients").where(field_path="user_id", op_string="==", value=doc_id)
              .where(field_path="client_name", op_string="==", value=client_name).get())

    # If client does not exist then set status accordingly
    if len(result) == 0:
        return {"status": "Client does not exist."}

    # Delete client
    db.collection("clients").document(result[0].id).delete()

    # Return success
    return {"status": "Success"}
