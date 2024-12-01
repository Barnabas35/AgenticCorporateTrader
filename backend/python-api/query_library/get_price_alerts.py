
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe

def q_get_price_alerts(request_json):

        # Make sure user session token is not empty
        if "session_token" not in request_json:
            return {"status": "No session token provided."}

        # Get user session token from request
        session_token = request_json["session_token"]

        # Parse session token to be safe for Firestore
        session_token = firestore_safe(session_token)

        # Get database reference
        db = DBAccess.get_db()

        # Find user in database with matching session token
        result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

        # If user is not found then set status accordingly
        if len(result) == 0:
            return {"status": "Invalid session token."}

        # Get document ID
        doc_id = result[0].id

        # Get alerts for user
        alerts = db.collection("price_alerts").where(field_path="user_id", op_string="==", value=doc_id).stream()

        # Looping through alerts to get alert details and adding them to a json list
        alert_list = []
        for alert in alerts:
            alert_list.append({"ticker": alert.to_dict()["ticker"],
                            "market": alert.to_dict()["market"],
                            "price": alert.to_dict()["price"],
                            "alert_id": alert.id})

        # Check if list is empty
        if len(alert_list) == 0:
            return {"status": "No alerts found."}

        # Return list of alerts
        return {"alerts": alert_list, "status": "Success"}
