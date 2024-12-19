
import os

def get_stripe_api_key():
    # Get stripe api key from environment variable
    STRIPE_API_KEY = os.getenv("STRIPE_API_KEY")

    # If STRIPE_API_KEY environment variable is not set, use fallback config.txt (api key is on the 4th line)
    if STRIPE_API_KEY is None:
        with open("config.txt", "r") as f:
            STRIPE_API_KEY = f.readlines()[3].strip("\n")
            f.close()

    return STRIPE_API_KEY


def get_stripe_webhook_secret():
    # Get stripe webhook secret from environment variable
    STRIPE_WEBHOOK_SECRET = os.getenv("STRIPE_WEBHOOK_SECRET")

    # If STRIPE_WEBHOOK_SECRET environment variable is not set, use fallback config.txt (webhook secret is on the 5th line)
    if STRIPE_WEBHOOK_SECRET is None:
        with open("config.txt", "r") as f:
            STRIPE_WEBHOOK_SECRET = f.readlines()[4].strip("\n")
            f.close()

    return STRIPE_WEBHOOK_SECRET
