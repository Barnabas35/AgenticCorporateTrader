
# This is a singleton class that initializes the Firebase app.

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from firebase_admin import storage
import os
import base64


class DBAccess:
    __instance = None
    db = None
    bucket = None

    # Creates a new instance of the class if there is not one already,
    # initializes the Firebase app and returns the database object
    @staticmethod
    def get_db():
        if DBAccess.__instance is None:
            DBAccess()

        if DBAccess.db is None:
            DBAccess.db = firestore.client()

        return DBAccess.db

    # Creates a new instance of the class if there is not one already,
    # initializes the Firebase app and returns the storage object
    @staticmethod
    def get_bucket():
        if DBAccess.__instance is None:
            DBAccess()

        if DBAccess.bucket is None:
            DBAccess.bucket = storage.bucket()

        return DBAccess.bucket

    def __init__(self):
        if DBAccess.__instance is not None:
            raise Exception("This class is a singleton!")
        else:

            # Get database URL from environment variable
            DB_URL_ENV = os.getenv("DB_URL")
            # Get storage URL from environment variable
            STORAGE_URL_ENV = os.getenv("STORAGE_URL")
            # Get certificate from environment variable
            CERT_ENV = os.getenv("CERT")

            # If DB_URL_ENV environment variable is not set, use fallback config.txt
            if DB_URL_ENV is None:
                with open("config.txt", "r") as f:
                    DB_URL_ENV = f.readlines()[0].strip("\n")
                    f.close()

            # If STORAGE_URL_ENV environment variable is not set, use fallback config.txt
            if STORAGE_URL_ENV is None:
                with open("config.txt", "r") as f:
                    STORAGE_URL_ENV = f.readlines()[1].strip("\n")
                    f.close()

            # Get certificate from environment variable or use fallback local file
            if CERT_ENV is None:
                # Use local certificate file
                cred = credentials.Certificate("agenticcorporatetrader-firebase-adminsdk-hms3b-b39a833119.json")
            else:
                # Decode certificate from base64
                decoded_cert = base64.b64decode(CERT_ENV)

                # Write certificate to file
                with open("cert.json", "wb") as f:
                    f.write(decoded_cert)
                    f.close()

                # Use certificate file
                cred = credentials.Certificate("cert.json")

            # Initialize Firebase app
            firebase_admin.initialize_app(cred, {
                "databaseURL": DB_URL_ENV,
                "storageBucket": STORAGE_URL_ENV
            })
            DBAccess.__instance = self
