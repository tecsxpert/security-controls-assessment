from __future__ import annotations

import json
import sys
import time
from pathlib import Path
from typing import Any

import requests

ROOT = Path(__file__).resolve().parent.parent
DATASET = ROOT / "evaluations" / "prompt_eval_dataset.json"
RESULTS = ROOT / "evaluations" / "prompt_eval_results.json"


def main() -> int:
    base_url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:5000"
    dataset = json.loads(DATASET.read_text(encoding="utf-8"))
    results: list[dict[str, Any]] = []

    for case in dataset:
        start = time.perf_counter()
        try:
            response = requests.post(f"{base_url}{case['endpoint']}", json=case["payload"], timeout=90)
            elapsed_ms = round((time.perf_counter() - start) * 1000, 2)
            body = response.json()
            passed = response.ok and _matches(case, body)
            results.append(
                {
                    "id": case["id"],
                    "endpoint": case["endpoint"],
                    "status_code": response.status_code,
                    "latency_ms": elapsed_ms,
                    "passed": passed,
                    "response": body,
                }
            )
        except Exception as exc:
            results.append({"id": case["id"], "endpoint": case["endpoint"], "passed": False, "error": str(exc)})

    passed_count = sum(1 for result in results if result["passed"])
    summary = {
        "total": len(results),
        "passed": passed_count,
        "accuracy": round(passed_count / len(results), 4) if results else 0.0,
        "results": results,
    }
    RESULTS.write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if passed_count == len(results) else 1


def _matches(case: dict[str, Any], body: dict[str, Any]) -> bool:
    for key, value in case.get("expected", {}).items():
        if body.get(key) != value:
            return False
    for key in case.get("expected_keys", []):
        if key not in body:
            return False
    if case["endpoint"] == "/recommend" and len(body.get("recommendations", [])) != 3:
        return False
    return True


if __name__ == "__main__":
    raise SystemExit(main())
