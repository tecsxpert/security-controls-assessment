from __future__ import annotations

import re
from typing import Any


class SecurityValidationError(ValueError):
    pass


INJECTION_PATTERNS = [
    re.compile(r"ignore\s+(all\s+)?previous\s+instructions", re.I),
    re.compile(r"system\s*prompt", re.I),
    re.compile(r"<\s*script\b", re.I),
    re.compile(r"drop\s+table", re.I),
    re.compile(r"exfiltrate|credential\s+dump|reverse\s+shell", re.I),
]


def sanitize_payload(payload: Any) -> Any:
    if payload is None:
        return payload
    if isinstance(payload, dict):
        for key, value in list(payload.items()):
            payload[key] = sanitize_payload(value)
        return payload
    if isinstance(payload, list):
        for index, value in enumerate(payload):
            payload[index] = sanitize_payload(value)
        return payload
    if isinstance(payload, str):
        cleaned = payload.strip()
        for pattern in INJECTION_PATTERNS:
            if pattern.search(cleaned):
                raise SecurityValidationError("Potential prompt or command injection detected")
        return cleaned
    return payload
