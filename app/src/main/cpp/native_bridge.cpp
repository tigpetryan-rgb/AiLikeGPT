#include <jni.h>

#include <algorithm>
#include <mutex>
#include <string>

#ifdef AILIKEGPT_WITH_LLAMA
#include "llama.h"
#endif

namespace {

jstring to_jstring(JNIEnv* env, const char* value) {
    return env->NewStringUTF(value);
}

jstring to_jstring(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

#ifdef AILIKEGPT_WITH_LLAMA
std::once_flag g_backend_once;
std::mutex g_runtime_mutex;
bool g_backend_initialized = false;
llama_model* g_model = nullptr;
llama_context* g_context = nullptr;

void ensure_backend_initialized() {
    std::call_once(g_backend_once, [] {
        llama_backend_init();
        g_backend_initialized = true;
    });
}

void unload_model_locked() {
    if (g_context != nullptr) {
        llama_free(g_context);
        g_context = nullptr;
    }

    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
}
#endif

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeVersion(
    JNIEnv* env,
    jobject /* thiz */
) {
    return to_jstring(env, "ailikegpt-native/0.2");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeBackendStatus(
    JNIEnv* env,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    ensure_backend_initialized();
    return to_jstring(env, "llama.cpp linked; backend initialized");
#else
    return to_jstring(env, "JNI ready; sync pinned llama.cpp to enable inference");
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeLoadModel(
    JNIEnv* env,
    jobject /* thiz */,
    jstring jmodel_path,
    jint context_size,
    jint threads
) {
#ifndef AILIKEGPT_WITH_LLAMA
    return to_jstring(env, "llama.cpp is not linked into this build");
#else
    if (jmodel_path == nullptr) {
        return to_jstring(env, "model path is null");
    }

    ensure_backend_initialized();

    const char* model_path = env->GetStringUTFChars(jmodel_path, nullptr);
    if (model_path == nullptr) {
        return to_jstring(env, "unable to read model path");
    }

    const std::string model_path_copy(model_path);
    env->ReleaseStringUTFChars(jmodel_path, model_path);

    std::lock_guard<std::mutex> lock(g_runtime_mutex);
    unload_model_locked();

    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(model_path_copy.c_str(), model_params);
    if (g_model == nullptr) {
        return to_jstring(env, "failed to load GGUF model");
    }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = static_cast<uint32_t>(std::max(512, static_cast<int>(context_size)));

    const int safe_threads = std::max(1, static_cast<int>(threads));
    context_params.n_threads = safe_threads;
    context_params.n_threads_batch = safe_threads;

    g_context = llama_init_from_model(g_model, context_params);
    if (g_context == nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
        return to_jstring(env, "model loaded but context creation failed");
    }

    return to_jstring(env, "");
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeUnloadModel(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    std::lock_guard<std::mutex> lock(g_runtime_mutex);
    unload_model_locked();
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeIsModelLoaded(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    std::lock_guard<std::mutex> lock(g_runtime_mutex);
    return (g_model != nullptr && g_context != nullptr) ? JNI_TRUE : JNI_FALSE;
#else
    return JNI_FALSE;
#endif
}

extern "C" JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM* /* vm */, void* /* reserved */) {
#ifdef AILIKEGPT_WITH_LLAMA
    {
        std::lock_guard<std::mutex> lock(g_runtime_mutex);
        unload_model_locked();
    }

    if (g_backend_initialized) {
        llama_backend_free();
        g_backend_initialized = false;
    }
#endif
}
