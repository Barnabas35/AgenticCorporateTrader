
import os
import json
import requests
from groq import Groq

class AI:
    __instance = None

    @staticmethod
    def prompt_ai(query, ai_type="groq"):
        if AI.__instance is None:
            AI()

        if ai_type == "groq":
            return AI.__instance.groq(query)
        elif ai_type == "llama":
            return AI.__instance.llama(query)
        elif ai_type == "openai":
            return AI.__instance.openai(query)
        else:
            return {"status": "Invalid AI type."}

    def __init__(self):
        if AI.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            AI.__instance = self

            # Get GROQ_API_KEY from environment variable
            self.GROQ_API_KEY = os.getenv("GROQ_API_KEY")

            # If GROQ_API_KEY environment variable is not set, use fallback config.txt
            if self.GROQ_API_KEY is None:
                with open("config.txt", "r") as f:
                    self.GROQ_API_KEY = f.readlines()[6].strip("\n")
                    f.close()

            # Get LLAMA_URL from environment variable
            self.LLAMA_URL = os.getenv("LLAMA_URL")

            # If LLAMA_URL environment variable is not set, use fallback config.txt
            if self.LLAMA_URL is None:
                with open("config.txt", "r") as f:
                    self.LLAMA_URL = f.readlines()[8].strip("\n")
                    f.close()

            # Get OPENAI_API_KEY from environment variable
            self.OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

            # If OPENAI_API_KEY environment variable is not set, use fallback config.txt
            if self.OPENAI_API_KEY is None:
                with open("config.txt", "r") as f:
                    self.OPENAI_API_KEY = f.readlines()[9].strip("\n")
                    f.close()


    def groq(self, query):

        client = Groq(
            api_key = self.GROQ_API_KEY,
        )

        chat_completion = client.chat.completions.create(
            messages=[
                {
                    "role": "user",
                    "content": f"{query}",
                }
            ],
            model="llama3-8b-8192",
        )

        return chat_completion.choices[0].message.content


    def llama(self, query):

        data = {
            "model": "llama3.2",
            "prompt": f"{query}",
            "stream": False
        }

        try:
            # Send POST request with JSON data
            response = requests.post(self.LLAMA_URL, json=data)

            # Check response status
            if response.status_code == 200:
                return response.json()["response"]
            else:
                return "[No Info]"

        except Exception as e:
            print(f"An error occurred: {e}")
            return "[No Info]"


    def openai(self, query):

        data = {
            "model": "gpt-4o-mini",
            "stream": False,
            "messages": [
                {
                    "role": "system",
                    "content": "You are a stock and crypto market researcher AI assistant.",
                },
                {
                    "role": "user",
                    "content": f"{query}",
                }
            ]
        }

        try:
            # Send POST request with JSON data
            response = requests.post("https://api.openai.com/v1/chat/completions", headers={"Authorization": f"Bearer {self.OPENAI_API_KEY}", "Content-Type": "application/json"}, json=data)

            # Check response status
            if response.status_code == 200:
                return response.json()["choices"][0]["message"]["content"]
            else:
                return "[No Info]"

        except Exception as e:
            print(f"An error occurred: {e}")
            return "[No Info]"
