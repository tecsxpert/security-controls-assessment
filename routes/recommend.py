from __future__ import annotations

from flask import Blueprint, jsonify, request

from services.ai_tasks import recommend_controls
from services.validation import require_json_fields

recommend_bp = Blueprint("recommend", __name__)


@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"finding": str})
    if error:
        return jsonify(error), 400

    context = payload.get("context", {})
    if context and not isinstance(context, dict):
        return jsonify({"error": "Validation failed", "details": {"context": "must be an object"}}), 400

    return jsonify(recommend_controls(payload["finding"], context=context))
