from __future__ import annotations


class DependencyUnavailableError(RuntimeError):
    def __init__(self, dependency: str, details: str) -> None:
        super().__init__(details)
        self.dependency = dependency
        self.details = details
