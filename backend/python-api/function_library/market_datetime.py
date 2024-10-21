
from datetime import datetime, timedelta


def get_last_market_day():

    # Get current time
    now = datetime.now()

    # Get yesterday's date (this is because the current day's data may not be available yet)
    yesterday = now - timedelta(days=1)

    # Check if yesterday was a saturday
    if yesterday.weekday() == 5:
        yesterday = yesterday - timedelta(days=1)

    # Check if yesterday was a sunday
    if yesterday.weekday() == 6:
        yesterday = yesterday - timedelta(days=2)

    # Get last open market's date in string format
    last_market_day = yesterday.strftime("%Y-%m-%d")

    return last_market_day


def get_previous_market_day(market_day):

    # Convert market_day to datetime object
    market_day = datetime.strptime(market_day, "%Y-%m-%d")

    # Get previous day
    previous_day = market_day - timedelta(days=1)

    # Check if previous day was a saturday
    if previous_day.weekday() == 5:
        previous_day = previous_day - timedelta(days=1)

    # Check if previous day was a sunday
    if previous_day.weekday() == 6:
        previous_day = previous_day - timedelta(days=2)

    # Get previous open market's date in string format
    previous_market_day = previous_day.strftime("%Y-%m-%d")

    return previous_market_day
