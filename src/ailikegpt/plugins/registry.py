from __future__ import annotations

import json
from typing import Any

from .base import ToolPlugin
from .builtin.calculator import CalculatorTool


class ToolRegistry:
    def __init__(self) -> None:
        self._tools: dict[str, ToolPlugin] = {}
        self.register(CalculatorTool())

    def register(self, tool: ToolPlugin) -> None:
        name = tool.spec.name
        if name in self._tools:
            raise ValueError(f"Tool already registered: {name}")
        self._tools[name] = tool

    def catalog(self) -> list[dict[str, Any]]:
        return [
            {
                "name": tool.spec.name,
                "description": tool.spec.description,
                "input_schema": tool.spec.input_schema,
            }
            for tool in self._tools.values()
        ]

    def catalog_json(self) -> str:
        return json.dumps(self.catalog(), ensure_ascii=False, indent=2)

    def execute(self, name: str, arguments: dict[str, Any]) -> str:
        tool = self._tools.get(name)
        if tool is None:
            return f"Tool error: unknown tool '{name}'."
        try:
            return tool.run(arguments)
        except Exception as exc:  # Tool failures are returned to the model, not fatal to the runtime.
            return f"Tool error: {type(exc).__name__}: {exc}"
