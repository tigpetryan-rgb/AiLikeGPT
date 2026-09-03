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

## Android implementation progress — 2026-09-03

The Android migration is active and the repository has moved beyond the Python-only reference state.

### Phase 1 foundation implemented

- Added Gradle Android project structure with `app` module.
- Stack pinned to Android Gradle Plugin 9.4.0, Kotlin 2.4.10, Compose BOM 2026.08.00, JDK 17.
- Uses AGP 9 built-in Kotlin rather than the obsolete `kotlin-android` application pattern.
- `compileSdk = 37`, `targetSdk = 37`, initial `minSdk = 28`.
- Initial ABI target: `arm64-v8a`.
- Android NDK target: `29.0.14206865`.
- CMake/JNI native shared library target: `ailikegpt_native`.
- Compose foundation screen reports native runtime and device information.
- Android manifest intentionally requests no `INTERNET` permission.

### Hardware/profile layer implemented

- Reads total/available RAM.
- Reads available app storage.
- Reads CPU core count and supported ABIs.
- Reads OpenGL ES capability and reported Vulkan hardware level.
- Added initial `Lite`, `Balanced`, `Power` recommendation policy.
- Added unit tests for the profile policy.
- Profile thresholds are an initial heuristic and must later be tuned against real device/model benchmarks without changing the locked product goals.

### Phase 2 local inference integration — generation foundation implemented

- Pinned upstream `ggml-org/llama.cpp` revision:
  `de8656bd94f1163188125542534e4bcbc9f9fb1f`.
- Pin is stored in `third_party/llama.cpp.lock`.
- Added `scripts/sync-llama.sh` for deterministic development bootstrap of that revision.
- Local synced source tree is intentionally ignored by Git; final offline distribution will package required native/runtime assets separately.
- Native CMake detects the pinned source tree and links `llama` + `llama-common`; without it, the JNI foundation can still build as a stub.
- Native backend initialization loads available GGML backends and initializes llama.cpp.
- JNI/Kotlin model lifecycle supports local GGUF load, context creation, loaded-state query, and unload/free lifecycle.
- Added chat generation foundation over the loaded local model:
  - reads the GGUF/model chat template with `llama_model_chat_template`
  - applies chat formatting with `llama_chat_apply_template`, with a plain fallback when the model has no template
  - converts Kotlin/Java UTF-16 prompts to standard UTF-8 in native code instead of relying on JNI Modified UTF-8
  - tokenizes locally with the model vocabulary
  - resets the llama context for each generation while keeping the model loaded
  - performs prompt prefill in bounded chunks
  - samples using greedy mode at zero temperature or a `min-p -> temperature -> distribution` sampler chain otherwise
  - streams generated token bytes through JNI
  - exposes generation cancellation and generation-active state
- Added an incremental UTF-8 decoder in Kotlin so token boundaries cannot corrupt split multi-byte characters.
- Added JVM tests that deliberately stream Armenian text and emoji one byte at a time, plus an incomplete trailing UTF-8 case.
- Current generation foundation is intentionally single-request/stateless at the context level; multi-turn context retention/management remains part of the later chat-engine/context-manager work.
- Encoder-model generation is not supported by this chat generator yet; the initial target remains decoder-style GGUF chat LLMs.

### Next implementation milestone

- Connect `NativeRuntime.generateChat(...)` to the Compose chat UI on a background execution path.
- Add visible streaming assistant-message state plus Stop/cancel behavior.
- Begin model manager/import work so users can select a local GGUF through Android-safe storage flows instead of supplying an absolute path manually.
- Continue toward persistent multi-turn conversations without changing the locked offline architecture.

### CI state

- `.github/workflows/android-ci.yml` is defined for Android SDK/NDK/CMake setup, pinned llama.cpp sync, JVM unit tests, and debug APK assembly using Gradle 9.6.
- GitHub Actions previously ended before workflow steps executed, and no workflow run was available for the latest streaming-generation commit at the time this context was updated.
- Therefore the new native/Kotlin generation path is source-reviewed against the exact pinned upstream llama.cpp API but must **not** be described as build-verified until CI executes successfully or a local Android build is performed.

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
