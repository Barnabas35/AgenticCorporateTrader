
from db_access import DBAccess
from function_library.security_string_parsing import firestore_safe
import time
import stripe
from function_library.stripe_api_key import get_stripe_api_key

def q_add_balance(request_json):

        # Make sure user session token and asset_quantity are not empty
        if "session_token" not in request_json or "usd_quantity" not in request_json:
            return {"status": "No session token or usd quantity provided."}

        # Get user session id and asset quantity from request
        session_token = request_json["session_token"]
        usd_quantity = str(request_json["usd_quantity"])

        # Parse session id to be safe for Firestore
        session_token = firestore_safe(session_token)

        # Parse asset quantity to be safe for Firestore
        try:
            usd_quantity = int(firestore_safe(usd_quantity))
        except ValueError:
            return {"status": "Invalid usd quantity."}

        # Get database reference
        db = DBAccess.get_db()

        # Find user in database with matching session id
        result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

        # If user is not found then set status accordingly
        if len(result) == 0:
            return {"status": "Invalid session id."}

        # Getting user ID
        user_id = result[0].id

        # Get stripe api key
        stripe.api_key = get_stripe_api_key()

        # Create payment intent
        payment_intent = stripe.PaymentIntent.create(
            amount=usd_quantity * 100,
            currency='usd',
            automatic_payment_methods={"enabled": True},
        )

        # Adding transaction to transaction log
        db.collection("users").document(user_id).collection("transaction_log").add({"market": "currency",
                                                                                    "transaction_type": "purchase",
                                                                                    "asset_quantity": usd_quantity,
                                                                                    "asset_value": 1,
                                                                                    "client_id": user_id,
                                                                                    "usd_quantity": usd_quantity,
                                                                                    "ticker_symbol": "USD",
                                                                                    "unix_timestamp": int(time.time()),
                                                                                    "payment_intent_id": payment_intent.id,
                                                                                    "payment_intent_status": "pending"})

        # Return status and client secret
        return {"client_secret": payment_intent["client_secret"], "status": "Success"}