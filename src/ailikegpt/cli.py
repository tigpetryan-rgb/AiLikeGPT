from __future__ import annotations

import argparse
import sys

from .core.agent import Agent
from .core.config import load_config
from .core.memory import ConversationMemory
from .core.model import LocalModel
from .plugins.registry import ToolRegistry


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="ailikegpt",
        description="Run AiLikeGPT completely on the local machine.",
    )
    parser.add_argument(
        "--config",
        default="config/default.toml",
        help="Path to the TOML configuration file.",
    )
    parser.add_argument(
        "--clear-memory",
        action="store_true",
        help="Clear the configured conversation history before starting.",
    )
    return parser


def main() -> None:
    args = build_parser().parse_args()

    try:
        config = load_config(args.config)
        memory = ConversationMemory(config.memory)
        if args.clear_memory:
            memory.clear()

        model = LocalModel(config.model)
        tools = ToolRegistry()
        agent = Agent(model=model, memory=memory, tools=tools, config=config.agent)
    except Exception as exc:
        print(f"Startup error: {exc}", file=sys.stderr)
        raise SystemExit(1) from exc

    print("AiLikeGPT offline runtime")
    print("Commands: /clear, /exit")

    while True:
        try:
            user_text = input("\nYou> ").strip()
        except (EOFError, KeyboardInterrupt):
            print()
            break

        if not user_text:
            continue
        if user_text in {"/exit", "/quit"}:
            break
        if user_text == "/clear":
            memory.clear()
            print("Memory cleared.")
            continue

        try:
            answer = agent.reply(user_text)
        except KeyboardInterrupt:
            print("\nGeneration cancelled.")
            continue
        except Exception as exc:
            print(f"Runtime error: {exc}", file=sys.stderr)
            continue

        print(f"AiLikeGPT> {answer}")


if __name__ == "__main__":
    main()
