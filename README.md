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

## Current Android implementation

Android migration is now active in the repository.

Implemented foundation:

- Android application module with Kotlin + Jetpack Compose.
- Android Gradle Plugin 9.4.0, Kotlin 2.4.10, Compose BOM 2026.08.00, JDK 17.
- `compileSdk`/`targetSdk` 37 and initial `arm64-v8a` ABI target.
- Android NDK 29 and CMake/JNI native bridge.
- No `INTERNET` permission in the application manifest.
- Device capability detection for RAM, available storage, CPU cores, ABI, OpenGL ES, and Vulkan hardware level.
- Initial `Lite`, `Balanced`, and `Power` device profile recommendation policy with unit tests.
- `llama.cpp` revision pinned in `third_party/llama.cpp.lock`.
- Reproducible development sync script at `scripts/sync-llama.sh`.
- Native runtime links `llama` and `llama-common` automatically when the pinned source tree is present.
- JNI/Kotlin model lifecycle API for local GGUF `load`, context creation, loaded-state query, and `unload`.
- GitHub Actions Android CI definition for unit tests and debug APK builds.

The next inference milestone is tokenization/prefill/decode plus streaming generation over the existing native model context.

## Repository shape

```text
AiLikeGPT/
├── AGENTS.md
├── PROJECT_PLAN_LOCKED.md
├── PROJECT_CONTEXT.md
├── app/                          # Android product implementation
│   └── src/main/
│       ├── java/com/ailikegpt/app/
│       └── cpp/                  # JNI/native inference bridge
├── gradle/
│   └── libs.versions.toml
├── scripts/
│   └── sync-llama.sh
├── third_party/
│   └── llama.cpp.lock            # exact upstream revision pin
├── src/ailikegpt/                # earlier Python behavior/reference prototype
├── config/
├── models/
└── tests/
```

The Python prototype remains a **behavior/reference implementation**, not the final platform architecture.

## Native dependency bootstrap

The repository pins the exact `llama.cpp` revision but does not duplicate the large upstream source tree in normal commits.

For a development checkout with network access during setup:

```bash
bash scripts/sync-llama.sh
```

After the dependency and build toolchain are present locally, model inference is designed to run on-device without a network connection. The final offline distribution path will package all required runtime components ahead of time.

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

Project license not selected yet. The pinned `llama.cpp` dependency is MIT-licensed upstream.
