
# This python script is responsible for running the Flask application.
# Database queries are not handled in this script.

from background_tasks.check_alerts import check_alerts
from flask import Flask, request, jsonify
from flask_cors import CORS
import threading

app = Flask(__name__)
CORS(app)


@app.route("/", methods=["GET"])
def index():
    return "This is the index page of this API."


# Get random number between 0 and 1000
@app.route("/random-number", methods=["GET"])
def random_number():
    from random import randint
    return jsonify({"random_number": randint(0, 1000)}), 200


# Get number of users
@app.route("/user-count", methods=["GET"])
def user_count():
    from query_library.user_count import q_user_count
    return jsonify({"user_count": q_user_count()}), 200


# Login - Get session token
@app.route("/login", methods=["POST"])
def login():
    from query_library.login import q_login
    return jsonify(q_login(request.json)), 200


# Register
@app.route("/register", methods=["POST"])
def register():
    from query_library.register import q_register
    return jsonify(q_register(request.json)), 200


# Get username
@app.route("/get-username", methods=["POST"])
def get_username():
    from query_library.get_username import q_get_username
    return jsonify(q_get_username(request.json)), 200


# Get email
@app.route("/get-email", methods=["POST"])
def get_email():
    from query_library.get_email import q_get_email
    return jsonify(q_get_email(request.json)), 200


# Get user profile icon
@app.route("/get-profile-icon", methods=["POST"])
def get_profile_icon():
    from query_library.get_profile_icon import q_get_profile_icon
    return jsonify(q_get_profile_icon(request.json)), 200


# Add new client
@app.route("/add-client", methods=["POST"])
def add_client():
    from query_library.add_client import q_add_client
    return jsonify(q_add_client(request.json)), 200


# Remove client
@app.route("/remove-client", methods=["POST"])
def remove_client():
    from query_library.remove_client import q_remove_client
    return jsonify(q_remove_client(request.json)), 200


# Get client list
@app.route("/get-client-list", methods=["POST"])
def get_client_list():
    from query_library.get_client_list import q_get_client_list
    return jsonify(q_get_client_list(request.json)), 200


# Submit support ticket
@app.route("/submit-support-ticket", methods=["POST"])
def submit_support_ticket():
    from query_library.submit_support_ticket import q_submit_support_ticket
    return jsonify(q_submit_support_ticket(request.json)), 200


# Get support ticket list
@app.route("/get-support-ticket-list", methods=["POST"])
def get_support_ticket_list():
    from query_library.get_support_ticket_list import q_get_support_ticket_list
    return jsonify(q_get_support_ticket_list(request.json)), 200


# Resolve support ticket
@app.route("/resolve-support-ticket", methods=["POST"])
def resolve_support_ticket():
    from query_library.resolve_support_ticket import q_resolve_support_ticket
    return jsonify(q_resolve_support_ticket(request.json)), 200


# Submit review
@app.route("/submit-review", methods=["POST"])
def submit_review():
    from query_library.submit_review import q_submit_review
    return jsonify(q_submit_review(request.json)), 200


# Get review list
@app.route("/get-review-list", methods=["POST"])
def get_review_list():
    from query_library.get_review_list import q_get_review_list
    return jsonify(q_get_review_list(request.json)), 200


# Get top stocks - Returns a list of stock symbols and their respective company names and prices (10 max)
@app.route("/get-top-stocks", methods=["GET"])
def get_top_stocks():
    from polygon_request_library.get_top_stocks import api_get_top_stocks
    return jsonify(api_get_top_stocks(request.args)), 200


# Quick search stock by text
@app.route("/text-search-stock", methods=["POST"])
def text_search_stock():
    from polygon_request_library.text_search_market import api_text_search_market
    return jsonify(api_text_search_market(request.json)), 200

  
# Get user type
@app.route("/get-user-type", methods=["POST"])
def get_user_type():
    from query_library.get_user_type import q_get_user_type
    return jsonify(q_get_user_type(request.json)), 200


# Delete user
@app.route("/delete-user", methods=["POST"])
def delete_user():
    from query_library.delete_user import q_delete_user
    return jsonify(q_delete_user(request.json)), 200


# All details of a ticker
@app.route("/get-ticker-info", methods=["POST"])
def get_stock_price():
    from polygon_request_library.get_ticker_info import api_get_ticker_info
    return jsonify(api_get_ticker_info(request.json)), 200


# Get ticker aggregates for time range
@app.route("/get-ticker-aggregates", methods=["POST"])
def get_stock_aggregates():
    from polygon_request_library.get_ticker_aggregates import api_get_ticker_aggregates
    return jsonify(api_get_ticker_aggregates(request.json)), 200


