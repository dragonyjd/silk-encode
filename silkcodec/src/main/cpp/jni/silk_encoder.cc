/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-20
 */

#include <jni.h>
#include <silk/SilkCodec.h>
#include "silk_encoder.h"
#include "android_log.h"


#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_baice_bc_1im_1silk_SilkEncoder_init
        (JNIEnv *env, jclass clazz) {
    return Silk_Encoder_Init();
}

JNIEXPORT jint JNICALL Java_com_baice_bc_1im_1silk_SilkEncoder_encode
            (JNIEnv *env, jclass clazz, jshortArray inArray, jbyteArray outArray, jint outLen) {
    jsize inLen = env->GetArrayLength(inArray);
    jshort inBuf[inLen];
    env->GetShortArrayRegion(inArray, 0, inLen, inBuf);

    jbyte outBuf[outLen];

    int encodeLength = Silk_Encoder_Encode((short *)inBuf, inLen, (unsigned char *)outBuf, outLen);

    env->SetByteArrayRegion(outArray, 0, outLen, outBuf);
    return encodeLength;
}

JNIEXPORT jdouble JNICALL Java_com_baice_bc_1im_1silk_SilkEncoder_finish
        (JNIEnv *env, jclass clazz) {
    return Silk_Encoder_Finish();
}

#ifdef __cplusplus
}
#endif