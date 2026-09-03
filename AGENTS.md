# AiLikeGPT — READ FIRST

This file is the first source of instructions for every coding agent, assistant, or new development chat working on this repository.

## Mandatory first step

Before proposing, planning, or changing code:

1. Read `PROJECT_PLAN_LOCKED.md` in full.
2. Read `PROJECT_CONTEXT.md` for the current project state and prior decisions.
3. Inspect the existing repository before making changes.

`PROJECT_PLAN_LOCKED.md` is the authoritative master plan.

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
- Tools and plugins must use explicit permissions and safe execution boundaries; local execution does not mean unlimited device access.

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
