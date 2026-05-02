from __future__ import annotations

import hashlib
import json
import os
from typing import Any

from dotenv import load_dotenv

load_dotenv()

try:
    import redis
except Exception:  # pragma: no cover
    redis = None

from services.errors import DependencyUnavailableError

_redis_client = None

if redis and os.getenv("REDIS_URL"):
    try:
        _redis_client = redis.from_url(os.getenv("REDIS_URL", ""), decode_responses=True)
        _redis_client.ping()
    except Exception:
        _redis_client = None


def get_redis_client():
    if _redis_client is None:
        raise DependencyUnavailableError("redis", "REDIS_URL is not configured or Redis is unreachable")
    return _redis_client


def cache_key(namespace: str, payload: Any) -> str:
    raw = json.dumps(payload, sort_keys=True, default=str)
    return f"{namespace}:{hashlib.sha256(raw.encode('utf-8')).hexdigest()}"


def get_cached(key: str) -> Any | None:
    value = get_redis_client().get(key)
    return json.loads(value) if value else None


def set_cached(key: str, value: Any, ttl_seconds: int = 900) -> None:
    get_redis_client().setex(key, ttl_seconds, json.dumps(value, default=str))


def cache_health() -> dict[str, str]:
    try:
        get_redis_client().set("health:redis", "ok", ex=60)
        value = get_redis_client().get("health:redis")
        return {"status": "ok", "mode": "redis", "set_get": "ok" if value == "ok" else "failed"}
    except Exception as exc:
        return {"status": "error", "mode": "redis", "reason": str(exc)}
