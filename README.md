# AiLikeGPT

AiLikeGPT is an **Android-first, fully offline AI assistant** designed to run its language model, memory, tools, plugins, and orchestration locally on the user's device.

The core product does **not** require OpenAI, Anthropic, Google, or any other cloud AI API to function.

> **For every new development chat/agent:** read `AGENTS.md`, then `PROJECT_PLAN_LOCKED.md`, then `PROJECT_CONTEXT.md` before changing the project.

## Locked product target

- Android APK is the primary product.
- Kotlin + Jetpack Compose for the application UI.
- Native local inference through Android NDK/JNI and `llama.cpp`.
- Local GGUF models with device-aware profiles.
- Local Room/SQLite memory and optional local embeddings/vector search.
- Local tools/plugins with explicit permission boundaries.
- No mandatory backend server, account, subscription infrastructure, or AI API.
- Final release must work in airplane mode / offline after installation and model availability.

The complete approved roadmap is in [`PROJECT_PLAN_LOCKED.md`](PROJECT_PLAN_LOCKED.md).

## Current repository state

The repository currently contains an early Python offline reference prototype that validated several core ideas before the Android target was locked:

```text
AiLikeGPT/
├── AGENTS.md
├── PROJECT_PLAN_LOCKED.md
├── PROJECT_CONTEXT.md
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
├── tests/
└── pyproject.toml
```

The Python prototype is a **behavior/reference implementation**, not the final platform architecture. Development now proceeds toward the locked Android/Kotlin/NDK architecture while preserving validated offline-agent concepts.

## Core runtime concept

```text
User
  -> Local model
  -> Structured local tool/plugin (when needed)
  -> Tool result
  -> Local model
  -> Final answer
```

Conversation data and project knowledge remain local by default.

## Offline definition

The v1.0 goal is not merely "works after calling a local server." The final application must be capable of running its core AI functions without internet access or a mandatory remote service. Distribution/model packaging will be designed so an offline installation bundle can be prepared in advance.

## License

Not selected yet.
