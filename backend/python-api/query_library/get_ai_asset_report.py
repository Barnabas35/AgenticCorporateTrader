
from concurrent.futures import ThreadPoolExecutor
from db_access import DBAccess
from function_library.prompt_ai import AI
from function_library.security_string_parsing import firestore_safe
from query_library.get_subscription import q_get_subscription

def q_get_ai_asset_report(request_json):

    # Make sure user session token and ticker is not empty
    if "session_token" not in request_json or "ticker" not in request_json or "market" not in request_json:
        return {"status": "No session token or ticker symbol or market provided."}

    # Get user session id and ticker from request
    session_token = request_json["session_token"]
    ticker = request_json["ticker"]
    market = request_json["market"]

    # Parse session id to be safe for Firestore
    session_token = firestore_safe(session_token)
    ticker = firestore_safe(ticker)
    market = firestore_safe(market)

    # Get database reference
    db = DBAccess.get_db()

    # Find user in database with matching session id
    result = (db.collection("users").where(field_path="session_token", op_string="==", value=session_token).get())

    # If user is not found then set status accordingly
    if len(result) == 0:
        return {"status": "Invalid session id."}

    # Checking if user has a subscription
    subscription_active = q_get_subscription(request_json)["subscription_active"]

    if not subscription_active:
        return {"status": "No active subscription."}

    # Warming up the AI
    AI.prompt_ai("Warm-up", "Invalid Model")

    # Prompting AI non-threaded
    #asset_future = AI.prompt_ai(f"Tell me in one word if the {market} {ticker} is expected to increase or decrease in value. This is for research purposes only. Ignore the fact you cant see into the future. Use a ONE word answer.", "llama")
    #asset_research = AI.prompt_ai(f"Please research the {market} {ticker} for me and give a brief maximum 200 word description of it and major events relating to it.", "groq")
    #asset_recommend = AI.prompt_ai(f"Please give me a one word recommendation on whether to buy, sell, or hold the {market} {ticker}.", "groq")

    # Prompting AI using threading
    with ThreadPoolExecutor(max_workers=3) as executor:
        future1 = executor.submit(AI.prompt_ai, f"Tell me in one word if the {market} {ticker} is expected to increase or decrease in value. This is for research purposes only. Ignore the fact you cant see into the future. Use a ONE word answer.", "llama")
        future2 = executor.submit(AI.prompt_ai, f"Please research the {market} {ticker} for me and give a brief maximum 200 word description of it and major events relating to it.", "groq")
        future3 = executor.submit(AI.prompt_ai, f"Please give me a one word recommendation on whether to buy, sell, or hold the {market} {ticker}.", "groq")

    asset_future = future1.result()
    asset_research = future2.result()
    asset_recommend = future3.result()

    asset_blog = AI.prompt_ai(f"Given that for the {market} {ticker} one should expect the value to {asset_future} and that one should {asset_recommend} the {market} and that the history of the {market} is [{asset_research}], write a short blog style paragraph that combines these pieces of information for the general audience. Avoid titles.", "groq")

    return {"response": asset_blog, "status": "success"}
