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
9. On 2026-09-03 the owner explicitly authorized a locked-plan change adding OpenAI policy independence: AiLikeGPT must not technically depend on OpenAI moderation/policy-enforcement services or inherit ChatGPT-specific behavioral restrictions as product requirements. Project behavior rules are to be local/project-controlled and independent of OpenAI services, while Android permissions, explicit user consent, sandboxing, applicable law, and platform/device security remain separate technical boundaries.
10. On 2026-09-03 the owner established a two-chat operating model: one persistent technical discussion chat prepares work; each new production chat executes exactly one Locked Master Plan point from a prewritten Google Drive Work Package and must continue through all subphases until that point's Definition of Done is satisfied.

## Locked product direction

The source of truth is `PROJECT_PLAN_LOCKED.md`. The critical invariants are:

- Android APK is the primary product.
- AI inference is local/on-device.
- No external AI API is mandatory.
- Final operation must be possible fully offline.
- No mandatory backend server or account.
- User data is private-by-default and local-first.
- Tools/plugins use explicit permissions and safe boundaries.
- OpenAI moderation/policy services and ChatGPT-specific behavioral restrictions are not product dependencies or inherited requirements; AiLikeGPT uses its own local/project-controlled behavior rules.

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

### Compose chat and local model import foundation implemented

- Added a local model store under private app storage.
- The app uses Android Storage Access Framework `OpenDocument` flow rather than broad filesystem permissions.
- Imported files must use a `.gguf` filename and are checked for the `GGUF` file magic before being accepted.
- Import copies the model into private app storage using a bounded buffer and keeps storage headroom to reduce partial/out-of-space failures.
- SHA-256 is calculated during the copy so the imported model has a reproducible integrity fingerprint available for later model-integrity workflows.
- Existing locally imported GGUF files are discovered and listed on launch.
- Import names are sanitized and collisions create a new unique local filename instead of overwriting an existing model.
- The Compose UI now includes a local model manager card:
  - import GGUF from device
  - list discovered local models
  - load/switch a local model
  - show import/load status and the most recent SHA-256 fingerprint
- The Compose UI now includes an offline chat card:
  - message input
  - background local generation through `NativeRuntime.generateChat(...)`
  - assistant text updated as token bytes are decoded into text
  - visible generation state
  - Stop button wired to native cancellation
- Generation/model import work runs off the main UI thread; token text is posted back to the main thread for Compose state updates.
- Composition disposal requests native generation cancellation.
- The Android manifest still has no `INTERNET` permission, and the new import/chat flow does not add one.

### Next implementation milestone

- **Production package WP-087 is now the only active implementation assignment.** It must complete Locked Master Plan point 87: CI build pipeline.
- Obtain a real Android compile/test result and repair any source/API issues exposed by an executing build; do not skip this verification gate.
- Only after WP-087 is completed should the discussion chat prepare a new production package for the next selected plan point.
- Candidate later work includes expanded model validation/metadata, hardware-profile runtime defaults, and persistent multi-turn conversation storage, but none of those are active production assignments yet.

### CI state

- `.github/workflows/android-ci.yml` is defined for Android SDK/NDK/CMake setup, pinned llama.cpp sync, JVM unit tests, and debug APK assembly using Gradle 9.6.
- No workflow run was available through the GitHub connector for the latest native-streaming/model-import/Compose commits when this context was updated.
- Therefore the current code has been source-reviewed against the exact pinned upstream llama.cpp API, but the Android application must **not** be described as build-verified until CI executes successfully or a local Android build is performed.
- WP-087 owns diagnosis, repair, actual CI execution, native-library verification, and the first recorded green Android build.

## Production operating model

The repository now uses `PRODUCTION_WORKFLOW.md` plus Google Drive Work Packages.

### Discussion chat responsibilities

- Keep architectural and technical discussion in the persistent discussion chat.
- Choose exactly one next unfinished Locked Master Plan point.
- Prepare its complete Work Package before opening a production chat.
- Supply the user a short kickoff sentence that points the new chat to the Work Package.

### Production chat responsibilities

- Execute exactly one assigned plan point.
- Read the Drive Work Package in full.
- Continue through all required implementation phases, debugging, tests, repairs, and verification without ending at an intermediate milestone.
- Complete every objective Definition of Done item before marking the package complete.
- Update repository context and the Work Package with evidence.
- Stop after its assigned plan point; never begin the next plan point.

### Drive structure

`AiLikeGPT / Project Information / Production Work Packages`

- `Active` — package ready/currently assigned.
- `Completed` — finished and verified packages.
- `Blocked` — only packages with proven external blockers.
- `Templates` — reusable Work Package structure.

Current Active package: `WP-087 — Complete and Verify CI Build Pipeline`.

## Repository read-first protocol

Every new coding agent/chat should:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read this `PROJECT_CONTEXT.md`.
4. If this is a production chat, read `PRODUCTION_WORKFLOW.md`.
5. If this is a production chat, read the assigned Google Drive Work Package in full.
6. Inspect current code and recent changes.
7. Production chats execute only their assigned plan point; discussion chats prepare the next package without redefining product direction.

## Google Drive knowledge/archive structure

The project owner requested a Drive folder named `AiLikeGPT` with:

- `Plan/` — authoritative plan copies and plan-related documents.
- `Project Information/` — project history, decisions, chat-derived information, implementation notes, and future accumulated project knowledge beginning with the initial project chat.
- `Project Information/Production Work Packages/` — one-plan-point production assignments, their protocol, completion evidence, and templates.

Future project documentation should preserve this separation: stable plan in `Plan`, evolving project knowledge in `Project Information`, and executable one-point assignments in `Production Work Packages`.
