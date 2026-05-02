from __future__ import annotations

from flask import Blueprint, jsonify, request

from services.ai_tasks import describe_control
from services.validation import require_json_fields

describe_bp = Blueprint("describe", __name__)


@describe_bp.route("/describe", methods=["POST"])
def describe():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"control": str})
    if error:
        return jsonify(error), 400

    return jsonify(describe_control(payload["control"]))
