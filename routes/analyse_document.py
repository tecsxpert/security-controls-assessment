from __future__ import annotations

from flask import Blueprint, jsonify, request

from services.ai_tasks import analyse_document
from services.validation import require_json_fields

analyse_document_bp = Blueprint("analyse_document", __name__)


@analyse_document_bp.route("/analyse-document", methods=["POST"])
def analyse_document_route():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"document": str})
    if error:
        return jsonify(error), 400

    metadata = payload.get("metadata", {})
    if metadata and not isinstance(metadata, dict):
        return jsonify({"error": "Validation failed", "details": {"metadata": "must be an object"}}), 400

    return jsonify(analyse_document(payload["document"], metadata=metadata))
