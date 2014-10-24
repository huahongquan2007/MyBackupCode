#include "robotbase_vision_NativeFaceDetection.h"

#include "opencv2/opencv.hpp"
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>
#include <string>
using namespace cv;
using namespace std;

#include <android/log.h>
// Utility for logging:
#define LOG_TAG    "CAMERA_RENDERER"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Face detection
CascadeClassifier faceDetector;
int IMG_WIDTH = 240;
int IMG_HEIGHT = 320;
string myPath;
int width = 0, height = 0;
bool myPathWrite = true;
/*
 * Class:     com_example_facedetectionsurfaceviewoverlay_Native
 * Method:    getPos
 * Signature: ([BII)V
 */
JNIEXPORT jint JNICALL Java_robotbase_vision_NativeFaceDetection_getPos
  (JNIEnv * env, jclass clazz, jbyteArray NV21FrameData, jintArray xArr, jintArray yArr, jintArray wArr, jintArray hArr, jlong pixelsArr){

//	Mat& pixelsMat = *(Mat*)pixelsArr;

    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
	Mat mGray(height, width, CV_8UC1, (unsigned char *) pNV21FrameData);
//	cvtColor(mGray, pixelsMat, CV_GRAY2BGRA);

	float scale_w = float(IMG_WIDTH) / mGray.cols;
	float scale_h = float(IMG_HEIGHT) / mGray.rows;
	resize(mGray, mGray, Size(), scale_w, scale_h);
	equalizeHist(mGray, mGray);

	std::vector<cv::Rect> faces;
	faceDetector.detectMultiScale(mGray, faces, 1.1, 3, 0 |  CV_HAAR_SCALE_IMAGE, cv::Size(40, 40));
	int sizeArr = (faces.size() > 0) ? faces.size() : 0;
//	LOG("HHQ: FACE DETECTION 123 %d Scale_w: %f Cols: %d", sizeArr, scale_w, mGray.cols);
	if(sizeArr > 0){
		jboolean isCopy=JNI_FALSE;
		jint *nxArr = env->GetIntArrayElements(xArr, &isCopy);
		jint *nyArr = env->GetIntArrayElements(yArr, &isCopy);
		jint *nwArr = env->GetIntArrayElements(wArr, &isCopy);
		jint *nhArr = env->GetIntArrayElements(hArr, &isCopy);

		for (int i = 0; i < faces.size(); i++)
		{
			LOG("HHQ: FACE DETECTION PROCESS %d", i);
			nxArr[i] = faces[i].x / scale_w;
			nyArr[i] = faces[i].y / scale_h;
			nwArr[i] = faces[i].width / scale_w;
			nhArr[i] = faces[i].height / scale_h;
		}

		env->ReleaseIntArrayElements(xArr, nxArr, false);
	    env->ReleaseIntArrayElements(yArr, nyArr, false);
	    env->ReleaseIntArrayElements(wArr, nwArr, false);
	    env->ReleaseIntArrayElements(hArr, nhArr, false);
	}

	faces.clear();
	env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
	return sizeArr;
}

/*
 * Class:     com_example_facedetectionsurfaceviewoverlay_Native
 * Method:    initCascade
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeFaceDetection_initCascade(
		JNIEnv * env, jclass clazz, jstring cascasdePath) {
	try {
		const char *cascasdeString = env->GetStringUTFChars(cascasdePath,0);
		faceDetector.load(cascasdeString);
		LOG("HHQ: LOAD FACE DETECTION");
	} catch (Exception e) {
	}
	if (faceDetector.empty()) {
		LOG("ERROR: Couldn't load Face Detector");
	}
}
/*
 * Class:     com_example_facedetectionsurfaceviewoverlay_Native
 * Method:    setPreviewSize
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeFaceDetection_setPreviewSize
  (JNIEnv * env, jclass clazz, jint w, jint h)
{
	LOG("HHQ: Native Set PreviewSize");
	width = w; height = h;
}
