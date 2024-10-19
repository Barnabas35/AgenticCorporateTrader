
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


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=80)
