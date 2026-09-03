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

object NativeRuntime {
    private val loadFailure: Throwable? = runCatching {
        System.loadLibrary("ailikegpt_native")
    }.exceptionOrNull()

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
    private external fun nativeUnloadModel()
    private external fun nativeIsModelLoaded(): Boolean
}
