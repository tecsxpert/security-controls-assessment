from __future__ import annotations

import json
from datetime import datetime, timezone
from typing import Any, Generator

from services.cache import cache_key, get_cached, set_cached
from services.errors import DependencyUnavailableError
from services.groq_client import client
from services.pii import audit_pii
from services.prompts import load_prompt
from services.rag import search_context, store_document


def describe_control(control: str) -> dict[str, Any]:
    fallback = {
        "description": f"{control} is a security control that should be assessed for design and operating effectiveness.",
        "objectives": ["Reduce risk", "Support compliance", "Improve control assurance"],
        "evidence_examples": ["Policy requirement", "Configuration screenshot", "Review or approval record"],
        "testing_guidance": "Inspect the control design, sample recent evidence, and verify exceptions are tracked.",
        "metadata": {"is_fallback": True},
    }
    return _cached_json("describe", {"control": control}, "describe_prompt.txt", fallback, control=control)


def categorise_control(text: str) -> dict[str, Any]:
    category = _local_category(text)
    fallback = {
        "category": category,
        "confidence": 0.45,
        "reasoning": "Fallback classification based on security assessment context.",
        "metadata": {"is_fallback": True},
    }
    return _cached_json("categorise", {"text": text}, "categorise_prompt.txt", fallback, input=text)


def recommend_controls(finding: str, context: dict[str, Any] | None = None) -> dict[str, Any]:
    context = context or {}
    related = search_context(finding, limit=3)
    fallback = {
        "recommendations": [
            {"title": "Define control owner", "priority": "High", "action": "Assign accountable owner and review cadence."},
            {"title": "Evidence retention", "priority": "Medium", "action": "Store audit evidence with timestamps and approvers."},
            {"title": "Continuous monitoring", "priority": "Medium", "action": "Track exceptions and overdue remediation in dashboards."},
        ],
        "metadata": {"is_fallback": True, "context_matches": len(related)},
    }
    return _cached_json(
        "recommend",
        {"finding": finding, "context": context},
        "recommend_prompt.txt",
        fallback,
        finding=finding,
        context=json.dumps(context, indent=2),
        retrieved_context=json.dumps(related, indent=2),
    )


def generate_report(assessment: dict[str, Any], stream: bool = False):
    if stream:
        return _generate_report_events(assessment)

    key = cache_key("generate_report", assessment)
    cached = get_cached(key)
    if cached:
        return cached

    prompt = load_prompt("report_prompt.txt", assessment=json.dumps(assessment, indent=2))
    fallback = {
        "executive_summary": "Assessment completed with fallback report generation.",
        "risk_rating": "Medium",
        "findings": [],
        "recommendations": [],
        "metadata": {"is_fallback": True},
    }
    result = client.chat_json([{"role": "user", "content": prompt}], fallback=fallback, max_tokens=2500)
    result["metadata"] = _metadata(result.get("metadata", {}))
    set_cached(key, result, ttl_seconds=1800)
    return result


def _generate_report_events(assessment: dict[str, Any]) -> Generator[dict[str, Any], None, None]:
    yield {"event": "started", "timestamp": _now()}
    yield {"event": "analysing_controls", "count": len(assessment.get("controls", [])) if isinstance(assessment.get("controls"), list) else 0}
    report = generate_report(assessment, stream=False)
    yield {"event": "completed", "report": report, "timestamp": _now()}


def analyse_document(document: str, metadata: dict[str, Any] | None = None) -> dict[str, Any]:
    pii = audit_pii(document)
    stored = store_document(document, metadata=metadata)
    fallback = {
        "summary": "Document was indexed for security control retrieval.",
        "key_controls": [],
        "risks": [],
        "metadata": {"is_fallback": True},
    }
    result = _cached_json(
        "analyse_document",
        {"document": document, "metadata": metadata},
        "analyse_document_prompt.txt",
        fallback,
        document=document[:6000],
        metadata=json.dumps(metadata or {}, indent=2),
    )
    result["metadata"] = _metadata({**result.get("metadata", {}), "pii_audit": pii, "rag": stored})
    return result


def answer_query(question: str) -> dict[str, Any]:
    contexts = search_context(question, limit=5)
    fallback = {
        "answer": "No authoritative answer is available from the local knowledge base.",
        "sources": contexts,
        "metadata": {"is_fallback": True},
    }
    return _cached_json(
        "query",
        {"question": question, "contexts": contexts},
        "query_prompt.txt",
        fallback,
        question=question,
        context=json.dumps(contexts, indent=2),
    )


def _cached_json(namespace: str, payload: dict[str, Any], prompt_name: str, fallback: dict[str, Any], **prompt_values: Any) -> dict[str, Any]:
    key = cache_key(namespace, payload)
    cached = get_cached(key)
    if cached:
        return cached

    prompt = load_prompt(prompt_name, **prompt_values)
    result = client.chat_json([{"role": "user", "content": prompt}], fallback=fallback)
    result["metadata"] = _metadata(result.get("metadata", {}))
    set_cached(key, result)
    return result


def _metadata(extra: dict[str, Any] | None = None) -> dict[str, Any]:
    return {"generated_at": _now(), **(extra or {})}


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _local_category(text: str) -> str:
    lowered = text.lower()
    if any(term in lowered for term in ["approval", "evidence", "policy", "pci", "audit", "compliance"]):
        return "Compliance Issue"
    if any(term in lowered for term in ["failed", "capacity", "outage", "job", "latency"]):
        return "Operational Issue"
    return "Security Risk"
