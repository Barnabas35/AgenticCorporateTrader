
import os
from groq import Groq

class AI:
    __instance = None

    @staticmethod
    def prompt_ai(query, ai_type="groq"):
        if AI.__instance is None:
            AI()

        if ai_type == "groq":
            return AI.__instance.groq(query)
        else:
            return {"status": "Invalid AI type."}

    def __init__(self):
        if AI.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            AI.__instance = self

            # Get GROQ_URL from environment variable
            self.GROQ_API_KEY = os.getenv("GROQ_API_KEY")

            # If GROQ_URL_ENV environment variable is not set, use fallback config.txt
            if self.GROQ_API_KEY is None:
                with open("config.txt", "r") as f:
                    self.GROQ_API_KEY = f.readlines()[6].strip("\n")
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