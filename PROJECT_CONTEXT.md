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
8. The owner requested that the plan be stored as an immutable/read-first project artifact for future chats and that project information be archived in Google Drive beginning with the initial chat.
9. On 2026-09-03 the owner explicitly authorized a locked-plan change adding OpenAI policy independence: AiLikeGPT must not technically depend on OpenAI moderation/policy-enforcement services or inherit ChatGPT-specific behavioral restrictions as product requirements. Project behavior rules are local/project-controlled and independent of OpenAI services, while Android permissions, explicit user consent, sandboxing, applicable law, and platform/device security remain separate technical boundaries.
10. On 2026-09-03 the owner established strict separation between persistent technical discussion, one-plan-point production execution, and failure investigation/repair routing.
11. On 2026-09-04 the owner added a fourth role: **Preparation chat**. One Preparation chat designs exactly one future Locked Master Plan point without production-code implementation, then creates a production-ready Work Package in `Production Work Packages / Staging`. This allows future work to be fully designed while the current CI gate is externally blocked without accumulating unverified source code.

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

The Python prototype is **not the final product architecture**. It is a behavior/reference prototype while implementation migrates toward Android/Kotlin/NDK.

## Android implementation progress

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

### Local inference generation foundation implemented

- Pinned upstream `ggml-org/llama.cpp` revision `de8656bd94f1163188125542534e4bcbc9f9fb1f`.
- Pin is stored in `third_party/llama.cpp.lock`.
- Added `scripts/sync-llama.sh` for deterministic bootstrap of that revision.
- Native CMake links the local llama.cpp tree when synchronized.
- JNI/Kotlin model lifecycle supports local GGUF load, context creation, loaded-state query, and unload/free lifecycle.
- Chat generation applies the model chat template when available, performs local tokenization/prefill/sampling, streams token bytes through JNI, and supports cancellation.
- Kotlin incremental UTF-8 decoding protects split multibyte characters.
- JVM tests cover Armenian/emoji byte streaming and incomplete trailing UTF-8.
- Current generation is intentionally single-request/stateless at the context level; persistent multi-turn context belongs to later plan work.
- Initial target remains decoder-style GGUF chat LLMs.

### Compose chat and local model import foundation implemented

- Local model store under private app storage.
- Android Storage Access Framework `OpenDocument` import flow; no broad storage permission.
- `.gguf` extension and `GGUF` magic validation.
- Bounded copy, storage headroom, SHA-256 during import, sanitized unique names and cleanup on failure.
- Compose model manager can import, list, load and switch local models and display status/fingerprint.
- Compose offline chat provides input, background local generation, streamed assistant updates, generation state and Stop/cancel.
- Import/generation run off the main UI thread.
- Android manifest still has no `INTERNET` permission.

## Current CI / production gate

Locked Master Plan point **87 — CI build pipeline** remains the implementation gate.

The GitHub-hosted runner failure was diagnosed to an account-level GitHub Actions usage restriction after included private-repository Actions minutes were exhausted. The repository-side Android CI workflow is prepared, but no executing hosted runner has yet produced a real Android build/test result. Therefore the Android application must **not** be described as build-verified.

The WP-087 production/repair packages remain Blocked until GitHub-hosted execution is restored or another explicitly approved execution route is used. No later feature Production package is authorized merely because WP-087 is blocked.

However, the owner explicitly authorized **Preparation work while this gate is blocked**: future Locked Plan points may be completely designed in separate one-point Preparation chats and their production-ready Work Packages may be queued under `Production Work Packages / Staging`. This preparation must not implement production source code.

## Four-chat operating model

The repository uses `PRODUCTION_WORKFLOW.md`, `PREPARATION_WORKFLOW.md`, and Google Drive packages.

### Discussion chat

- Persistent technical/project-control chat.
- Chooses sequencing and the next point to prepare.
- Creates/activates one Preparation assignment.
- Decides when a staged production package may be promoted to Active.
- Gives one short kickoff sentence for the specialized chat.

### Preparation chat

- Exactly one future Locked Master Plan point.
- Reads the assigned `Preparation / Active` package and relevant repository state.
- Resolves architecture, file-by-file changes, data/state, APIs/contracts, UI flows, migrations, tests, risks and boundaries as applicable.
- Does **not** implement production application/native/workflow code and does not make implementation commits.
- Creates the production-ready Work Package under `Production Work Packages / Staging`.
- Moves its Preparation package to `Preparation / Completed` after verified staging.

### Production chat

- Exactly one assigned Locked Plan point.
- Reads the Active Work Package in full.
- Implements, debugs, tests, repairs and verifies until the package Definition of Done is satisfied.
- Source/compiler/test/build/integration failures are work to fix, not blockers.
- Only a proven external restriction outside repository/tool control permits an incomplete ending.
- Stops after the assigned point; never begins a second point.

### Failure Investigation chat

- Exactly one concrete failure.
- Diagnoses and gathers evidence; does not implement the fix.
- Writes root cause/evidence into its Failure package.
- Automatically creates or updates the same-plan-point production repair package and verifies routing.
- Moves the Failure package to Resolved and gives a one-line Production launch sentence.

## Drive structure

`AiLikeGPT / Project Information / Production Work Packages`

Main production lifecycle:

- `Staging` — production-ready packages prepared in advance, not yet authorized to execute.
- `Active` — exactly the production package currently authorized to execute.
- `Completed` — finished and verified production packages.
- `Blocked` — externally blocked production packages.
- `Templates` — reusable production package structure.

Preparation lifecycle:

- `Preparation / Active`
- `Preparation / Completed`
- `Preparation / Blocked`
- `Preparation / Templates`
- `Preparation Chat Protocol — One Plan Point, No Production Code`

Failure lifecycle:

- `Failures / Active`
- `Failures / Resolved`
- `Failures / Templates`
- Failure Investigation Protocol

## Repository read-first protocol

Every new project chat should:

1. Read `AGENTS.md`.
2. Read `PROJECT_PLAN_LOCKED.md` in full.
3. Read this `PROJECT_CONTEXT.md`.
4. Identify its role before acting.
5. Preparation chat: read `PRODUCTION_WORKFLOW.md`, `PREPARATION_WORKFLOW.md`, the Drive Preparation Protocol and assigned Preparation package.
6. Production chat: read `PRODUCTION_WORKFLOW.md` and the assigned Active Work Package.
7. Failure Investigation chat: read `PRODUCTION_WORKFLOW.md`, the Failure Protocol, assigned Failure package and related production package.
8. Inspect current repository state relevant to its assignment.
9. Respect role boundaries and the one-plan-point/one-failure rule.

## Google Drive knowledge/archive structure

The project owner requested a Drive folder named `AiLikeGPT` with:

- `Plan/` — authoritative plan copies and plan-related documents.
- `Project Information/` — project history, decisions, chat-derived information, implementation notes, and accumulated project knowledge.
- `Project Information/Production Work Packages/` — Preparation, Production and Failure packages, protocols, staging, completion evidence and templates.

Future project documentation should preserve this separation: stable plan in `Plan`, evolving knowledge in `Project Information`, preparation in `Production Work Packages / Preparation`, staged execution packages in `Staging`, and only currently authorized implementation in `Active`.
