
# This is a singleton class that initializes the Firebase app.

import firebase_admin
from firebase_admin import credentials
import os

# Get database URL from environment variable
databaseURL_ENV = os.getenv("databaseURL")

# If environment variable is not set, use fallback config.txt
if databaseURL_ENV is None:
    with open("config.txt", "r") as f:
        databaseURL_ENV = f.read()
        f.close()


class DBAccess:
    __instance = None

    @staticmethod
    def connect():
        if DBAccess.__instance is None:
            DBAccess()
        return DBAccess.__instance

    def __init__(self):
        if DBAccess.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            cred = credentials.Certificate("agenticcorporatetrader-firebase-adminsdk-hms3b-b39a833119.json")
            firebase_admin.initialize_app(cred, {
                "databaseURL": databaseURL_ENV
            })
            DBAccess.__instance = self
