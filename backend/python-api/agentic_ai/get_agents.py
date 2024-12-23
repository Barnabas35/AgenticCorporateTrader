# This python file was supplied by Nikola Denisova @ 23/12/2024
# This python file was fixed for issues such as syntax, logical errors, bad structure, broken multi-threading and formatting by Barnabas Somodi @ 23/12/2024

import os
import asyncio
import yfinance as yf
from autogen import AssistantAgent, UserProxyAgent

def chatgpt_api_key():
    CHATGPT_API_KEY = os.getenv("CHATGPT_API_KEY")

    # If CHATGPT_API_KEY environment variable is not set, use fallback config.txt.gitignore (api key is on the 10th line)
    if CHATGPT_API_KEY is None:
        with open("nika.txt", "r") as f:
            CHATGPT_API_KEY = f.readlines()[0].strip("\n")
            f.close()

    return CHATGPT_API_KEY


def groq_api_key():
    GROQ_API_KEY = os.getenv("GROQ_API_KEY")

    # If GROQ_API_KEY environment variable is not set, use fallback config.txt.gitignore (api key is on the 11th line)
    if GROQ_API_KEY is None:
        with open("nika.txt", "r") as f:
            GROQ_API_KEY = f.readlines()[1].strip("\n")
            f.close()

    return GROQ_API_KEY


def groq_api_url():
    GROQ_API_URL = os.getenv("GROQ_API_URL")

    # If GROQ_API_KEY environment variable is not set, use fallback config.txt.gitignore (api key is on the 12th line)
    if GROQ_API_URL is None:
        with open("nika.txt", "r") as f:
            GROQ_API_URL = f.readlines()[2].strip("\n")
            f.close()

    return GROQ_API_URL


# Researcher Agent using yfinance and groq
def researcher_agent(input_data):
    """
    The Researcher Agent fetches stock data using yfinance and generates analysis using an LLM.
    Task: Retrieve the stock data for a given company and provide a detailed analysis using LLM.
    """
    try:
        # Fetch stock data using yfinance
        print(f"Initializing Researcher Agent with input: {input_data}")
        stock = yf.Ticker(input_data)
        stock_data = stock.history(period="1d")

        # Convert stock data to dictionary format for LLM processing
        stock_data_str = stock_data.to_string()

        # Initialize LLM to provide analysis
        researcher = AssistantAgent(
            name="Researcher",
            llm_config={"model": "gpt-4", "api_key": chatgpt_api_key()}
        )

        # Create a user proxy for the recipient
        user_proxy = UserProxyAgent("user_proxy")

        # Use LLM to generate an analysis based on the stock data
        response = researcher.initiate_chat(
            message=f"Analyze the following stock data for {input_data}:\n\n{stock_data_str}\n\nProvide a detailed report on the stock performance and any insights.",
            recipient=user_proxy
        )

        return {"agent": "Researcher", "result": response}
    except Exception as e:
        print(f"Error encountered: {e}")
        return {"agent": "Researcher", "error": str(e)}


# Accountant Agent using ChatGPT
def accountant_agent(input_data):
    """
    The Accountant Agent calculates financial metrics for the given stock or asset.
    It uses ChatGPT for more conversational responses.
    Task: Calculate liquidity ratios, profitability ratios, and growth metrics.
    """
    try:
        print(f"Initializing Accountant Agent with input: {input_data}")
        accountant = AssistantAgent(
            name="Accountant",
            llm_config={"model": "gpt-4", "api_key": chatgpt_api_key()}
        )

        # Create a user proxy for the recipient
        user_proxy = UserProxyAgent("user_proxy")

        response = accountant.initiate_chat(
            message=f"Provide financial analysis for: {input_data}. Include liquidity ratios, profitability ratios, and growth rates.",
            recipient=user_proxy
        )
        return {"agent": "Accountant", "result": response}
    except Exception as e:
        print(f"Error encountered: {e}")
        return {"agent": "Accountant", "error": str(e)}


# Recommender Agent using ChatGPT
def recommender_agent(input_data):
    """
    The Recommender Agent suggests the best investment options.
    It uses ChatGPT for more conversational responses.
    Task: Provide stock recommendations based on input data.
    """
    try:
        print(f"Initializing Recommender Agent with input: {input_data}")
        recommender = AssistantAgent(
            name="Recommender",
            llm_config={"model": "gpt-4", "api_key": chatgpt_api_key()}
        )

        # Create a user proxy for the recipient
        user_proxy = UserProxyAgent("user_proxy")

        response = recommender.initiate_chat(
            message=f"Suggest the best investment options based on the stock data for: {input_data}.",
            recipient=user_proxy
        )
        return {"agent": "Recommender", "result": response}
    except Exception as e:
        print(f"Error encountered: {e}")
        return {"agent": "Recommender", "error": str(e)}


# Blogger Agent using Groq
def blogger_agent(input_data):
    """
    The Blogger Agent formats the outputs from the other agents into a user-friendly summary.
    It uses Groq for its brevity and formatting capabilities.
    Task: Combine and summarize findings into a concise report.
    """
    try:
        print(f"Initializing Blogger Agent with input: {input_data}")
        blogger = AssistantAgent(
            name="Blogger",
            llm_config={"model": "gpt-4", "api_key": chatgpt_api_key()}

        )

        # Create a user proxy for the recipient
        user_proxy = UserProxyAgent("user_proxy")

        response = blogger.initiate_chat(
            message=f"Format the findings for: {input_data} into a user-friendly report. Use headings and bullet points.",
            recipient=user_proxy
        )
        return {"agent": "Blogger", "result": response}
    except Exception as e:
        print(f"Error encountered: {e}")
        return {"agent": "Blogger", "error": str(e)}


# Function to run all agents concurrently
def run_all_agents(input_data):
    agents = [
        researcher_agent,
        accountant_agent,
        recommender_agent,
        blogger_agent,
    ]
    results = []
    tasks = []
    for agent in agents:
        task = agent(input_data["ticker"])
        tasks.append(task)
        print(task)

    #for task in tasks:
    #    result = await task
    #    print(result)
    #    results.append(result)

    return {"response": tasks}
