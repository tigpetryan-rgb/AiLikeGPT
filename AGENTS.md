# AiLikeGPT — READ FIRST

This file is the first source of instructions for every coding agent, assistant, or new development chat working on this repository.

## Mandatory first step

Before proposing, planning, or changing code:

1. Read `PROJECT_PLAN_LOCKED.md` in full.
2. Read `PROJECT_CONTEXT.md` for the current project state and prior decisions.
3. Read `CHAT_WORKFLOW.md` and identify the chat role before changing project state.
4. Read `PRODUCTION_WORKFLOW.md` if this is a Preparation, Production/execution, or Failure Investigation chat.
5. Inspect the existing repository before making changes.
6. If this is a Preparation chat, read `PREPARATION_WORKFLOW.md` and the assigned Google Drive Preparation package in full before designing the point.
7. If this is a Production chat, read the assigned Google Drive Work Package in full before implementation.
8. If this is a Failure Investigation chat, read the assigned Google Drive Failure package and Failure Investigation Protocol before diagnosis.

`PROJECT_PLAN_LOCKED.md` is the authoritative master plan.

## Online bootstrap rule

Before role work, inspect the project's GitHub and Google Drive workflow structure. If the folders/protocols/templates required by the assigned role are missing, create only the missing pieces before continuing.

Bootstrap must be idempotent: do not create duplicate folders/documents, do not silently overwrite an existing authoritative protocol, and do not change the locked plan unless the project owner explicitly authorizes it.

## Locked product definition

AiLikeGPT is an **Android APK-first, fully offline AI assistant**.

The following principles are non-negotiable unless the project owner explicitly changes the locked master plan:

- Android APK is the primary product target.
- AI inference runs locally on-device.
- No external AI API is mandatory for core functionality.
- The final app must work without internet access, including in airplane mode / air-gapped conditions after installation and model availability.
- No mandatory backend server.
- No mandatory user account.
- Conversations, memory, documents, indexes, and tool activity are private-by-default and local-first.
- Models, tools, plugins, memory, orchestration, and core runtime belong to the product/runtime itself rather than depending on a hosted AI service.
- AiLikeGPT does not technically depend on OpenAI moderation/policy-enforcement services or ChatGPT-specific behavior restrictions; project behavior rules belong to the project's own local/configurable policy layer.
- Tools and plugins must use explicit permissions and safe execution boundaries; local execution does not mean unlimited device access.

## Preparation-chat rule

A Preparation chat designs **exactly one** future Locked Master Plan point and stages its detailed Production Work Package without implementing production code.

It must inspect the current repository, resolve the architecture, file-by-file plan, data/API/UI/migration/test details and risks required by that point, then create the production-ready package under `Production Work Packages / Staging`. It must not modify application/native/workflow production code, make implementation commits, fix build failures, claim unexecuted tests passed, or start a second plan point.

Detailed preparation protocol: `PREPARATION_WORKFLOW.md` and the Google Drive Preparation Chat Protocol.

## Production-chat rule

A Production chat executes **exactly one** Locked Master Plan point from its assigned Google Drive Work Package, including a failure-derived repair package tied to that same point.

It may divide that point into any number of implementation phases, commits, build attempts, debugging loops, or tests, but it must not end merely because one intermediate phase is finished. It must continue until the Work Package Definition of Done is satisfied, then stop without beginning the next plan point.

The only allowed incomplete ending is a proven external blocker outside the repository and outside the connected tools available to the chat. Source-code failures, compiler failures, test failures, build failures, and integration bugs must be repaired within the same Production chat.

## Failure-Investigation rule

A Failure Investigation chat is **diagnosis and routing only**.

It must not implement the production fix or make a fix commit. It must identify the root cause with evidence, update its Failure package, and before ending **automatically create or update an Active production repair Work Package** tied to the same Locked Plan point. It must verify that repair package in Drive, move the Failure package to Resolved, and return one short launch sentence naming the exact Active repair package for the next Production chat.

A Failure Investigation chat that ends with only analysis, recommendations, or a handoff paragraph—but no Active production repair package—is incomplete.

Detailed operational protocol: `PRODUCTION_WORKFLOW.md` and the Google Drive Failure Investigation Protocol.

## Target architecture

- Android: Kotlin + Jetpack Compose.
- Native inference: `llama.cpp` through Android NDK/JNI.
- Models: local GGUF, with Lite/Balanced/Power device profiles.
- Memory: Room/SQLite plus optional local embeddings/vector search.
- Agent runtime: local model -> structured tool -> result -> local model -> final answer.
- Plugins: manifest-driven, permission-controlled, user-enableable.
- Storage access: Android Storage Access Framework and scoped permissions.
- Multimodal roadmap: local vision, local STT, local TTS, optional local image generation.

## Change-control rule

Do **not** modify, reinterpret, replace, or silently narrow the locked plan because an implementation detail is difficult. Technical implementation details may evolve when tests or Android constraints require it, but the product goals and locked principles must remain intact.

Only an explicit instruction from the project owner to change the locked master plan authorizes changing `PROJECT_PLAN_LOCKED.md` or these locked product principles.

## Existing prototype note

The repository began with a small Python/`llama-cpp-python` offline prototype. It is useful as a behavior/reference prototype, but the locked product target is now Android APK. New architecture work should migrate toward the Android/Kotlin/NDK target without losing validated offline-agent concepts.
