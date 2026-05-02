# Security Notes

This AI service handles security-control data and may process sensitive assessment evidence. Key threats and controls:

1. Prompt injection: request middleware blocks common instruction-override phrases and script payloads before prompts are built.
2. Sensitive data leakage: `/analyse-document` runs PII audit checks and returns structured metadata so callers can enforce retention or masking.
3. Abuse and denial of service: Flask-Limiter applies default request throttling and Flask caps request size with `MAX_CONTENT_LENGTH`.
4. Dependency outage: Groq, Redis, and ChromaDB integrations degrade to explicit fallback modes rather than crashing the API.
5. Retrieval poisoning: RAG metadata is preserved with every chunk so the backend can trace source documents and enforce trust boundaries.

OWASP validation checklist:

- Validate and trim JSON input before route handling.
- Return structured errors without stack traces.
- Keep API keys in environment variables only.
- Use CORS configuration through `CORS_ORIGINS`.
- Log service failures without logging raw secrets.
