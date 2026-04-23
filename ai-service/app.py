""" 
Flask entry point
Adding sanitization middleware
"""
from flask import Flask
from middleware.input_sanitize import register_sanitization_hooks

app = Flask(__name__)

# checks every incoming request body
# to be called before any routes are registered
register_sanitization_hooks(app)

# register routes below


@app.route('/health', methods=['GET'])
def health():
    return {"status": "ok", "service": "tool-53-ai-service"}, 200

 
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)