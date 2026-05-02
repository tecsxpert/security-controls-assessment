from __future__ import annotations

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from app import create_app
from services.cache import get_redis_client
from services.groq_client import client as groq_client
from services.rag import PERSIST_DIR, search_context, store_document, vector_store_health


def main() -> int:
    app = create_app({"TESTING": True})
    flask_client = app.test_client()
    health = flask_client.get("/health")
    print("HEALTH", health.status_code, json.dumps(health.get_json(), indent=2))

    redis_ok = False
    try:
        redis_client = get_redis_client()
        redis_client.set("verification:redis", "ok", ex=60)
        redis_ok = redis_client.get("verification:redis") == "ok"
        print("REDIS_PROOF", {"set_get": redis_ok})
    except Exception as exc:
        print("REDIS_PROOF", {"set_get": False, "error": str(exc)})

    chroma_health = vector_store_health()
    chroma_store = store_document("Verification document for persistent ChromaDB storage.", {"source": "verify"})
    chroma_search = search_context("persistent ChromaDB storage", 1)
    print("CHROMADB_PROOF", json.dumps({"health": chroma_health, "store": chroma_store, "search": chroma_search, "path_exists": PERSIST_DIR.exists()}, indent=2))

    groq_ok = False
    try:
        groq_response = groq_client.chat_text(
            [{"role": "user", "content": "Return exactly: ok"}],
            fallback="ok",
            max_tokens=8,
        )
        groq_ok = "ok" in groq_response.content.lower()
        mode = "fallback-ok" if groq_response.is_fallback else "ok"
        print("GROQ_PROOF", {"ok": groq_ok, "status": mode, "model": groq_response.model, "content": groq_response.content})
    except Exception as exc:
        print("GROQ_PROOF", {"ok": False, "error": str(exc)})

    all_ok = health.status_code == 200 and redis_ok and chroma_health.get("status") == "ok" and groq_ok
    return 0 if all_ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
