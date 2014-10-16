#include "robotbase_vision_NativeAndroidCamera.h"
#include "opencv2/opencv.hpp"
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <stdio.h>
#include <string.h>

using namespace cv;
using namespace std;

#include <android/log.h>
// Utility for logging:
#define LOG_TAG    "CAMERA_RENDERER"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

/*
 * Class:     robotbase_vision_NativeAndroidCamera
 * Method:    getFrame
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeAndroidCamera_getFrame
  (JNIEnv *env, jclass clazz, jbyteArray NV21FrameData, jint width, jint height,jlong matAdd, jbyteArray byteAdd){
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
	Mat mGray(height, width, CV_8UC1, (unsigned char *) pNV21FrameData);

	Mat& pixel = *(Mat*)matAdd;
	pixel = mGray.clone();

	jbyte * poutPixels = env->GetByteArrayElements(byteAdd, 0);
	int size = mGray.total() * mGray.elemSize();
    memcpy(poutPixels, mGray.data,size * sizeof(char));

	env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
	env->ReleaseByteArrayElements(byteAdd, poutPixels, 0);
	return;
}
