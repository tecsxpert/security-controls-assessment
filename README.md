# Security Controls Assessment AI Service

Flask AI service for describing, categorising, recommending, analysing, and reporting on security controls. It integrates with Groq for generation, ChromaDB for retrieval, Redis for cache, and a Spring Boot backend through `integration/java/AIServiceClient.java`.

## Run AI Service

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements.txt
$env:GROQ_API_KEY="your-real-groq-key"
$env:REDIS_URL="redis://localhost:6379/0"
$env:CHROMA_PERSIST_DIR="./data/chroma"
python app.py
```

The AI service no longer returns synthetic fallback responses. Groq, Redis, and ChromaDB must be configured for full operation.

## Run Backend

```powershell
cd backend
mvn spring-boot:run
```

Backend proxy URLs:

- `GET http://localhost:8080/api/ai/health`
- `POST http://localhost:8080/api/ai/describe`
- `POST http://localhost:8080/api/ai/categorise`
- `POST http://localhost:8080/api/ai/recommend`
- `POST http://localhost:8080/api/ai/generate-report`

## Run Frontend

```powershell
cd frontend
npm install
npm run dev
```

Set `VITE_AI_API_BASE_URL=http://localhost:5000` if the AI service runs on a different URL.

## Endpoints

- `GET /health`
- `POST /describe`
- `POST /categorise`
- `POST /recommend`
- `POST /generate-report`
- `POST /generate-report/stream`
- `POST /analyse-document`
- `POST /query`

## Environment

- `GROQ_API_KEY`: Groq API key.
- `GROQ_MODEL`: defaults to `llama-3.3-70b-versatile`.
- `REDIS_URL`: optional Redis URL.
- `CHROMA_PERSIST_DIR`: defaults to `./data/chroma`.
- `RATE_LIMIT`: defaults to `60 per minute`.
- `CORS_ORIGINS`: defaults to `*`.

## Evaluation

```powershell
python scripts/evaluate_prompts.py http://localhost:5000
python scripts/benchmark_api.py http://localhost:5000
```

Local `.env` defaults:

```text
GROQ_API_KEY=placeholder-local-testing-key
REDIS_URL=redis://localhost:6379
CHROMA_PERSIST_DIR=./chroma_data
```
