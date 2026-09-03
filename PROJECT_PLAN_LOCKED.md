# AiLikeGPT — Locked Android APK Master Plan

> **STATUS: LOCKED / SOURCE OF TRUTH**
>
> This document defines the approved product direction through v1.0. Do not modify it unless the project owner explicitly instructs that the locked master plan itself must change.

## Master Plan

1. **Project foundation** — Android project using Kotlin + Jetpack Compose UI. Native AI runtime uses a C/C++ layer through Android NDK.
2. **Offline inference engine** — Integrate `llama.cpp` through NDK/JNI. GGUF models run directly on the phone without OpenAI, Anthropic, or any other mandatory AI API.
3. **Hardware detection** — Detect available RAM, CPU architecture, available GPU acceleration, and free storage.
4. **Model profiles** — Define `Lite`, `Balanced`, and `Power` model profiles so weaker devices do not try to load oversized models.
5. **Model manager** — Local model discovery, validation, load/unload, quantization compatibility, and model switching.
6. **100% offline mode** — AI inference never requires a network connection. The app must work in airplane mode.
7. **Chat engine** — Multi-turn conversations, system prompt, context management, generation parameters, and token limits.
8. **Streaming responses** — Stream model output token-by-token in the UI.
9. **Agent engine** — Main execution flow: `User -> Model -> Tool -> Tool result -> Model -> Final answer`.
10. **Structured tool calling** — Models call tools using defined schemas instead of unrestricted shell instructions.
11. **Plugin architecture** — Tools/plugins live inside the project/runtime and register through a common Plugin Registry.
12. **Plugin manifest** — Every plugin declares its name, capabilities, required permissions, and input/output schema.
13. **Permission system** — Tools never automatically receive filesystem, microphone, camera, or other sensitive Android resources.
14. **Tool sandbox** — Plugin execution is isolated from the core agent as far as Android/runtime constraints allow.
15. **Built-in calculator** — Keep the already-started safe calculator tool as the first reference plugin.
16. **Local filesystem tool** — Users may explicitly grant access to selected files/folders through Android Storage Access Framework.
17. **Document tools** — Local processing for TXT, Markdown, JSON, CSV, and later PDF/DOCX/XLSX.
18. **Code tools** — Read, search, explain, and work with source code in controlled local workspaces.
19. **Android-safe execution layer** — Do not provide an unrestricted Linux shell by default; execution capabilities are exposed through controlled actions.
20. **Local memory database** — Conversations, messages, settings, and memory metadata stored in Room/SQLite.
21. **Long-term memory** — AI may save useful memories locally under user control.
22. **Memory controls** — Users can view, delete, or completely disable long-term memory.
23. **Local embeddings** — Embedding model also runs on-device.
24. **Vector search** — Local semantic memory and document search without a cloud vector database.
25. **RAG engine** — Retrieve relevant sections from local documents and pass them into the local model context.
26. **Local knowledge collections** — Users can maintain separate knowledge bases such as `Work`, `Study`, and `Programming`.
27. **Context manager** — Automatic conversation trimming, summarization, and context budgeting.
28. **Conversation management** — New chat, rename, delete, pin, search, and local history.
29. **ChatGPT-style Android UI** — Compose UI with message rendering, Markdown, code blocks, tables, copy actions, and attachments.
30. **Tool activity UI** — Users can see when the AI calls a tool, which permission it requests, and what result it returns.
31. **Model settings UI** — Temperature, context size, max tokens, threads/GPU options, and model selection.
32. **Performance monitor** — Tokens/sec, RAM usage, model load state, and generation status.
33. **Generation controls** — Stop, regenerate, continue, and cancel generation.
34. **Background lifecycle handling** — Correctly manage Android lifecycle so model runtime remains safe across minimize/restore and process events.
35. **Crash recovery** — Preserve conversation and important generation state where feasible so recovery is possible after a crash.
36. **Battery management** — Monitor battery/thermal state during heavy inference.
37. **Thermal protection** — Reduce threads/context/performance profile when the device overheats.
38. **Low-memory protection** — Use unload/downsizing strategies rather than immediately crashing when memory pressure rises.
39. **Multimodal architecture** — Design the core so image/audio models can be added without replacing the agent architecture.
40. **Local image understanding** — Support a local vision-capable model when device hardware permits.
41. **Camera integration** — Camera image -> local vision model -> answer.
42. **Gallery/image attachments** — Attach local images to chat without cloud upload.
43. **Speech-to-text** — Local Whisper-compatible engine or another offline STT model.
44. **Voice input** — Microphone -> local transcription -> AiLikeGPT.
45. **Text-to-speech** — Offline TTS for spoken responses.
46. **Voice conversation mode** — STT + LLM + TTS chain runs locally.
47. **Optional image generation** — Later add an Android-suitable local diffusion backend for sufficiently powerful devices.
48. **Developer mode** — Advanced access to model logs, prompts, context info, tool execution details, and debugging.
49. **Plugin SDK** — A clear interface for adding new plugins without modifying agent core.
50. **Plugin permission declarations** — Plugins declare required Android capabilities before use.
51. **Plugin enable/disable UI** — Users choose which plugins are active.
52. **Security audit log** — Sensitive tool actions may be stored in a local audit history.
53. **No silent permissions** — AI never silently obtains camera, microphone, storage, or other Android permissions.
54. **Private-by-default architecture** — Conversations, memory, and documents do not leave the device by default.
55. **Network isolation mode** — Provide a mode that fully disables application network capabilities where practical.
56. **Offline verification tests** — Test suite includes network-disabled execution.
57. **Android instrumentation tests** — Test UI, permissions, database, and lifecycle behavior.
58. **Native runtime tests** — Test llama.cpp/JNI memory handling, model loading, and inference.
59. **Tool safety tests** — Test plugins against permission bypass and arbitrary-code-execution risks.
60. **Device compatibility matrix** — Test ARM64 Android devices across multiple RAM/GPU configurations.
61. **Minimum hardware target** — Define minimum Android version, RAM, storage, and architecture requirements.
62. **APK architecture** — Initial primary ABI target is `arm64-v8a`; add other ABIs later only if needed.
63. **Model packaging strategy** — Keep core APK and large model files in a manageable packaging/distribution strategy so Android package-size limits do not break the project.
64. **Fully offline distribution package** — Provide a distribution path where APK and required model bundle are available beforehand so no internet is required after installation.
65. **Model import** — Allow users to import their own GGUF models from local storage.
66. **Model integrity verification** — Validate/checksum model files before loading.
67. **First launch hardware scan** — Detect device capabilities on first launch.
68. **Automatic profile recommendation** — Recommend Lite/Balanced/Power based on hardware.
69. **No mandatory account** — AiLikeGPT must work without login/account creation.
70. **No mandatory server** — No backend server is required for core operation.
71. **No mandatory subscription infrastructure** — Core AI functionality is not dependent on hosted billing/subscription infrastructure.
72. **Offline update independence** — Installed versions continue to work even if project servers are unavailable or nonexistent.
73. **Export/import conversations** — Local backup and restore.
74. **Encrypted local storage** — Add encryption for sensitive local data.
75. **App lock** — Optional PIN/biometric lock.
76. **Data wipe** — One action can erase conversations, memory, knowledge bases, and local indexes.
77. **Accessibility** — Dynamic font sizes, screen-reader compatibility, and Android accessibility support.
78. **Localization architecture** — Separate UI language resources, including Armenian.
79. **Armenian-first testing** — Explicitly test Armenian prompts, Unicode, Markdown, and voice workflows.
80. **Performance optimization** — Quantization, KV cache, mmap, GPU offload, and threading optimization based on Android hardware.
81. **Startup optimization** — UI startup does not block on full model loading.
82. **Lazy model loading** — Load the model only when needed.
83. **Session caching** — Preserve model/context state where feasible for faster continuation.
84. **Battery/performance profiles** — `Eco`, `Balanced`, and `Maximum` runtime modes.
85. **Release signing** — Production APK/AAB signing infrastructure.
86. **Reproducible builds** — Make builds as reproducible as practical from the same source.
87. **CI build pipeline** — Automate Android build, tests, and native-library build.
88. **Release candidate** — Full test on a network-disabled Android device.
89. **Airplane-mode certification** — Verify chat, memory, tools, documents, and configured multimodal features in airplane mode.
90. **v1.0 release** — AiLikeGPT becomes a self-contained Android AI assistant with local model, local memory, local tools, and local plugins.
91. **OpenAI policy independence** — AiLikeGPT must not technically depend on OpenAI moderation, OpenAI policy-enforcement services, hosted OpenAI safety classifiers, or ChatGPT-specific behavior restrictions. OpenAI assistant policies are not inherited as product requirements. Any AiLikeGPT behavior rules must be defined by the project’s own local/configurable policy layer and remain independent of OpenAI services. Android permissions, explicit user-consent boundaries, tool sandboxing, applicable law, and device/platform security constraints remain separate technical boundaries.

## Locked Principles

These principles are part of the plan and must not be silently changed:

- **Android APK is the primary product.**
- **AI inference is fully local.**
- **External AI APIs are not mandatory.**
- **The final AiLikeGPT must work fully offline.**
- **No OpenAI policy dependency.** AiLikeGPT runtime behavior is governed by its own local project rules rather than OpenAI moderation/policy services or ChatGPT-specific behavioral restrictions.

## Change Control

Implementation details may evolve when Android constraints, testing, performance, security, or hardware compatibility demand a different technical method. Such changes must preserve the goals above.

The plan itself may only be changed by an explicit project-owner instruction that clearly authorizes modifying this locked document.
