
from function_library.send_email import send_email_smtp
from function_library.security_string_parsing import firestore_unsafe
from stock_api_access import StockAPIAccess
from db_access import DBAccess
import yfinance as yf
import time


def check_alerts():

    previous_ticker_prices_stocks = {}
    previous_ticker_prices_crypto = {}

    while True:
        try:
            current_ticker_prices_stocks = {}
            current_ticker_prices_crypto = {}

            # Get database reference
            db = DBAccess.get_db()

            # Get all price alerts
            alerts = db.collection("price_alerts").get()

            # Creating ticker watchlist
            stock_watchlist = set()
            crypto_watchlist = set()

            # Looping through all alerts and adding tickers to watchlist
            for alert in alerts:
                alert_data = alert.to_dict()

                if alert_data["market"] == "stocks":
                    stock_watchlist.add(alert_data["ticker"])
                elif alert_data["market"] == "crypto":
                    crypto_watchlist.add(alert_data["ticker"])

            # Finding current prices of tickers in watchlist
            client = StockAPIAccess.get_client()

            for stock in stock_watchlist:
                snapshot = client.get_snapshot_ticker("stocks", stock)
                current_ticker_prices_stocks[stock] = snapshot.day.close

            for crypto in crypto_watchlist:
                crypto_usd = f"{crypto}-USD"
                ticker = yf.Ticker(crypto_usd)
                latest_price = ticker.history(period="1d", interval="1m").tail(1)['Close'].iloc[0]
                current_ticker_prices_crypto[crypto] = latest_price

            # Checking if any alerts have been triggered
            for alert in alerts:
                alert_dict = alert.to_dict()
                ticker = alert_dict["ticker"]
                market = alert_dict["market"]
                price = alert_dict["price"]
                alert_id = alert.id
                user_id = alert_dict["user_id"]

                market_closed_indicator = [None, 0]
                # Making sure market is stocks AND that the market is open by checking if the price is not None or 0
                if market == "stocks" and (current_ticker_prices_stocks.get(ticker, None) not in market_closed_indicator):
                    current_ticker_price = current_ticker_prices_stocks[ticker]
                    previous_ticker_price = previous_ticker_prices_stocks.get(ticker, None)

                    if previous_ticker_price is not None:
                        if (previous_ticker_price <= price <= current_ticker_price) or (previous_ticker_price >= price >= current_ticker_price):

                            # Remove alert from database
                            db.collection("price_alerts").document(alert_id).delete()

                            # Get user email
                            user = db.collection("users").document(user_id).get()
                            user_email = firestore_unsafe(user.to_dict()["email"])

                            # Send email to user
                            print(send_email_smtp(user_email, f"Price Alert For {ticker}", f"Your price alert for the stock {ticker} set at ${price} has been triggered. The price is now ${current_ticker_price}."))

                elif market == "crypto":
                    current_ticker_price = current_ticker_prices_crypto[ticker]
                    previous_ticker_price = previous_ticker_prices_crypto.get(ticker, None)

                    if previous_ticker_price is not None:
                        if (previous_ticker_price <= price <= current_ticker_price) or (previous_ticker_price >= price >= current_ticker_price):
                            # Remove alert from database
                            db.collection("price_alerts").document(alert_id).delete()

                            # Get user email
                            user = db.collection("users").document(user_id).get()
                            user_email = firestore_unsafe(user.to_dict()["email"])

                            # Send email to user
                            print(send_email_smtp(user_email, f"Price Alert For {ticker}", f"Your price alert for the crypto {ticker} set at ${price} has been triggered. The price is now ${current_ticker_price}."))

            previous_ticker_prices_stocks = current_ticker_prices_stocks
            previous_ticker_prices_crypto = current_ticker_prices_crypto

            time.sleep(300)
        except:
            time.sleep(300)