# Logout user
@app.route("/logout", methods=["POST"])
def logout():
    from query_library.logout import q_logout
    return jsonify(q_logout(request.json)), 200


# Search for crypto by text
@app.route("/text-search-crypto", methods=["POST"])
def text_search_crypto():
    from polygon_request_library.text_search_crypto import api_text_search_crypto
    return jsonify(api_text_search_crypto(request.json)), 200


# Get top cryptos
@app.route("/get-top-cryptos", methods=["GET"])
def get_top_cryptos():
    from yfinance_requests.get_top_cryptos import api_get_top_cryptos
    return jsonify(api_get_top_cryptos(request.args)), 200


# Get crypto info
@app.route("/get-crypto-info", methods=["POST"])
def get_crypto_info():
    from yfinance_requests.get_crypto_info import api_get_crypto_info
    return jsonify(api_get_crypto_info(request.json)), 200


# Get crypto aggregates for time range
@app.route("/get-crypto-aggregates", methods=["POST"])
def get_crypto_aggregates():
    from yfinance_requests.get_crypto_aggregates import api_get_crypto_aggregates
    return jsonify(api_get_crypto_aggregates(request.json)), 200


# Admin get user list
@app.route("/get-user-list", methods=["POST"])
def admin_get_user_list():
    from query_library.get_user_list import q_get_user_list
    return jsonify(q_get_user_list(request.json)), 200


# Admin delete user
@app.route("/admin-delete-user", methods=["POST"])
def admin_delete_user():
    from query_library.admin_delete_user import q_admin_delete_user
    return jsonify(q_admin_delete_user(request.json)), 200


# Get balance
@app.route("/get-balance", methods=["POST"])
def get_balance():
    from query_library.get_balance import q_get_balance
    return jsonify(q_get_balance(request.json)), 200


# Purchase asset
@app.route("/purchase-asset", methods=["POST"])
def purchase_asset():
    from query_library.purchase_asset import q_purchase_asset
    return jsonify(q_purchase_asset(request.json)), 200


# Sell asset
@app.route("/sell-asset", methods=["POST"])
def sell_asset():
    from query_library.sell_asset import q_sell_asset
    return jsonify(q_sell_asset(request.json)), 200


# Get user assets
@app.route("/get-user-assets", methods=["POST"])
def get_user_assets():
    from query_library.get_user_assets import q_get_user_assets
    return jsonify(q_get_user_assets(request.json)), 200


# Get asset
@app.route("/get-asset", methods=["POST"])
def get_asset():
    from query_library.get_asset import q_get_asset
    return jsonify(q_get_asset(request.json)), 200


# Add balance
@app.route("/add-balance", methods=["POST"])
def add_balance():
    from query_library.add_balance import q_add_balance
    return jsonify(q_add_balance(request.json)), 200


# Get asset transactions log
@app.route("/get-asset-transaction-log", methods=["POST"])
def get_asset_transactions_log():
    from query_library.get_asset_transaction_log import q_get_asset_transaction_log
    return jsonify(q_get_asset_transaction_log(request.json)), 200


# Get asset report
@app.route("/get-asset-report", methods=["POST"])
def get_asset_report():
    from query_library.get_asset_report import q_get_asset_report
    return jsonify(q_get_asset_report(request.json)), 200


# Exchange tokens
@app.route("/exchange-tokens", methods=["POST"])
def exchange_tokens():
    from query_library.exchange_tokens import q_exchange_tokens
    return jsonify(q_exchange_tokens(request.json)), 200


# Register with token
@app.route("/register-with-token", methods=["POST"])
def register_with_token():
    from query_library.register_with_token import q_register_with_token
    return jsonify(q_register_with_token(request.json)), 200


# Create price alert
@app.route("/create-price-alert", methods=["POST"])
def create_price_alert():
    from query_library.create_price_alert import q_create_price_alert
    return jsonify(q_create_price_alert(request.json)), 200


# Get price alerts
@app.route("/get-price-alerts", methods=["POST"])
def get_price_alerts():
    from query_library.get_price_alerts import q_get_price_alerts
    return jsonify(q_get_price_alerts(request.json)), 200


# Delete price alert
@app.route("/delete-price-alert", methods=["POST"])
def delete_price_alert():
    from query_library.delete_price_alert import q_delete_price_alert
    return jsonify(q_delete_price_alert(request.json)), 200


if __name__ == "__main__":

    # Start background task to check price alerts
    thread = threading.Thread(target=check_alerts, daemon=True)
    thread.start()

    # Run Flask app
    app.run(debug=True, host="0.0.0.0", port=80, use_reloader=False)
