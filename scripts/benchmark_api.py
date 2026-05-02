from __future__ import annotations

import json
import statistics
import sys
import time
from pathlib import Path
from typing import Any

import requests

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "evaluations" / "performance_results.json"


REQUESTS = [
    ("/describe", {"control": "Privileged access requires MFA."}),
    ("/categorise", {"text": "Database backup encryption is missing."}),
    ("/recommend", {"finding": "No access review evidence exists.", "context": {"system": "ERP"}}),
    ("/generate-report", {"assessment": {"name": "Benchmark review", "controls": [{"id": "IAM-01", "status": "gap"}]}}),
]


def main() -> int:
    base_url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:5000"
    samples: list[dict[str, Any]] = []

    for path, payload in REQUESTS:
        first = _timed_post(base_url, path, payload)
        second = _timed_post(base_url, path, payload)
        samples.append(
            {
                "endpoint": path,
                "first_ms": first["latency_ms"],
                "second_ms": second["latency_ms"],
                "cache_speedup_ms": round(first["latency_ms"] - second["latency_ms"], 2),
                "first_status": first["status_code"],
                "second_status": second["status_code"],
            }
        )

    latencies = [item["second_ms"] for item in samples]
    summary = {
        "samples": samples,
        "p50_cached_ms": round(statistics.median(latencies), 2),
        "max_cached_ms": round(max(latencies), 2),
        "cache_improved_all": all(item["cache_speedup_ms"] >= 0 for item in samples),
    }
    RESULTS.write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if summary["cache_improved_all"] else 1


def _timed_post(base_url: str, path: str, payload: dict[str, Any]) -> dict[str, Any]:
    start = time.perf_counter()
    response = requests.post(f"{base_url}{path}", json=payload, timeout=120)
    latency_ms = round((time.perf_counter() - start) * 1000, 2)
    return {"status_code": response.status_code, "latency_ms": latency_ms}


if __name__ == "__main__":
    raise SystemExit(main())
