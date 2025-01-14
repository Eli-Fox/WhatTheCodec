#include <jni.h>

#include "video_config.h"
#include "frame_extractor.h"

// File with JNI bindings for VideoFileConfig java class.

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_getFileFormat(JNIEnv *env, jobject instance) {
    auto *videoConfig = video_config_get(instance);
    return env->NewStringUTF(videoConfig->avFormatContext->iformat->long_name);
}

JNIEXPORT void JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_release(JNIEnv *env, jobject instance) {
    video_config_free(instance);
}

JNIEXPORT jstring JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_getCodecName(JNIEnv *env, jobject instance) {
    auto *videoConfig = video_config_get(instance);
    return env->NewStringUTF(videoConfig->avVideoCodec->long_name);
}

JNIEXPORT jint JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_getWidth(JNIEnv *, jobject instance) {
    auto *videoConfig = video_config_get(instance);
    return videoConfig->parameters->width;
}

JNIEXPORT jint JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_getHeight(JNIEnv *, jobject instance) {
    auto *videoConfig = video_config_get(instance);
    return videoConfig->parameters->height;
}

JNIEXPORT void JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_nativeNewFD(JNIEnv *, jobject instance,
                                                            jint jFileDescriptor) {
    video_config_new(instance, jFileDescriptor);
}

JNIEXPORT void JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_nativeNewPath(JNIEnv *env, jobject instance,
                                                              jstring jfilePath) {
    const char *filePath = env->GetStringUTFChars(jfilePath, nullptr);

    video_config_new(instance, filePath);

    env->ReleaseStringUTFChars(jfilePath, filePath);
}

JNIEXPORT jboolean JNICALL
Java_com_javernaut_whatthecodec_VideoFileConfig_fillWithPreview(JNIEnv *env, jobject instance,
                                                                jobjectArray jBitmaps) {
    bool areAllFramesOk = frame_extractor_fill_with_preview(env, instance, jBitmaps);
    return static_cast<jboolean>(areAllFramesOk);
}
}