from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import tomllib


DEFAULT_SYSTEM_PROMPT = "You are AiLikeGPT, a private offline AI assistant."


@dataclass(slots=True)
class ModelConfig:
    path: Path
    context_size: int = 8192
    threads: int = 0
    gpu_layers: int = 0
    temperature: float = 0.7
    max_tokens: int = 1024
    chat_format: str = ""


@dataclass(slots=True)
class MemoryConfig:
    database: Path
    conversation_id: str = "default"
    max_history_messages: int = 30


@dataclass(slots=True)
class AgentConfig:
    max_tool_rounds: int = 6
    system_prompt: str = DEFAULT_SYSTEM_PROMPT


@dataclass(slots=True)
class AppConfig:
    model: ModelConfig
    memory: MemoryConfig
    agent: AgentConfig


def load_config(path: str | Path = "config/default.toml") -> AppConfig:
    config_path = Path(path)
    with config_path.open("rb") as file:
        raw = tomllib.load(file)

    model = raw.get("model", {})
    memory = raw.get("memory", {})
    agent = raw.get("agent", {})

    return AppConfig(
        model=ModelConfig(
            path=Path(model.get("path", "models/model.gguf")),
            context_size=int(model.get("context_size", 8192)),
            threads=int(model.get("threads", 0)),
            gpu_layers=int(model.get("gpu_layers", 0)),
            temperature=float(model.get("temperature", 0.7)),
            max_tokens=int(model.get("max_tokens", 1024)),
            chat_format=str(model.get("chat_format", "")),
        ),
        memory=MemoryConfig(
            database=Path(memory.get("database", "data/ailikegpt.sqlite3")),
            conversation_id=str(memory.get("conversation_id", "default")),
            max_history_messages=int(memory.get("max_history_messages", 30)),
        ),
        agent=AgentConfig(
            max_tool_rounds=int(agent.get("max_tool_rounds", 6)),
            system_prompt=str(agent.get("system_prompt", DEFAULT_SYSTEM_PROMPT)),
        ),
    )
