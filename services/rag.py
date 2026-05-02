from __future__ import annotations

import hashlib
import os
import threading
import uuid
from pathlib import Path
from typing import Any

from dotenv import load_dotenv

load_dotenv()

try:
    import chromadb
except Exception:  # pragma: no cover
    chromadb = None

from services.errors import DependencyUnavailableError

COLLECTION_NAME = os.getenv("CHROMA_COLLECTION", "security_controls")
PERSIST_DIR = Path(os.getenv("CHROMA_PERSIST_DIR", "./data/chroma")).resolve()

_collection = None
_collection_lock = threading.Lock()


def _get_collection():
    global _collection
    with _collection_lock:
        if _collection is not None:
            return _collection
        if not chromadb:
            raise DependencyUnavailableError("chromadb", "chromadb package is not installed")
        PERSIST_DIR.mkdir(parents=True, exist_ok=True)
        client = chromadb.PersistentClient(path=str(PERSIST_DIR))
        _collection = client.get_or_create_collection(name=COLLECTION_NAME)
        return _collection


def chunk_text(text: str, chunk_size: int = 900, overlap: int = 120) -> list[str]:
    normalized = " ".join(text.split())
    if not normalized:
        return []
    chunks: list[str] = []
    start = 0
    while start < len(normalized):
        end = min(start + chunk_size, len(normalized))
        chunks.append(normalized[start:end])
        if end == len(normalized):
            break
        start = max(end - overlap, start + 1)
    return chunks


def embed_text(text: str, dimensions: int = 64) -> list[float]:
    digest = hashlib.sha256(text.encode("utf-8")).digest()
    values = list(digest) * ((dimensions // len(digest)) + 1)
    return [round((value / 255.0) * 2 - 1, 6) for value in values[:dimensions]]


def store_document(text: str, metadata: dict[str, Any] | None = None) -> dict[str, Any]:
    metadata = metadata or {}
    chunks = chunk_text(text)
    if not chunks:
        return {"stored_chunks": 0, "document_ids": []}

    ids = [str(uuid.uuid4()) for _ in chunks]
    embeddings = [embed_text(chunk) for chunk in chunks]
    metadatas = [{**metadata, "chunk_index": index} for index, _ in enumerate(chunks)]
    collection = _get_collection()
    collection.add(ids=ids, documents=chunks, embeddings=embeddings, metadatas=metadatas)
    return {"stored_chunks": len(chunks), "document_ids": ids}


def search_context(query: str, limit: int = 5) -> list[dict[str, Any]]:
    query_embedding = embed_text(query)
    collection = _get_collection()
    result = collection.query(query_embeddings=[query_embedding], n_results=limit)
    docs = result.get("documents", [[]])[0]
    metadatas = result.get("metadatas", [[]])[0]
    distances = result.get("distances", [[]])[0]
    return [
        {"text": doc, "metadata": metadata or {}, "distance": distance}
        for doc, metadata, distance in zip(docs, metadatas, distances)
    ]


def _distance(a: list[float], b: list[float]) -> float:
    return sum((left - right) ** 2 for left, right in zip(a, b))


def vector_store_health() -> dict[str, str]:
    try:
        _get_collection()
        return {"status": "ok", "mode": "chromadb", "path": str(PERSIST_DIR)}
    except Exception as exc:
        return {"status": "error", "mode": "chromadb", "reason": str(exc), "path": str(PERSIST_DIR)}
