from __future__ import annotations

import json
import logging
import os
import time
from dataclasses import dataclass
from typing import Any, Iterable

from dotenv import load_dotenv

try:
    from groq import Groq
except Exception:  # pragma: no cover - dependency may be absent during local compile
    Groq = None

load_dotenv()

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class GroqResult:
    content: str
    model: str
    is_fallback: bool = False


class GroqClient:
    def __init__(self) -> None:
        self.api_key = os.getenv("GROQ_API_KEY", "")
        self.model = os.getenv("GROQ_MODEL", "llama-3.3-70b-versatile")
        self.timeout = float(os.getenv("GROQ_TIMEOUT_SECONDS", "20"))
        self.max_retries = int(os.getenv("GROQ_MAX_RETRIES", "3"))
        self._client = Groq(api_key=self.api_key, timeout=self.timeout) if Groq and self.api_key else None

    def is_configured(self) -> bool:
        return bool(Groq and self.api_key and self._client)

    def is_local_fallback_enabled(self) -> bool:
        lowered = self.api_key.strip().lower()
        return lowered in {"placeholder-local-testing-key", "placeholder", "test", "local"}

    def chat_json(
        self,
        messages: Iterable[dict[str, str]],
        *,
        fallback: dict[str, Any],
        temperature: float = 0.2,
        max_tokens: int = 1200,
    ) -> dict[str, Any]:
        result = self.chat_text(messages, fallback=json.dumps(fallback), temperature=temperature, max_tokens=max_tokens)
        try:
            parsed = json.loads(_extract_json(result.content))
            if isinstance(parsed, dict):
                parsed.setdefault("metadata", {})
                parsed["metadata"].update({"model": result.model, "is_fallback": result.is_fallback})
                return parsed
        except json.JSONDecodeError:
            logger.warning("Groq returned non-JSON content")

        raise RuntimeError("Groq returned invalid JSON and fallback responses are disabled")

    def chat_text(
        self,
        messages: Iterable[dict[str, str]],
        *,
        fallback: str,
        temperature: float = 0.2,
        max_tokens: int = 1200,
    ) -> GroqResult:
        if self.is_local_fallback_enabled():
            return GroqResult(content=fallback, model="local-testing-fallback", is_fallback=True)
        if not self._client:
            raise RuntimeError("GROQ_API_KEY is not configured or groq package is unavailable")

        last_error: Exception | None = None
        for attempt in range(1, self.max_retries + 1):
            try:
                response = self._client.chat.completions.create(
                    model=self.model,
                    messages=list(messages),
                    temperature=temperature,
                    max_tokens=max_tokens,
                )
                content = response.choices[0].message.content or ""
                if not content.strip():
                    raise ValueError("empty Groq response")
                return GroqResult(content=content.strip(), model=self.model)
            except Exception as exc:
                last_error = exc
                logger.warning("Groq request failed on attempt %s/%s: %s", attempt, self.max_retries, exc)
                time.sleep(min(2**attempt, 8))

        logger.error("Groq unavailable after retries: %s", last_error)
        raise RuntimeError(f"Groq unavailable after retries: {last_error}")

    def test_connection(self) -> dict[str, Any]:
        if self.is_local_fallback_enabled():
            return {"status": "fallback-ok", "model": "local-testing-fallback", "reason": "placeholder GROQ_API_KEY configured"}
        if not self._client:
            return {"status": "error", "reason": "GROQ_API_KEY is not configured"}
        result = self.chat_text(
            [{"role": "user", "content": "Return the word ok."}],
            fallback="unavailable",
            max_tokens=8,
        )
        return {"status": "ok", "model": result.model}


def _extract_json(content: str) -> str:
    stripped = content.strip()
    if stripped.startswith("```"):
        stripped = stripped.strip("`")
        stripped = stripped.removeprefix("json").strip()
    start = stripped.find("{")
    end = stripped.rfind("}")
    if start >= 0 and end >= start:
        return stripped[start : end + 1]
    return stripped


client = GroqClient()


def groq_health() -> dict[str, Any]:
    return client.test_connection()


def call_groq(prompt: str) -> str:
    return client.chat_text([{"role": "user", "content": prompt}], fallback="{}").content
