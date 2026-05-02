from __future__ import annotations

import os

try:
    from flask_limiter import Limiter
    from flask_limiter.util import get_remote_address
except Exception:  # pragma: no cover
    Limiter = None
    get_remote_address = None


def init_rate_limiter(app):
    if not Limiter:
        return None
    return Limiter(
        get_remote_address,
        app=app,
        default_limits=[os.getenv("RATE_LIMIT", "60 per minute")],
        storage_uri=os.getenv("RATE_LIMIT_STORAGE_URI", "memory://"),
    )
