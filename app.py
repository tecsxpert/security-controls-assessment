from flask import Flask
from routes.categorise import categorise_bp

app = Flask(__name__)

app.register_blueprint(categorise_bp)

if __name__ == "__main__":
    app.run(port=5000, debug=True)