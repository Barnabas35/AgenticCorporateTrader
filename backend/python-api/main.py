
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
def username():
    from query_library.get_username import q_get_username
    return jsonify(q_get_username(request.json)), 200


# Get email
@app.route("/get-email", methods=["POST"])
def email():
    from query_library.get_email import q_get_email
    return jsonify(q_get_email(request.json)), 200


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=80)
