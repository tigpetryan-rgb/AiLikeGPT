#include <jni.h>

#include <algorithm>
#include <atomic>
#include <cstdint>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#ifdef AILIKEGPT_WITH_LLAMA
#include "llama.h"
#endif

namespace {

constexpr const char* kGenerationCancelled = "__AILIKEGPT_CANCELLED__";

std::atomic_bool g_cancel_requested{false};
std::atomic_bool g_generating{false};

jstring to_jstring(JNIEnv* env, const char* value) {
    return env->NewStringUTF(value);
}

jstring to_jstring(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

void append_utf8_code_point(std::string& output, uint32_t code_point) {
    if (code_point <= 0x7F) {
        output.push_back(static_cast<char>(code_point));
    } else if (code_point <= 0x7FF) {
        output.push_back(static_cast<char>(0xC0 | (code_point >> 6)));
        output.push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
    } else if (code_point <= 0xFFFF) {
        output.push_back(static_cast<char>(0xE0 | (code_point >> 12)));
        output.push_back(static_cast<char>(0x80 | ((code_point >> 6) & 0x3F)));
        output.push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
    } else {
        output.push_back(static_cast<char>(0xF0 | (code_point >> 18)));
        output.push_back(static_cast<char>(0x80 | ((code_point >> 12) & 0x3F)));
        output.push_back(static_cast<char>(0x80 | ((code_point >> 6) & 0x3F)));
        output.push_back(static_cast<char>(0x80 | (code_point & 0x3F)));
    }
}

bool jstring_to_utf8(JNIEnv* env, jstring value, std::string* output) {
    if (value == nullptr || output == nullptr) return false;

    const jsize length = env->GetStringLength(value);
    const jchar* chars = env->GetStringChars(value, nullptr);
    if (chars == nullptr) return false;

    output->clear();
    output->reserve(static_cast<size_t>(length) * 3U);

    for (jsize index = 0; index < length; ++index) {
        uint32_t code_point = chars[index];

        if (code_point >= 0xD800 && code_point <= 0xDBFF) {
            if (index + 1 < length) {
                const uint32_t low = chars[index + 1];
                if (low >= 0xDC00 && low <= 0xDFFF) {
                    code_point = 0x10000 + ((code_point - 0xD800) << 10) + (low - 0xDC00);
                    ++index;
                } else {
                    code_point = 0xFFFD;
                }
            } else {
                code_point = 0xFFFD;
            }
        } else if (code_point >= 0xDC00 && code_point <= 0xDFFF) {
            code_point = 0xFFFD;
        }

        append_utf8_code_point(*output, code_point);
    }

    env->ReleaseStringChars(value, chars);
    return true;
}

#ifdef AILIKEGPT_WITH_LLAMA
std::once_flag g_backend_once;
std::mutex g_runtime_mutex;
std::mutex g_generation_mutex;
bool g_backend_initialized = false;
llama_model* g_model = nullptr;
llama_context* g_context = nullptr;
uint32_t g_context_size = 4096;
int32_t g_threads = 1;

void ensure_backend_initialized() {
    std::call_once(g_backend_once, [] {
        ggml_backend_load_all();
        llama_backend_init();
        g_backend_initialized = true;
    });
}

void free_context_locked() {
    if (g_context != nullptr) {
        llama_free(g_context);
        g_context = nullptr;
    }
}

bool create_context_locked() {
    if (g_model == nullptr) return false;

    free_context_locked();

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = g_context_size;
    context_params.n_batch = std::min<uint32_t>(g_context_size, 512U);
    context_params.n_threads = g_threads;
    context_params.n_threads_batch = g_threads;

    g_context = llama_init_from_model(g_model, context_params);
    return g_context != nullptr;
}

void unload_model_locked() {
    free_context_locked();

    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
}

bool format_chat_prompt_locked(
    const std::string& system_prompt,
    const std::string& user_prompt,
    std::string* formatted_prompt,
    std::string* error
) {
    if (g_model == nullptr || formatted_prompt == nullptr || error == nullptr) {
        return false;
    }

    const char* chat_template = llama_model_chat_template(g_model, nullptr);
    if (chat_template != nullptr && chat_template[0] != '\0') {
        std::vector<llama_chat_message> messages;
        if (!system_prompt.empty()) {
            messages.push_back({"system", system_prompt.c_str()});
        }
        messages.push_back({"user", user_prompt.c_str()});

        int32_t needed = llama_chat_apply_template(
            chat_template,
            messages.data(),
            messages.size(),
            true,
            nullptr,
            0
        );
        if (needed < 0) {
            *error = "failed to size model chat template";
            return false;
        }

        std::vector<char> buffer(static_cast<size_t>(needed) + 1U);
        int32_t written = llama_chat_apply_template(
            chat_template,
            messages.data(),
            messages.size(),
            true,
            buffer.data(),
            static_cast<int32_t>(buffer.size())
        );
        if (written < 0) {
            *error = "failed to apply model chat template";
            return false;
        }

        if (written > static_cast<int32_t>(buffer.size())) {
            buffer.resize(static_cast<size_t>(written) + 1U);
            written = llama_chat_apply_template(
                chat_template,
                messages.data(),
                messages.size(),
                true,
                buffer.data(),
                static_cast<int32_t>(buffer.size())
            );
            if (written < 0) {
                *error = "failed to apply resized model chat template";
                return false;
            }
        }

        formatted_prompt->assign(buffer.data(), static_cast<size_t>(written));
        return true;
    }

    formatted_prompt->clear();
    if (!system_prompt.empty()) {
        formatted_prompt->append("System: ");
        formatted_prompt->append(system_prompt);
        formatted_prompt->append("\n\n");
    }
    formatted_prompt->append("User: ");
    formatted_prompt->append(user_prompt);
    formatted_prompt->append("\nAssistant:");
    return true;
}

bool tokenize_prompt_locked(
    const std::string& prompt,
    std::vector<llama_token>* tokens,
    std::string* error
) {
    if (g_model == nullptr || tokens == nullptr || error == nullptr) return false;

    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    if (vocab == nullptr) {
        *error = "loaded model has no vocabulary";
        return false;
    }

    int32_t token_count = llama_tokenize(
        vocab,
        prompt.data(),
        static_cast<int32_t>(prompt.size()),
        nullptr,
        0,
        true,
        true
    );

    if (token_count == 0) {
        tokens->clear();
        return true;
    }

    size_t required = static_cast<size_t>(token_count < 0 ? -token_count : token_count);
    tokens->resize(required);

    int32_t written = llama_tokenize(
        vocab,
        prompt.data(),
        static_cast<int32_t>(prompt.size()),
        tokens->data(),
        static_cast<int32_t>(tokens->size()),
        true,
        true
    );

    if (written < 0) {
        required = static_cast<size_t>(-written);
        tokens->resize(required);
        written = llama_tokenize(
            vocab,
            prompt.data(),
            static_cast<int32_t>(prompt.size()),
            tokens->data(),
            static_cast<int32_t>(tokens->size()),
            true,
            true
        );
    }

    if (written < 0) {
        *error = "failed to tokenize prompt";
        return false;
    }

    tokens->resize(static_cast<size_t>(written));
    return true;
}

bool token_to_piece_locked(llama_token token, std::string* piece, std::string* error) {
    if (g_model == nullptr || piece == nullptr || error == nullptr) return false;

    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    char small_buffer[256];
    int32_t written = llama_token_to_piece(
        vocab,
        token,
        small_buffer,
        sizeof(small_buffer),
        0,
        true
    );

    if (written >= 0) {
        piece->assign(small_buffer, static_cast<size_t>(written));
        return true;
    }

    std::vector<char> buffer(static_cast<size_t>(-written));
    written = llama_token_to_piece(
        vocab,
        token,
        buffer.data(),
        static_cast<int32_t>(buffer.size()),
        0,
        true
    );
    if (written < 0) {
        *error = "failed to convert generated token to bytes";
        return false;
    }

    piece->assign(buffer.data(), static_cast<size_t>(written));
    return true;
}

bool emit_token_bytes(
    JNIEnv* env,
    jobject callback,
    jmethodID on_bytes,
    const std::string& piece
) {
    if (piece.empty()) return true;

    jbyteArray bytes = env->NewByteArray(static_cast<jsize>(piece.size()));
    if (bytes == nullptr) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        return false;
    }

    env->SetByteArrayRegion(
        bytes,
        0,
        static_cast<jsize>(piece.size()),
        reinterpret_cast<const jbyte*>(piece.data())
    );
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        env->DeleteLocalRef(bytes);
        return false;
    }

