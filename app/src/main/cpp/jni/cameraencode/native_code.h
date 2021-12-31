#include <jni.h>

#ifndef NATIVE_ENCODE_H
#define NATIVE_ENCODE_H
#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <jni.h>

extern "C"  {
JNIEXPORT void JNICALL Java_com_example_tdyffmpeg_camera_encode_NativeEncoder_onPreviewFrame(JNIEnv *, jobject, jbyteArray, jint, jint);

JNIEXPORT void JNICALL Java_com_example_tdyffmpeg_camera_encode_NativeEncoder_encodeMP4Start(JNIEnv *, jobject, jstring, jint, jint);

JNIEXPORT void JNICALL Java_com_example_tdyffmpeg_camera_encode_NativeEncoder_encodeMP4Stop(JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_example_tdyffmpeg_camera_encode_NativeEncoder_encodeJPEG(JNIEnv *, jobject, jstring, jint, jint);
}


#ifdef __cplusplus
}
#endif
#endif
