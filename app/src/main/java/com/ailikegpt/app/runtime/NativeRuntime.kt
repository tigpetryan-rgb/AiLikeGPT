package com.ailikegpt.app.runtime

data class NativeRuntimeStatus(
    val loaded: Boolean,
    val version: String,
    val backend: String,
    val error: String? = null,
)

sealed interface ModelLoadResult {
    data object Success : ModelLoadResult
    data class Failure(val message: String) : ModelLoadResult
}

data class GenerationConfig(
    val maxTokens: Int = 512,
    val temperature: Float = 0.8f,
    val minP: Float = 0.05f,
    val seed: Int = -1,
)

sealed interface GenerationResult {
    data object Completed : GenerationResult
    data object Cancelled : GenerationResult
    data class Failure(val message: String) : GenerationResult
}

object NativeRuntime {
    private const val CANCELLED_MARKER = "__AILIKEGPT_CANCELLED__"

    private val loadFailure: Throwable? = runCatching {
        System.loadLibrary("ailikegpt_native")
    }.exceptionOrNull()

    private fun interface NativeByteStream {
        fun onBytes(bytes: ByteArray)
    }

    fun status(): NativeRuntimeStatus {
        loadFailure?.let { failure ->
            return NativeRuntimeStatus(
                loaded = false,
                version = "unavailable",
                backend = "native library not loaded",
                error = failure.message ?: failure::class.java.simpleName,
            )
        }

        return runCatching {
            NativeRuntimeStatus(
                loaded = true,
                version = nativeVersion(),
                backend = nativeBackendStatus(),
            )
        }.getOrElse { failure ->
            NativeRuntimeStatus(
                loaded = false,
                version = "unavailable",
                backend = "JNI call failed",
                error = failure.message ?: failure::class.java.simpleName,
            )
        }
    }

    fun loadModel(
        absolutePath: String,
        contextSize: Int = 4096,
        threads: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
    ): ModelLoadResult {
        loadFailure?.let { failure ->
            return ModelLoadResult.Failure(
                failure.message ?: "native runtime is unavailable",
            )
        }

        if (absolutePath.isBlank()) {
            return ModelLoadResult.Failure("model path is blank")
        }

        return runCatching {
            val error = nativeLoadModel(
                modelPath = absolutePath,
                contextSize = contextSize.coerceAtLeast(512),
                threads = threads.coerceAtLeast(1),
            )
            if (error.isEmpty()) {
                ModelLoadResult.Success
            } else {
                ModelLoadResult.Failure(error)
            }
        }.getOrElse { failure ->
            ModelLoadResult.Failure(
                failure.message ?: "native model load failed",
            )
        }
    }

    fun generateChat(
        userPrompt: String,
        systemPrompt: String = "",
        config: GenerationConfig = GenerationConfig(),
        onToken: (String) -> Unit,
    ): GenerationResult {
        loadFailure?.let { failure ->
            return GenerationResult.Failure(
                failure.message ?: "native runtime is unavailable",
            )
        }

        if (userPrompt.isBlank()) {
            return GenerationResult.Failure("user prompt is blank")
        }
        if (!isModelLoaded()) {
            return GenerationResult.Failure("no model is loaded")
        }

        val decoder = Utf8IncrementalDecoder(onToken)
        var callbackFailure: Throwable? = null
        val callback = NativeByteStream { bytes ->
            if (callbackFailure == null) {
                runCatching {
                    decoder.push(bytes)
                }.onFailure { failure ->
                    callbackFailure = failure
                    cancelGeneration()
                }
            }
        }

        val nativeResult = runCatching {
            nativeGenerateChat(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = config.maxTokens.coerceIn(1, 8192),
                temperature = config.temperature.coerceIn(0f, 5f),
                minP = config.minP.coerceIn(0f, 1f),
                seed = config.seed,
                callback = callback,
            )
        }.getOrElse { failure ->
            return GenerationResult.Failure(
                failure.message ?: "native generation call failed",
            )
        }

        if (callbackFailure == null) {
            runCatching {
                decoder.finish()
            }.onFailure { failure ->
                callbackFailure = failure
            }
        }

        callbackFailure?.let { failure ->
            return GenerationResult.Failure(
                failure.message ?: "stream callback failed",
            )
        }

        return when (nativeResult) {
            "" -> GenerationResult.Completed
            CANCELLED_MARKER -> GenerationResult.Cancelled
            else -> GenerationResult.Failure(nativeResult)
        }
    }

    fun cancelGeneration() {
        if (loadFailure == null) {
            runCatching { nativeCancelGeneration() }
        }
    }

    fun isGenerating(): Boolean {
        if (loadFailure != null) return false
        return runCatching { nativeIsGenerating() }.getOrDefault(false)
    }

    fun unloadModel() {
        if (loadFailure == null) {
            runCatching { nativeUnloadModel() }
        }
    }

    fun isModelLoaded(): Boolean {
        if (loadFailure != null) return false
        return runCatching { nativeIsModelLoaded() }.getOrDefault(false)
    }

    private external fun nativeVersion(): String
    private external fun nativeBackendStatus(): String
    private external fun nativeLoadModel(
        modelPath: String,
        contextSize: Int,
        threads: Int,
    ): String
    private external fun nativeGenerateChat(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int,
        temperature: Float,
        minP: Float,
        seed: Int,
        callback: NativeByteStream,
    ): String
    private external fun nativeCancelGeneration()
    private external fun nativeIsGenerating(): Boolean
    private external fun nativeUnloadModel()
    private external fun nativeIsModelLoaded(): Boolean
}
