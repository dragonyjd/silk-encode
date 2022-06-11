# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.6)
project(silkcodec)

# silk
include_directories(src/main/cpp/silk/interface)
include_directories(src/main/cpp/silk/src)

aux_source_directory(src/main/cpp/silk/src SILK_SRC)



# wcv codec (WeChat Voice Codec)
include_directories(src/main/cpp)
aux_source_directory(src/main/cpp/silk SILK_CODEC_SRC)
add_library(silkcodec SHARED
        src/main/cpp/jni/silk_encoder.cc
        src/main/cpp/jni/silk_decoder.cc
        src/main/cpp/jni/JNI_OnLoad.cc
        src/main/cpp/jni/util/scope_jenv.cc
        src/main/cpp/jni/util/var_cache.cc
        src/main/cpp/jni/util/comm_function.cc
        ${SILK_SRC} ${SILK_CODEC_SRC})
find_library(android-log log)
target_link_libraries(silkcodec ${android-log})