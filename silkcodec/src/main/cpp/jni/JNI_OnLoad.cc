
#include <jni.h>
#include <pthread.h>
#include "jni/util/var_cache.h"
#include "jni/util/scope_jenv.h"

pthread_key_t g_env_key;

static void __DetachCurrentThread(void* a) {
    if (NULL != VarCache::Singleton()->GetJvm()) {
        VarCache::Singleton()->GetJvm()->DetachCurrentThread();
    }
}

extern "C" {

jobject g_objDecodeCallback = 0;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    if (0 != pthread_key_create(&g_env_key, __DetachCurrentThread)) {
        return(JNI_ERR);
    }

    ScopeJEnv jenv(jvm);
    VarCache::Singleton()->SetJvm(jvm);

    LoadClass(jenv.GetEnv());
    LoadStaticMethod(jenv.GetEnv());
    LoadMethod(jenv.GetEnv());

    JNIEnv *env = jenv.GetEnv();

    jclass cls = env->FindClass("com/baice/bc_im_silk/SilkDecoder");
    if (cls) {
        g_objDecodeCallback = env->NewGlobalRef(cls);
        env->DeleteLocalRef(cls);
    } else {
        printf("--SilkDecoder class");
        return -1;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    VarCache* cache_instance = VarCache::Singleton();
    ScopeJEnv scope_jenv(cache_instance->GetJvm());
    JNIEnv *env = scope_jenv.GetEnv();

    if (g_objDecodeCallback != 0) {
        env->DeleteGlobalRef(g_objDecodeCallback);
        g_objDecodeCallback = 0;
    }

    VarCache::Release();
}

}