    env->CallVoidMethod(callback, on_bytes, bytes);
    const bool callback_failed = env->ExceptionCheck();
    if (callback_failed) env->ExceptionClear();
    env->DeleteLocalRef(bytes);
    return !callback_failed;
}

struct GenerationStateGuard {
    GenerationStateGuard() {
        g_generating.store(true, std::memory_order_release);
        g_cancel_requested.store(false, std::memory_order_release);
    }

    ~GenerationStateGuard() {
        g_generating.store(false, std::memory_order_release);
        g_cancel_requested.store(false, std::memory_order_release);
    }
};
#endif

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeVersion(
    JNIEnv* env,
    jobject /* thiz */
) {
    return to_jstring(env, "ailikegpt-native/0.3");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeBackendStatus(
    JNIEnv* env,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    ensure_backend_initialized();
    return to_jstring(env, "llama.cpp linked; backend initialized; streaming generation ready");
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
    (void)jmodel_path;
    (void)context_size;
    (void)threads;
    return to_jstring(env, "llama.cpp is not linked into this build");
#else
    if (jmodel_path == nullptr) {
        return to_jstring(env, "model path is null");
    }

    ensure_backend_initialized();

    std::string model_path;
    if (!jstring_to_utf8(env, jmodel_path, &model_path)) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        return to_jstring(env, "unable to read model path");
    }

    g_cancel_requested.store(true, std::memory_order_release);
    std::lock_guard<std::mutex> lock(g_runtime_mutex);
    unload_model_locked();

    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (g_model == nullptr) {
        return to_jstring(env, "failed to load GGUF model");
    }

    g_context_size = static_cast<uint32_t>(std::max(512, static_cast<int>(context_size)));
    g_threads = static_cast<int32_t>(std::max(1, static_cast<int>(threads)));

    if (!create_context_locked()) {
        llama_model_free(g_model);
        g_model = nullptr;
        return to_jstring(env, "model loaded but context creation failed");
    }

    return to_jstring(env, "");
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeGenerateChat(
    JNIEnv* env,
    jobject /* thiz */,
    jstring jsystem_prompt,
    jstring juser_prompt,
    jint max_tokens,
    jfloat temperature,
    jfloat min_p,
    jint seed,
    jobject callback
) {
#ifndef AILIKEGPT_WITH_LLAMA
    (void)jsystem_prompt;
    (void)juser_prompt;
    (void)max_tokens;
    (void)temperature;
    (void)min_p;
    (void)seed;
    (void)callback;
    return to_jstring(env, "llama.cpp is not linked into this build");
#else
    if (juser_prompt == nullptr || jsystem_prompt == nullptr) {
        return to_jstring(env, "prompt is null");
    }
    if (callback == nullptr) {
        return to_jstring(env, "stream callback is null");
    }

    std::unique_lock<std::mutex> generation_lock(g_generation_mutex, std::try_to_lock);
    if (!generation_lock.owns_lock()) {
        return to_jstring(env, "generation already in progress");
    }

    std::lock_guard<std::mutex> runtime_lock(g_runtime_mutex);
    if (g_model == nullptr || g_context == nullptr) {
        return to_jstring(env, "no model is loaded");
    }
    if (llama_model_has_encoder(g_model)) {
        return to_jstring(env, "encoder models are not supported by the chat generator yet");
    }

    std::string system_prompt;
    std::string user_prompt;
    if (!jstring_to_utf8(env, jsystem_prompt, &system_prompt) ||
        !jstring_to_utf8(env, juser_prompt, &user_prompt)) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        return to_jstring(env, "unable to read UTF-16 prompt text");
    }

    if (!create_context_locked()) {
        return to_jstring(env, "failed to reset llama context for generation");
    }

    GenerationStateGuard generation_state;

    std::string prompt;
    std::string error;
    if (!format_chat_prompt_locked(system_prompt, user_prompt, &prompt, &error)) {
        return to_jstring(env, error);
    }

    std::vector<llama_token> prompt_tokens;
    if (!tokenize_prompt_locked(prompt, &prompt_tokens, &error)) {
        return to_jstring(env, error);
    }
    if (prompt_tokens.empty()) {
        return to_jstring(env, "prompt tokenization produced no tokens");
    }

    const uint32_t context_capacity = llama_n_ctx(g_context);
    if (prompt_tokens.size() > context_capacity) {
        return to_jstring(env, "prompt exceeds loaded context size");
    }

    const int safe_max_tokens = std::max(1, static_cast<int>(max_tokens));
    const float safe_temperature = std::max(0.0F, static_cast<float>(temperature));
    const float safe_min_p = std::clamp(static_cast<float>(min_p), 0.0F, 1.0F);

    jclass callback_class = env->GetObjectClass(callback);
    if (callback_class == nullptr) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        return to_jstring(env, "unable to inspect stream callback");
    }

    jmethodID on_bytes = env->GetMethodID(callback_class, "onBytes", "([B)V");
    if (on_bytes == nullptr) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        env->DeleteLocalRef(callback_class);
        return to_jstring(env, "stream callback does not expose onBytes(byte[])");
    }

    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    const size_t prefill_chunk_size = std::min<size_t>(512U, context_capacity);
    size_t offset = 0;

    while (offset < prompt_tokens.size()) {
        if (g_cancel_requested.load(std::memory_order_acquire)) {
            env->DeleteLocalRef(callback_class);
            return to_jstring(env, kGenerationCancelled);
        }

        const size_t chunk_size = std::min(prefill_chunk_size, prompt_tokens.size() - offset);
        llama_batch batch = llama_batch_get_one(
            prompt_tokens.data() + offset,
            static_cast<int32_t>(chunk_size)
        );
        const int32_t decode_result = llama_decode(g_context, batch);
        if (decode_result != 0) {
            env->DeleteLocalRef(callback_class);
            return to_jstring(env, "llama prompt prefill failed");
        }
        offset += chunk_size;
    }

    using SamplerPtr = std::unique_ptr<llama_sampler, decltype(&llama_sampler_free)>;
    SamplerPtr sampler(
        llama_sampler_chain_init(llama_sampler_chain_default_params()),
        &llama_sampler_free
    );
    if (!sampler) {
        env->DeleteLocalRef(callback_class);
        return to_jstring(env, "failed to create sampler chain");
    }

    if (safe_temperature <= 0.0F) {
        llama_sampler_chain_add(sampler.get(), llama_sampler_init_greedy());
    } else {
        if (safe_min_p > 0.0F) {
            llama_sampler_chain_add(sampler.get(), llama_sampler_init_min_p(safe_min_p, 1));
        }
        llama_sampler_chain_add(sampler.get(), llama_sampler_init_temp(safe_temperature));
        llama_sampler_chain_add(
            sampler.get(),
            llama_sampler_init_dist(static_cast<uint32_t>(seed))
        );
    }

    size_t context_used = prompt_tokens.size();
    bool cancelled = false;

    for (int generated = 0; generated < safe_max_tokens; ++generated) {
        if (g_cancel_requested.load(std::memory_order_acquire)) {
            cancelled = true;
            break;
        }

        const llama_token token = llama_sampler_sample(sampler.get(), g_context, -1);
        if (llama_vocab_is_eog(vocab, token)) {
            break;
        }

        std::string piece;
        if (!token_to_piece_locked(token, &piece, &error)) {
            env->DeleteLocalRef(callback_class);
            return to_jstring(env, error);
        }

        if (!emit_token_bytes(env, callback, on_bytes, piece)) {
            env->DeleteLocalRef(callback_class);
            return to_jstring(env, "stream callback failed");
        }

        if (g_cancel_requested.load(std::memory_order_acquire)) {
            cancelled = true;
            break;
        }

        if (generated + 1 >= safe_max_tokens || context_used >= context_capacity) {
            break;
        }

        llama_token next_input = token;
        llama_batch batch = llama_batch_get_one(&next_input, 1);
        const int32_t decode_result = llama_decode(g_context, batch);
        if (decode_result != 0) {
            env->DeleteLocalRef(callback_class);
            return to_jstring(env, "llama token decode failed");
        }
        ++context_used;
    }

    env->DeleteLocalRef(callback_class);
    return to_jstring(env, cancelled ? kGenerationCancelled : "");
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeCancelGeneration(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    g_cancel_requested.store(true, std::memory_order_release);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeIsGenerating(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    return g_generating.load(std::memory_order_acquire) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeUnloadModel(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    g_cancel_requested.store(true, std::memory_order_release);
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
    g_cancel_requested.store(true, std::memory_order_release);
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
