from __future__ import annotations

from pathlib import Path
from typing import Any

from llama_cpp import Llama

from .config import ModelConfig


class LocalModel:
    """Embedded local model adapter backed by llama.cpp Python bindings."""

    def __init__(self, config: ModelConfig) -> None:
        model_path = Path(config.path)
        if not model_path.exists():
            raise FileNotFoundError(
                f"Local model not found: {model_path}. Place a GGUF model there or update config/default.toml."
            )

        kwargs: dict[str, Any] = {
            "model_path": str(model_path),
            "n_ctx": config.context_size,
            "n_gpu_layers": config.gpu_layers,
            "verbose": False,
        }
        if config.threads > 0:
            kwargs["n_threads"] = config.threads
        if config.chat_format:
            kwargs["chat_format"] = config.chat_format

        self._llm = Llama(**kwargs)
        self._temperature = config.temperature
        self._max_tokens = config.max_tokens

    def chat(self, messages: list[dict[str, str]]) -> str:
        response = self._llm.create_chat_completion(
            messages=messages,
            temperature=self._temperature,
            max_tokens=self._max_tokens,
        )
        choice = response["choices"][0]
        message = choice["message"]
        content = message.get("content") or ""
        return str(content).strip()
