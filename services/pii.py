from __future__ import annotations

import re

PII_PATTERNS = {
    "email": re.compile(r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b", re.I),
    "phone": re.compile(r"\b(?:\+?\d{1,3}[-.\s]?)?(?:\(?\d{3}\)?[-.\s]?)\d{3}[-.\s]?\d{4}\b"),
    "ssn": re.compile(r"\b\d{3}-\d{2}-\d{4}\b"),
    "credit_card": re.compile(r"\b(?:\d[ -]*?){13,16}\b"),
}


def audit_pii(text: str) -> dict[str, object]:
    findings = {name: len(pattern.findall(text)) for name, pattern in PII_PATTERNS.items()}
    return {
        "contains_pii": any(count > 0 for count in findings.values()),
        "findings": {name: count for name, count in findings.items() if count > 0},
    }
