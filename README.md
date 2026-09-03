# AiLikeGPT

AiLikeGPT is an **offline-first AI assistant** designed to run a local language model, memory, tools, and plugins on the user's own machine.

The core runtime does not require OpenAI, Anthropic, Google, or any other cloud AI API.

## Goals

- Local GGUF model inference through `llama.cpp` bindings.
- Project-local tools and plugins.
- Local SQLite conversation memory.
- No mandatory network access at runtime.
- Cross-platform Python core as the first implementation.
- Clear permission boundaries for tools that touch files or the operating system.

## Current MVP architecture

```text
AiLikeGPT/
├── config/
│   └── default.toml
├── models/
│   └── README.md
├── src/ailikegpt/
│   ├── cli.py
│   ├── core/
│   │   ├── agent.py
│   │   ├── config.py
│   │   ├── memory.py
│   │   └── model.py
│   └── plugins/
│       ├── base.py
│       ├── registry.py
│       └── builtin/
│           └── calculator.py
├── .gitignore
└── pyproject.toml
```

## Runtime flow

1. The CLI loads the local configuration.
2. `LocalModel` opens a GGUF model from `models/`.
3. `Agent` sends the conversation and tool catalog directly to the local model.
4. The model either returns a final answer or requests a local tool.
5. The tool executes locally and its result is returned to the model.
6. Messages are saved to a local SQLite database.

## Quick start

Requirements:

- Python 3.11+
- A local GGUF instruct/chat model
- A working `llama-cpp-python` installation for your hardware

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -e .
```

Place a GGUF model under `models/`, then update `config/default.toml`.

```bash
ailikegpt
```

## What “offline” means

After dependencies and a model are present locally, the AiLikeGPT runtime is intended to work without an internet connection. Model inference, tool execution, memory, and orchestration stay on-device.

The model binary is intentionally not committed to Git because GGUF files are usually very large. A future packaged desktop distribution can bundle the selected model and dependencies for a true one-install offline experience.

## Roadmap

- [x] Offline-first project skeleton
- [x] Local GGUF inference adapter
- [x] Local tool registry
- [x] SQLite chat memory
- [x] CLI assistant
- [ ] Streaming responses
- [ ] Desktop UI
- [ ] Plugin permission manifests
- [ ] Sandboxed plugin execution
- [ ] Local embeddings and semantic memory
- [ ] Voice input/output
- [ ] Multimodal local models
- [ ] Fully bundled offline installer

## License

Not selected yet.
