#include <jni.h>

namespace {

jstring to_jstring(JNIEnv* env, const char* value) {
    return env->NewStringUTF(value);
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeVersion(
    JNIEnv* env,
    jobject /* thiz */
) {
    return to_jstring(env, "ailikegpt-native/0.1");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ailikegpt_app_runtime_NativeRuntime_nativeBackendStatus(
    JNIEnv* env,
    jobject /* thiz */
) {
#ifdef AILIKEGPT_WITH_LLAMA
    return to_jstring(env, "llama.cpp linked");
#else
    return to_jstring(env, "JNI ready; llama.cpp integration pending Phase 2");
#endif
}
