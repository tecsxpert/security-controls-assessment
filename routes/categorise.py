from __future__ import annotations

from flask import Blueprint, jsonify, request

from services.ai_tasks import categorise_control
from services.validation import require_json_fields

categorise_bp = Blueprint("categorise", __name__)


@categorise_bp.route("/categorise", methods=["POST"])
def categorise():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"text": str})
    if error:
        return jsonify(error), 400

    result = categorise_control(payload["text"])
    return jsonify(result)
