from db_access import DBAccess


def q_user_count():

    # Get database reference
    db = DBAccess.get_db()

    # Execute query
    result = db.collection("users").count().get()

    # Parse result then return
    return result[0][0].value
