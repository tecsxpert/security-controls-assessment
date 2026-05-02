from __future__ import annotations

import json

from flask import Blueprint, Response, jsonify, request, stream_with_context

from services.ai_tasks import generate_report
from services.cache import get_redis_client
from services.validation import require_json_fields

generate_report_bp = Blueprint("generate_report", __name__)


@generate_report_bp.route("/generate-report", methods=["POST"])
def generate_report_route():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"assessment": dict})
    if error:
        return jsonify(error), 400

    return jsonify(generate_report(payload["assessment"], stream=False))


@generate_report_bp.route("/generate-report/stream", methods=["POST"])
def generate_report_stream_route():
    payload = request.get_json(silent=True) or {}
    error = require_json_fields(payload, {"assessment": dict})
    if error:
        return jsonify(error), 400

    get_redis_client()

    @stream_with_context
    def event_stream():
        for event in generate_report(payload["assessment"], stream=True):
            yield f"data: {json.dumps(event)}\n\n"

    return Response(event_stream(), mimetype="text/event-stream")
