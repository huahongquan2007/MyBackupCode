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

//    int len = std::max(src.cols, src.rows);
//    cv::Point2f pt(240/2., 320/2.);
//    cv::Mat r = cv::getRotationMatrix2D(pt, angle, 1.0);
//    circle(src, pt, 10, Scalar(255,0,0));
    //cv::warpAffine(src, dst, r, cv::Size(src.rows, src.cols));
//    cv::warpAffine(src, dst, r, dst.size());

}

/*
 * Class:     robotbase_vision_NativeAndroidCamera
 * Method:    getFrame
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeAndroidCamera_getFrame
  (JNIEnv *env, jclass clazz, jbyteArray NV21FrameData, jint width, jint height,jlong matAdd, jbyteArray byteAdd){
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    Mat mYuv(240 + 120, 320, CV_8UC1, (unsigned char *) pNV21FrameData);
    Mat mRgb(240,320, CV_8UC4);
    cvtColor(mYuv, mRgb, CV_YUV420sp2RGBA);
    Mat mGray(240,320, CV_8UC1);
    cvtColor(mRgb, mGray, CV_BGRA2GRAY);
    Mat mGrayRotated(320, 240, CV_8UC1);
    rotate(mGray, mGrayRotated);

	jbyte * poutPixels = env->GetByteArrayElements(byteAdd, 0);
	int size = mGrayRotated.total() * mGray.elemSize();
    memcpy(poutPixels, mGrayRotated.data,size * sizeof(char));

	Mat& pixel = *(Mat*)matAdd; // CV_8UC4
	rotate(mRgb, pixel);

	mRgb.release();
	mGray.release();
	mGrayRotated.release();

	env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
	env->ReleaseByteArrayElements(byteAdd, poutPixels, 0);
	return;
}
