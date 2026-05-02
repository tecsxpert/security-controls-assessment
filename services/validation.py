from __future__ import annotations

from typing import Any


def require_json_fields(payload: dict[str, Any], schema: dict[str, type]) -> dict[str, Any] | None:
    errors: dict[str, str] = {}
    for field, expected_type in schema.items():
        value = payload.get(field)
        if value is None:
            errors[field] = "is required"
        elif not isinstance(value, expected_type):
            errors[field] = f"must be {expected_type.__name__}"
        elif isinstance(value, str) and not value.strip():
            errors[field] = "cannot be blank"

    if errors:
        return {"error": "Validation failed", "details": errors}
    return None
