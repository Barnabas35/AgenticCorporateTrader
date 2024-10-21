
# This python script is responsible for running the Flask application.
# Database queries are not handled in this script.

from flask import Flask, request, jsonify
from flask_cors import CORS

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


# Quick search stock by text - Returns a list of stock symbols and their respective company names (5 max)
@app.route("/text-search-stock", methods=["POST"])
def text_search_stock():
    from polygon_request_library.text_search_stock import q_text_search_stock
    return jsonify(q_text_search_stock(request.json)), 200


# Get stock list by text search - Returns a list of stock symbols and their respective company names and prices (50 max)
@app.route("/get-stock-list", methods=["POST"])
def get_stock_list():
    from polygon_request_library.get_stock_list import q_get_stock_list
    return jsonify(q_get_stock_list(request.json)), 200


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=80)
