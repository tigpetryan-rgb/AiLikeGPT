# AiLikeGPT — Project Context

This file records the project state and decisions so a new development chat can resume without reconstructing the history from memory.

## Project identity

- Project: **AiLikeGPT**
- GitHub repository: `tigpetryan-rgb/AiLikeGPT`
- Repository visibility: private
- Default branch: `main`
- Product target: Android APK
- Core product goal: a ChatGPT-like AI assistant whose model, memory, tools, plugins, and orchestration can run locally/offline without requiring an external AI API.

## Genesis / decisions from the initial project chat

1. The GitHub account was connected and the repository `tigpetryan-rgb/AiLikeGPT` was located.
2. The owner defined the project as creating an AI assistant similar in capability/experience to ChatGPT, but with backend model/tools/plugins contained in the project/runtime and with no mandatory AI API requirement.
3. The core requirement was strengthened to **fully offline AI operation**.
4. An initial MVP scaffold was created in the repository with a Python local-inference prototype.
5. The owner requested a fixed plan through completion.
6. The original desktop target was explicitly changed to an **Android APK** target.
7. A new Android-focused master plan was approved and is now stored in `PROJECT_PLAN_LOCKED.md`.
8. The owner requested that the plan be stored as an immutable/read-first project artifact for future chats and that project information be archived in Google Drive beginning with this chat.

## Locked product direction

The source of truth is `PROJECT_PLAN_LOCKED.md`. The critical invariants are:

- Android APK is the primary product.
- AI inference is local/on-device.
- No external AI API is mandatory.
- Final operation must be possible fully offline.
- No mandatory backend server or account.
- User data is private-by-default and local-first.
- Tools/plugins use explicit permissions and safe boundaries.

## Target technical architecture

- Kotlin + Jetpack Compose UI.
- C/C++ native inference runtime through Android NDK/JNI.
- `llama.cpp` + local GGUF models.
- Device-aware Lite/Balanced/Power profiles.
- Room/SQLite for local conversation and memory persistence.
- Local embeddings/vector search for semantic memory/RAG.
- Manifest-driven local plugin/tool system with Android permission controls.
- Storage Access Framework for user-approved file/folder access.
- Future local multimodal capabilities: vision, STT, TTS, and optional image generation.

## Existing prototype already committed

Before the Android target was locked, a small offline Python reference prototype was created. It currently includes concepts/files for:

- Local GGUF inference adapter using `llama-cpp-python`.
- Agent loop: model -> tool -> model -> final response.
- SQLite conversation memory.
- Plugin registry and plugin contract.
- Safe calculator reference tool.
- CLI entrypoint.
- Configuration under `config/default.toml`.
- Basic calculator safety/unit test.

The Python prototype is **not the final product architecture**. It should be treated as a behavior/reference prototype while the implementation migrates toward Android/Kotlin/NDK.

## Repository read-first protocol

Every new coding agent/chat should:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read this `PROJECT_CONTEXT.md`.
4. Inspect current code and recent changes.
5. Continue the next unfinished plan phase without redefining the product direction.

## Google Drive knowledge/archive structure

The project owner requested a Drive folder named `AiLikeGPT` with:

- `Plan/` — authoritative plan copies and plan-related documents.
- `Project Information/` — project history, decisions, chat-derived information, implementation notes, and future accumulated project knowledge beginning with the initial project chat.

Future project documentation should preserve this separation: stable plan in `Plan`, evolving project knowledge in `Project Information`.
