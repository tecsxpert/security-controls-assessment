from __future__ import annotations

from flask import Blueprint, jsonify

from services.cache import cache_health
from services.groq_client import groq_health
from services.rag import vector_store_health

health_bp = Blueprint("health", __name__)


@health_bp.route("/health", methods=["GET"])
def health():
    dependencies = {
        "groq": groq_health(),
        "redis": cache_health(),
        "chromadb": vector_store_health(),
    }
    acceptable = {"ok", "fallback-ok"}
    overall_status = "ok" if all(value.get("status") in acceptable for value in dependencies.values()) else "error"
    return jsonify(
        {
            "status": overall_status,
            "service": "security-controls-ai",
            "dependencies": dependencies,
        }
    ), 200 if overall_status == "ok" else 503
