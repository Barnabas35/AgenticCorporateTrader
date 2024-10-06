
# This python script is responsible for running the Flask application.
# Database queries are not handled in this script.

from flask import Flask, request, jsonify

app = Flask(__name__)


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


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
