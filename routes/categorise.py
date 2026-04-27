from flask import Blueprint, request, jsonify
from services.groq_client import call_groq
import json

categorise_bp = Blueprint("categorise", __name__)

@categorise_bp.route("/categorise", methods=["POST"])
def categorise():
    data = request.json
    text = data.get("text")

    if not text:
        return jsonify({"error": "Text is required"}), 400

    # Load prompt
    with open("prompts/categorise_prompt.txt", "r") as f:
        template = f.read()

    prompt = template.replace("{input}", text)

    # 🔥 Call AI
    result = call_groq(prompt)

    # ✅ Convert string → JSON
    try:
        parsed = json.loads(result)
        return jsonify(parsed)
    except Exception as e:
        print("JSON ERROR:", e)
        return jsonify({
            "error": "Invalid AI response",
            "raw": result
        })