#include "robotbase_vision_NativeMotionDetection.h"
#include "opencv2/opencv.hpp"
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/video/background_segm.hpp>
#include <vector>
#include <string>

using namespace cv;
using namespace std;

#include <android/log.h>
// Utility for logging:
#define LOG_TAG    "CAMERA_RENDERER"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#include <utility.h>

//static const int MINX = 30;
//static const int MINY = 30;
//static const float STEP_FACE = 0.4;
//static const float STEP_AROUND = 2;
enum State
{
	STATE_FREE,
	STATE_PAN,
	STATE_TILT,
	STATE_CAPTURE,
	STATE_ERROR,
	STATE_LOOK_AROUND
};

vector<FaceInfo> face_info_list;
vector<PointInfo> point_info_list;
bool START_CAPTURE;
time_t start_capture_time;
time_t state_free_time;
float curAngleTilt, curAnglePan;
State curState;
track_motion_class trackMotion;

static bool myPathWrite = true;
/*
 * Class:     com_example_motiondetectionopencv_NativeMotionDetection
 * Method:    processFrame
 * Signature: ([B)V
 */
int IMG_WIDTH = 240;
int IMG_HEIGHT = 180;

JNIEXPORT jintArray JNICALL Java_robotbase_vision_NativeMotionDetection_processFrame(
		JNIEnv *env, jclass clazz, jbyteArray frameData, jint width, jint height, jlong testImg) {
	jbyte * pFrameData = env->GetByteArrayElements(frameData, 0);
	Mat captureFrame(height, width, CV_8UC1, (unsigned char *) pFrameData);
	Mat& foreMat = *(Mat*)testImg;
	if (!captureFrame.data) {
		cout << "Can not read frame" << endl;
	}

	LOG("READY TO GET FACES %d", width);
	Mat mGray, mFore;
	float scale_w = float(IMG_WIDTH) / captureFrame.cols;
	float scale_h = float(IMG_HEIGHT) / captureFrame.rows;
	resize(captureFrame, mGray, Size(), scale_w, scale_h);
	resize(mGray, foreMat, Size(), 1.0f/scale_w, 1.0f/scale_h);
	PointInfo motion;
	trackMotion.run2(mGray, motion, mFore);


	LOG("MOTION X: %d, Y: %d, foreMat size: %d", motion.point.x, motion.point.y, foreMat.rows);


//	if(myPathWrite){
//		myPathWrite = false;
//		vector<int> compression_params;
//		compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
//		bool cool = imwrite("/data/data/com.example.motiondetectionopencv/files/myImage-ForeMat.png", foreMat, compression_params);
////		imwrite("/data/data/com.example.facedetectionsurfaceviewoverlay_Native/files/myImage-Gray.png", mGray, compression_params);
//		if(cool){
//			LOG("HHQ: SUCCESS FOREMAT");
//		}
//		else{
//			LOG("HHQ: FAIL FOREMAT");
//		}
//	}

	env->ReleaseByteArrayElements(frameData, pFrameData, 0);
}
