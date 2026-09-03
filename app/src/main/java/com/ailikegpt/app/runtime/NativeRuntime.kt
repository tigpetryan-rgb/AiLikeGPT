package com.ailikegpt.app.runtime

data class NativeRuntimeStatus(
    val loaded: Boolean,
    val version: String,
    val backend: String,
    val error: String? = null,
)

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

    private external fun nativeVersion(): String
    private external fun nativeBackendStatus(): String
}
