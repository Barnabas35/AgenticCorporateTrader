
# This is a singleton class that provides access to the polygon client

from polygon import RESTClient
import requests
import os


class StockAPIAccess:
    __instance = None

    @staticmethod
    def get_client():
        if StockAPIAccess.__instance is None:
            StockAPIAccess()
        return StockAPIAccess.__instance.client

    @staticmethod
    def request(url):
        api_key = StockAPIAccess.__instance.API_KEY_ENV
        response = requests.get(url, headers={"Authorization": f"Bearer {api_key}"})

        if response.status_code != 200:
            return response.status_code

        return response.json()

    def __init__(self):
        if StockAPIAccess.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            StockAPIAccess.__instance = self

            # Try to get API key from environment variable
            API_KEY_ENV = os.getenv("POLYGON_API_KEY")

            # If API_KEY_ENV environment variable is not set, use fallback config.txt
            if API_KEY_ENV is None:
                with open("config.txt", "r") as f:
                    API_KEY_ENV = f.readlines()[2].strip("\n")
                    f.close()

            self.client = RESTClient(API_KEY_ENV)
