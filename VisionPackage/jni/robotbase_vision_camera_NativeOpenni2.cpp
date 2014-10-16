#include "robotbase_vision_camera_NativeOpenni2.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "Build_OpenNI2/Include/OpenNI.h"
#include "Build_OpenNI2/Common/OniSampleUtilities.h"
#include "buildOpenCV/include/opencv2/highgui/highgui.hpp"
#include "buildOpenCV/include/opencv2/imgproc/imgproc.hpp"
#include <android/log.h>
// Utility for logging:
#define LOG_TAG    "OPENNI2_LOG"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace openni;
using namespace cv;

Device device;
VideoStream color;
VideoFrameRef colorFrame;
Status rc;
Mat frame;

int init() {
	rc = OpenNI::initialize();
	if (rc != STATUS_OK) {
		LOG("Initialize failed\n%s\n", OpenNI::getExtendedError());
		return 1;
	}

	rc = device.open(ANY_DEVICE);
	if (rc != STATUS_OK) {
		LOG("Couldn't open device\n%s\n", OpenNI::getExtendedError());
		return 2;
	}

	if (device.getSensorInfo(SENSOR_COLOR) != NULL) {
		rc = color.create(device, SENSOR_COLOR);
		if (rc != STATUS_OK) {
			LOG("Couldn't create color stream\n%s\n",
					OpenNI::getExtendedError());
			return 3;
		}
	}

	rc = color.start();
	if (rc != STATUS_OK) {
		LOG("Couldn't start the color stream\n%s\n",
				OpenNI::getExtendedError());
		return 4;
	}

	LOG("INIT OK");

	return 0;
}

Mat getData() {
	color.readFrame(&colorFrame);
	const openni::RGB888Pixel* imageBuffer =
			(const openni::RGB888Pixel*) colorFrame.getData();

	frame.create(colorFrame.getHeight(), colorFrame.getWidth(),
	CV_8UC3);
	memcpy(frame.data, imageBuffer,
			3 * colorFrame.getHeight() * colorFrame.getWidth()
					* sizeof(uint8_t));
	return frame;
}

JNIEXPORT void JNICALL Java_robotbase_vision_camera_NativeOpenni2_InitCamera(
		JNIEnv *env, jclass clazz) {
	init();
}

JNIEXPORT void JNICALL Java_robotbase_vision_camera_NativeOpenni2_GetImage(JNIEnv *env,
		jclass clazz, jlong address) {
	Mat& foreMat = *(Mat*) address;
	foreMat = getData().clone();
}

JNIEXPORT void JNICALL Java_robotbase_vision_camera_NativeOpenni2_GetImageByte
  (JNIEnv *env, jclass clazz, jbyteArray data_byte) {
	Mat mGray;
	cvtColor(frame, mGray, CV_BGR2GRAY);
	jbyte * poutPixels = env->GetByteArrayElements(data_byte, 0);
	int size = mGray.total() * mGray.elemSize();
                        memcpy(poutPixels, mGray.data,size * sizeof(char));

	env->ReleaseByteArrayElements(data_byte, poutPixels, 0);
}
