
from stock_api_access import StockAPIAccess
from function_library.security_string_parsing import firestore_safe


def api_get_top_stocks(req_args):

    # Check if limit is in request arguments
    if "limit" not in req_args:
        limit = 10
    else:
        # Get limit from request arguments
        limit = str(req_args.get("limit"))

        # Make sure limit is an integer
        if not limit.isnumeric():
            return {"status": "Limit must be an integer."}

        # Make limit safe for Firestore
        limit = firestore_safe(limit)

        try:
            limit = int(limit)
        except ValueError:
            return {"status": "Limit must be an integer."}

    # Get top stocks limited
    top_stocks = ["AAPL", "MSFT", "AMZN", "GOOGL", "TSLA", "META", "NVDA", "AMD", "INTC", "IBM"]
    selected_stocks = top_stocks[:limit]

    # Get client
    client = StockAPIAccess.get_client()

    # Get top stocks
    top_stocks = []
    for ss in selected_stocks:
        stock = client.get_previous_close_agg(ss)
        details = client.get_ticker_details(ss)
        top_stocks.append({"symbol": ss,
                           "company_name": details.name,
                           "price": stock[0].close,
                           "currency": details.currency_name})

    return {"stocks": top_stocks, "status": "Success"}
