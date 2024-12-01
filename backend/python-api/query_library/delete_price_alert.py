
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe

def q_delete_price_alert(request_json):

        # Make sure user session token and alert id are not empty
        if ("session_token" not in request_json
                or "alert_id" not in request_json):
            return {"status": "No session token or alert id provided."}

        # Get user session token and alert id from request
        session_token = request_json["session_token"]
        alert_id = request_json["alert_id"]

        # Parse session token and alert id to be safe for Firestore
        session_token = firestore_safe(session_token)
        alert_id = firestore_safe(alert_id)

        # Get database reference
        db = DBAccess.get_db()

        # Find user in database with matching session token
        result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

        # If user is not found then set status accordingly
        if len(result) == 0:
            return {"status": "Invalid session token."}

        # Get alert with matching alert id
        result = (db.collection("price_alerts").document(alert_id).get())

        # If alert is not found then set status accordingly
        if not result.exists:
            return {"status": "Invalid price alert id."}

        # Get price alert document ID
        alert_doc_id = result.id

        # Delete alert
        db.collection("price_alerts").document(alert_doc_id).delete()

        # Return success status
        return {"status": "Success"}
