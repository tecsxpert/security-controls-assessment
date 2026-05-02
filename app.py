from __future__ import annotations

import logging
import os
from typing import Any

from flask import Flask, jsonify, request
from flask_cors import CORS
from dotenv import load_dotenv

load_dotenv()

from routes.analyse_document import analyse_document_bp
from routes.categorise import categorise_bp
from routes.describe import describe_bp
from routes.generate_report import generate_report_bp
from routes.health import health_bp
from routes.query import query_bp
from routes.recommend import recommend_bp
from services.rate_limit import init_rate_limiter
from services.errors import DependencyUnavailableError
from services.security import SecurityValidationError, sanitize_payload


def create_app(config: dict[str, Any] | None = None) -> Flask:
    app = Flask(__name__)
    app.config.update(
        JSON_SORT_KEYS=False,
        MAX_CONTENT_LENGTH=int(os.getenv("MAX_CONTENT_LENGTH", "10485760")),
    )
    if config:
        app.config.update(config)

    CORS(app, resources={r"/*": {"origins": os.getenv("CORS_ORIGINS", "*")}})
    init_rate_limiter(app)
    _configure_logging()

    @app.before_request
    def validate_and_trim_request() -> None:
        if request.method in {"POST", "PUT", "PATCH"} and request.is_json:
            payload = request.get_json(silent=True)
            sanitize_payload(payload)

    @app.errorhandler(SecurityValidationError)
    def handle_security_error(error: SecurityValidationError):
        return jsonify({"error": "Invalid input", "details": str(error)}), 400

    @app.errorhandler(DependencyUnavailableError)
    def handle_dependency_error(error: DependencyUnavailableError):
        return jsonify({"error": "Dependency unavailable", "dependency": error.dependency, "details": error.details}), 503

    @app.errorhandler(RuntimeError)
    def handle_runtime_error(error: RuntimeError):
        return jsonify({"error": "Runtime error", "details": str(error)}), 503

    @app.errorhandler(400)
    def handle_bad_request(error):
        return jsonify({"error": "Bad request", "details": str(error)}), 400

    @app.errorhandler(404)
    def handle_not_found(error):
        return jsonify({"error": "Not found"}), 404

    @app.errorhandler(429)
    def handle_rate_limited(error):
        return jsonify({"error": "Rate limit exceeded"}), 429

    @app.errorhandler(500)
    def handle_server_error(error):
        app.logger.exception("Unhandled server error: %s", error)
        return jsonify({"error": "Internal server error"}), 500

    app.register_blueprint(health_bp)
    app.register_blueprint(describe_bp)
    app.register_blueprint(categorise_bp)
    app.register_blueprint(recommend_bp)
    app.register_blueprint(generate_report_bp)
    app.register_blueprint(analyse_document_bp)
    app.register_blueprint(query_bp)
    return app


def _configure_logging() -> None:
    level = os.getenv("LOG_LEVEL", "INFO").upper()
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
    )


app = create_app()


if __name__ == "__main__":
    app.run(
        host=os.getenv("FLASK_HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", "5000")),
        debug=os.getenv("FLASK_DEBUG", "false").lower() == "true",
    )
