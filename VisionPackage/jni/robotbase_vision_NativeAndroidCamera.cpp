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

/**
 * Rotate an image
 */
void rotate(cv::Mat& src, cv::Mat& dst)
{
	cv::transpose(src, dst);
	cv::flip(dst, dst, 0);
}

/*
 * Class:     robotbase_vision_NativeAndroidCamera
 * Method:    getFrame
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeAndroidCamera_getFrame
  (JNIEnv *env, jclass clazz, jbyteArray NV21FrameData, jint width, jint height,jlong matAdd, jbyteArray byteAdd, jbyteArray byteRgb){



    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    Mat mYuv(width + width/2, height, CV_8UC1, (unsigned char *) pNV21FrameData);
    Mat mRgb(width, height, CV_8UC3);
    cvtColor(mYuv, mRgb, CV_YUV420sp2RGB);
    Mat mGray(width , height, CV_8UC1);
    cvtColor(mRgb, mGray, CV_BGR2GRAY);
    Mat mGrayRotated(height, width, CV_8UC1);
    rotate(mGray, mGrayRotated);
    Mat mRgbRotated(height, width, CV_8UC3);
    rotate(mRgb, mRgbRotated);

	jbyte * poutPixels = env->GetByteArrayElements(byteAdd, 0);
	int size = mGrayRotated.total() * mGray.elemSize();
    memcpy(poutPixels, mGrayRotated.data,size * sizeof(char));

	jbyte * poutPixelsRgb = env->GetByteArrayElements(byteRgb, 0);
	int sizeRgb = mRgbRotated.total() * mRgbRotated.elemSize();
    memcpy(poutPixelsRgb, mRgbRotated.data,sizeRgb * sizeof(char));

	Mat& pixel = *(Mat*)matAdd; // CV_8UC4
	pixel = mRgbRotated.clone();
//	rotate(mRgb, pixel);

	mRgb.release();
	mGray.release();
	mGrayRotated.release();
	mRgbRotated.release();

	env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
	env->ReleaseByteArrayElements(byteAdd, poutPixels, 0);
	env->ReleaseByteArrayElements(byteRgb, poutPixelsRgb, 0);
	return;
}
