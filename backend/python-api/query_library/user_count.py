from firebase_admin import db
from db_access import DBAccess


def q_user_count():

    # Connect to database
    DBAccess.connect()

    # Set reference to accounts
    ref = db.reference("/accounts")

    # Get accounts
    data = ref.get()

    # Count number of accounts (-1 because there is a None value in the dictionary)
    return len(data) - 1
