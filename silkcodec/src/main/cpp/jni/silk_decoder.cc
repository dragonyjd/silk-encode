/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-20
 */

#include <jni.h>
#include <silk/SilkCodec.h>
#include <jni/util/comm_function.h>
#include "jni/util/var_cache.h"
#include "jni/util/scope_jenv.h"
#include "silk_decoder.h"
#include "android_log.h"

#ifdef __cplusplus
extern "C" {
#endif

extern jobject g_objBcImLogic;

DEFINE_FIND_CLASS(KSilkJava2C, "com/baice/bc_im_silk/SilkDecoder")

JNIEXPORT jint JNICALL Java_com_baice_bc_1im_1silk_SilkDecoder_decode
            (JNIEnv *env, jclass clazz, jstring filename) {
    const char *fn = env->GetStringUTFChars(filename, JNI_FALSE);
    return Silk_Decoder(fn);
}

JNIEXPORT void JNICALL Java_com_baice_bc_1im_1silk_SilkDecoder_reset
        (JNIEnv *env, jclass clazz) {
    Silk_Decoder_Reset();
}

JNIEXPORT jdouble JNICALL Java_com_baice_bc_1im_1silk_SilkDecoder_getRecordLength
        (JNIEnv *env, jclass clazz, jstring filename) {
    const char *fn = env->GetStringUTFChars(filename, JNI_FALSE);
    return Silk_Get_Record_Length(fn);
}

DEFINE_FIND_STATIC_METHOD(KSilkJava2C_decode_callback, KSilkJava2C, "decode_callback", "([SI)V")
void decode_callback(short outBuf[], uint32_t outLen)
{
    if (outBuf != NULL && outLen > 0) {
        VarCache* cache_instance = VarCache::Singleton();
        ScopeJEnv scope_jenv(cache_instance->GetJvm());
        JNIEnv *env = scope_jenv.GetEnv();

        jshortArray outArray = env->NewShortArray(outLen);
        env->SetShortArrayRegion(outArray, 0, outLen, outBuf);
        JNU_CallStaticMethodByMethodInfo(env, KSilkJava2C_decode_callback, outArray, (jint)outLen);
        env->DeleteLocalRef(outArray);
    }
}

#ifdef __cplusplus
}
#endif