
# This python script is responsible for running the Flask application.
# Database queries are not handled in this script.

from flask import Flask, request, jsonify

app = Flask(__name__)


@app.route("/", methods=["GET"])
def index():
    return "This is the index page of this API."


@app.route("/random-number", methods=["GET"])
def random_number():
    from random import randint
    return jsonify({"random_number": randint(0, 1000)}), 200


if __name__ == "__main__":
    app.run(debug=True)
