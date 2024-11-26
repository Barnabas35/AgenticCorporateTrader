
import stripe
from function_library.stripe_api_key import get_stripe_webhook_secret
from db_access import DBAccess

def q_payment_webhook(request):
    payload = request.get_data(as_text=True)
    sig_header = request.headers.get('Stripe-Signature')
    endpoint_secret = get_stripe_webhook_secret()

    try:
        event = stripe.Webhook.construct_event(
            payload, sig_header, endpoint_secret
        )

    except ValueError as e:
        # Invalid payload
        return {'error': 'Invalid payload'}

    except stripe.error.SignatureVerificationError as e:
        # Invalid signature
        return {'error': 'Invalid signature'}

    # Handle the event
    if event['type'] == 'payment_intent.succeeded':
        payment_intent = event['data']['object']

        # Fulfill the order or update your database
        print(f"PaymentIntent {payment_intent['id']} succeeded.")

        # Get database reference
        db = DBAccess.get_db()

        # Get a list of all user ids
        user_ids = db.collection("users").list_documents()

        # Loop through all user ids
        for user_id in user_ids:

            # Get transaction log where market is currency
            transaction_log = db.collection("users").document(user_id.id).collection("transaction_log").where(field_path="market", op_string="==", value="currency").stream()

            # Loop through transaction log
            for transaction in transaction_log:

                # Check if transaction type is purchase and payment intent status is pending
                if transaction.to_dict()["transaction_type"] == "purchase" and transaction.to_dict()["payment_intent_status"] == "pending":

                    # Check if payment intent id matches the payment intent id of the event
                    if transaction.to_dict()["payment_intent_id"] == payment_intent['id']:

                        # Update payment intent status to succeeded
                        transaction.reference.update({"payment_intent_status": "succeeded"})

                        # Break out of the loop
                        break


    elif event['type'] == 'payment_intent.payment_failed' or event['type'] == 'payment_intent.canceled':
        payment_intent = event['data']['object']

        # Handle the failure
        print(f"PaymentIntent {payment_intent['id']} failed.")

        # Get database reference
        db = DBAccess.get_db()

        # Get a list of all user ids
        user_ids = db.collection("users").list_documents()

        # Loop through all user ids
        for user_id in user_ids:

            # Get transaction log where market is currency
            transaction_log = db.collection("users").document(user_id.id).collection("transaction_log").where(field_path="market", op_string="==", value="currency").stream()

            # Loop through transaction log
            for transaction in transaction_log:

                # Check if transaction type is purchase and payment intent status is pending
                if transaction.to_dict()["transaction_type"] == "purchase" and transaction.to_dict()["payment_intent_status"] == "pending":

                    # Check if payment intent id matches the payment intent id of the event
                    if transaction.to_dict()["payment_intent_id"] == payment_intent['id']:

                        # Update payment intent status to failed
                        transaction.reference.update({"payment_intent_status": "failed/cancelled"})

                        # Break out of the loop
                        break

    return {'status': 'success'}
