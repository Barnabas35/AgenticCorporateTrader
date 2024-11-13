
import yfinance as yf

def api_get_top_cryptos(request_args):

    # Check if limit is in request
    if "limit" not in request_args:
        limit = 10
    else:
        # Get limit from request arguments
        limit = str(request_args.get("limit"))

        # Make sure limit is an integer
        if not limit.isnumeric():
            return {"status": "Limit must be an integer."}

        try:
            limit = min(max(int(limit), 1), 10)
        except ValueError:
            return {"status": "Limit must be an integer."}

    # Top 10 cryptos
    cryptos = ["BTC-USD", "ETH-USD", "DOGE-USD", "USDT-USD", "BCH-USD", "LTC-USD", "LINK-USD", "XLM-USD", "ADA-USD", "XRP-USD"]

    crypto_details = []

    for i in range(limit):
        ticker = yf.Ticker(cryptos[i])
        ticker_info = ticker.info
        latest_price = ticker.history(period="1d", interval="1m").tail(1)['Close'].iloc[0]

        crypto_details.append({
            "symbol": ticker_info["symbol"][0:-4],
            "name": ticker_info["longName"][0:-4],
            "price": latest_price
        })

    return {"crypto_details": crypto_details, "status": "Success"}