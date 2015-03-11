#include "aios_core_vision_facedetection_NativeFaceDetectionCV.h"
#include <opencv2/opencv.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <stdio.h>
#include <string.h>

#include "json/json.h"

using namespace cv;
using namespace std;

#include <android/log.h>

#define LOG_TAG    "VisionNative-FaceDetect"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#include "aios_core_vision_utils.h"

// Face detection
CascadeClassifier faceDetector;
int IMG_WIDTH = 240;
int IMG_HEIGHT = 320;
jstring result;

JNIEXPORT jstring JNICALL Java_aios_core_vision_facedetection_NativeFaceDetectionCV_update
(JNIEnv * env, jclass clazz, jbyteArray frameArray, jint width, jint height){

    jbyte * pFrameData = env->GetByteArrayElements(frameArray, 0);
	Mat mRgb(height, width, CV_8UC3, (unsigned char *) pFrameData);

    Mat mGray;
	cvtColor(mRgb, mGray, CV_RGB2GRAY);

	float scale_w = float(IMG_WIDTH) / mGray.cols;
	float scale_h = float(IMG_HEIGHT) / mGray.rows;
	resize(mGray, mGray, Size(), scale_w, scale_h);
	equalizeHist(mGray, mGray);

	std::vector<cv::Rect> faces;
	faceDetector.detectMultiScale(mGray, faces, 1.1, 3, 0 |  CV_HAAR_SCALE_IMAGE, cv::Size(25, 25));
	int sizeArr = (faces.size() > 0) ? faces.size() : 0;

//	LOG("update: NumOfFace: %d Scale_w: %f Cols: %d", sizeArr, scale_w, mGray.cols);

    if(sizeArr > 0){
        // create json result
        Json::Value jsonResult;

		for (int i = 0; i < faces.size(); i++)
		{
            Json::Value jsonObject;
            jsonObject["time"] = now_ms();
            jsonObject["position"][0] = faces[i].x / scale_w;
            jsonObject["position"][1] = faces[i].y / scale_h;
            jsonObject["position"][2] = faces[i].width / scale_w;
            jsonObject["position"][3] = faces[i].height / scale_h;

            jsonResult[i] = jsonObject;
		}
        const char* json_str = jsonResult.toStyledString().c_str();
        result = env->NewStringUTF(json_str);
	}else{
	    result = env->NewStringUTF("");
	}

	mRgb.release();
	mGray.release();
	env->ReleaseByteArrayElements(frameArray, pFrameData, 0);

	return result;
}

JNIEXPORT void JNICALL Java_aios_core_vision_facedetection_NativeFaceDetectionCV_initCascade
  (JNIEnv * env, jclass clazz, jstring cascadePath){
    try {
        const char *cascadeString = env->GetStringUTFChars(cascadePath,0);
        faceDetector.load(cascadeString);
        LOG("Init Face Detection successfully");
    } catch (Exception &e) {
        return;
    }
    if (faceDetector.empty()) {
        LOG("ERROR: Couldn't load Face Detector");
    }
}