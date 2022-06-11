// Tencent is pleased to support the open source community by making Mars available.
// Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.

// Licensed under the MIT License (the "License"); you may not use this file except in 
// compliance with the License. You may obtain a copy of the License at
// http://opensource.org/licenses/MIT

// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing permissions and
// limitations under the License.


/**
 * created on : 2012-07-30
 * author    : yanguoyue
 */

#include "comm_function.h"

#include <jni.h>
#include <string>
#include <android/log.h>

#include "var_cache.h"

using namespace std;

jvalue __JNU_CallStaticMethodByName(
    JNIEnv* _env,
    jclass _clazz,
    const char* _name,
    const char* _descriptor,
    va_list args) {

    VarCache* cacheInastance = VarCache::Singleton();

    jmethodID mid;
    jvalue result;
    memset(&result, 0 , sizeof(result));

    mid = cacheInastance->GetStaticMethodId(_env, _clazz, _name, _descriptor);

    if (mid) {
        const char* p = _descriptor;

        /* skip over argument types to find out the
         return type */
        while (*p != ')')
            p++;

        /* skip ')' */
        p++;

        switch (*p) {
        case 'V':
            _env->CallStaticVoidMethodV(_clazz, mid, args);
            break;

        case '[':
        case 'L':
            result.l = _env->CallStaticObjectMethodV(_clazz, mid, args);
            break;

        case 'Z':
            result.z = _env->CallStaticBooleanMethodV(_clazz, mid, args);
            break;

        case 'B':
            result.b = _env->CallStaticByteMethodV(_clazz, mid, args);
            break;

        case 'C':
            result.c = _env->CallStaticCharMethodV(_clazz, mid, args);
            break;

        case 'S':
            result.s = _env->CallStaticShortMethodV(_clazz, mid, args);
            break;

        case 'I':
            result.i = _env->CallStaticIntMethodV(_clazz, mid, args);
            break;

        case 'J':
            result.j = _env->CallStaticLongMethodV(_clazz, mid, args);
            break;

        case 'F':
            result.f = _env->CallStaticFloatMethodV(_clazz, mid, args);
            break;

        case 'D':
            result.d = _env->CallStaticDoubleMethodV(_clazz, mid, args);
            break;

        default:
            _env->FatalError("illegal _descriptor");
            break;
        }
    }

    return result;
}

jvalue JNU_CallStaticMethodByMethodInfo(JNIEnv* _env, JniMethodInfo methodInfo, ...) {
    jclass _clazz = VarCache::Singleton()->GetClass(_env, methodInfo.classname.c_str());

    va_list args;
    va_start(args, methodInfo);
    jvalue result = __JNU_CallStaticMethodByName(_env, _clazz, methodInfo.methodname.c_str(), methodInfo.methodsig.c_str(), args);
    va_end(args);

    return result;
}


// char* to jstring
jstring JNU_Chars2Jstring(JNIEnv* _env, const char* pat) {
    VarCache* cacheInastance = VarCache::Singleton();
    jclass str_class = cacheInastance->GetClass(_env, "java/lang/String");
    jmethodID ctorID = cacheInastance->GetMethodId(_env, str_class, "<init>", "([BLjava/lang/String;)V");

    jbyteArray bytes;

    if (pat != NULL) {
        bytes = _env->NewByteArray((jsize)strlen(pat));
        _env->SetByteArrayRegion(bytes, 0, (jsize)strlen(pat), (jbyte*) pat);
    } else {
        bytes = _env->NewByteArray(1);
        char ch[1] =
        { 0 };
        _env->SetByteArrayRegion(bytes, 0, 1, (jbyte*) ch);
    }

    jstring encoding = _env->NewStringUTF("utf-8");

    jstring jstr = (jstring) _env->NewObject(str_class, ctorID, bytes, encoding);
    _env->DeleteLocalRef(bytes);
    _env->DeleteLocalRef(encoding);

    return jstr;
}
