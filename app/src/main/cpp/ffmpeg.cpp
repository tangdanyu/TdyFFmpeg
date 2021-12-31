#include <jni.h>
#include <string>
#include <android/log.h>
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"cpp",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"cpp",FORMAT,##__VA_ARGS__);
extern "C" {
#include <libavcodec/avcodec.h>
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_tdy_ffmpeg_NativeLib_stringFromJNI(
        JNIEnv* env,
        jclass clazz) {
    std::string config = avcodec_configuration();

    LOGE("configuration %s\n",config.c_str());

    return env->NewStringUTF(config.c_str());
}

