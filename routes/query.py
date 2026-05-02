from __future__ import annotations

from flask import Blueprint, jsonify, request

from services.ai_tasks import answer_query
from services.validation import require_json_fields

query_bp = Blueprint("query", __name__)


@query_bp.route("/query", methods=["POST"])
def query():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"question": str})
    if error:
        return jsonify(error), 400

    result = answer_query(payload["question"])
    return jsonify(result)
