/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-20
 */

#ifndef ANDROID_LOG_H
#define ANDROID_LOG_H

#include <android/log.h>

#define LOG_TAG "bc_im_silk"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //ANDROID_LOG_H
