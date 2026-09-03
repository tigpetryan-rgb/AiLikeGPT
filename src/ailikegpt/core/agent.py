from __future__ import annotations

import json
from typing import Any

from .config import AgentConfig
from .memory import ConversationMemory
from .model import LocalModel
from ..plugins.registry import ToolRegistry


class Agent:
    def __init__(
        self,
        model: LocalModel,
        memory: ConversationMemory,
        tools: ToolRegistry,
        config: AgentConfig,
    ) -> None:
        self._model = model
        self._memory = memory
        self._tools = tools
        self._config = config

    def _system_message(self) -> str:
        return f"""{self._config.system_prompt}

LOCAL TOOL CATALOG
{self._tools.catalog_json()}

TOOL PROTOCOL
When no tool is needed, answer with exactly one JSON object:
{{"type":"final","content":"your answer"}}

When a tool is needed, answer with exactly one JSON object:
{{"type":"tool","name":"tool_name","arguments":{{...}}}}

Do not wrap the JSON in Markdown fences. Do not invent tools. After receiving a LOCAL_TOOL_RESULT message, either request another tool or return a final answer using the same JSON protocol.
"""

    @staticmethod
    def _parse_action(text: str) -> dict[str, Any] | None:
        candidate = text.strip()
        try:
            action = json.loads(candidate)
        except json.JSONDecodeError:
            return None
        return action if isinstance(action, dict) else None

    def reply(self, user_text: str) -> str:
        self._memory.add("user", user_text)
        messages: list[dict[str, str]] = [
            {"role": "system", "content": self._system_message()},
            *self._memory.history(),
        ]

        for _ in range(self._config.max_tool_rounds + 1):
            raw = self._model.chat(messages)
            action = self._parse_action(raw)

            if action is None:
                self._memory.add("assistant", raw)
                return raw

            action_type = action.get("type")
            if action_type == "final":
                content = str(action.get("content", "")).strip()
                self._memory.add("assistant", content)
                return content

            if action_type != "tool":
                fallback = str(action.get("content") or raw).strip()
                self._memory.add("assistant", fallback)
                return fallback

            tool_name = str(action.get("name", "")).strip()
            arguments = action.get("arguments", {})
            if not isinstance(arguments, dict):
                arguments = {}

            tool_result = self._tools.execute(tool_name, arguments)
            messages.append({"role": "assistant", "content": raw})
            messages.append(
                {
                    "role": "user",
                    "content": (
                        f"LOCAL_TOOL_RESULT\n"
                        f"tool={tool_name}\n"
                        f"result={tool_result}\n"
                        "Continue using the TOOL PROTOCOL."
                    ),
                }
            )

        answer = "I reached the local tool-call limit before producing a final answer."
        self._memory.add("assistant", answer)
        return answer
