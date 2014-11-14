#include "robotbase_vision_NativeFaceTracking.h"
#include <android/log.h>
#include <stdlib.h>
#include <ctime>

#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace std;
using namespace cv;

const int maxCorners = 30;
const int minCorners = 15;
const int maxLoopFindCorners = 10;
const double qualityLevel = 0.01;
const double minDistance = 5;
const TermCriteria termcrit(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.03);
const Size subPixWinSize(10, 10), winSize(31, 31);
const int FACE_LOCATION_LIFESPAN = 2;

// Utility for logging:
#define LOG_TAG    "CAMERA_RENDERER"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

class FaceLocation {
public:
	int x, y, w, h;
	vector<Point2f> points[2];
	String faceName;
	int meanX, meanY;
	float width, height;
	time_t lastTime;
	FaceLocation() {
		faceName = random_string();
		updateTime();
	}
	~FaceLocation() {

	}
	string random_string() {
		vector<string> a;
		a.push_back("A");
		a.push_back("B");
		a.push_back("C");
		a.push_back("D");
		a.push_back("E");
		a.push_back("F");
		return a[rand() % 6];
	}
	void updateTime() {
		lastTime = time(NULL);
		cout << "UpdateTime " << lastTime << endl;
	}
	double getPeriodTime() {
		time_t curTime = time(NULL);
		double period = difftime(curTime, lastTime);
		// double period = curTime - lastTime;
		cout << "getPeriodTime: lastTime " << lastTime << " curTime " << curTime
				<< " Period " << period << endl;
		return period;
	}
	void updateMean() {
		meanX = meanY = 0;
		float minX, maxX, minY, maxY;
		minX = maxX = points[0][0].x;
		minY = maxY = points[0][0].y;
		for (size_t j = 0; j < points[0].size(); j++) {
			minX = (points[0][j].x < minX) ? points[0][j].x : minX;
			maxX = (points[0][j].x > maxX) ? points[0][j].x : maxX;
			minY = (points[0][j].y < minY) ? points[0][j].y : minY;
			maxY = (points[0][j].y > maxY) ? points[0][j].y : maxY;

			if (points[0][j].x < 0)
				cout << "ALERT " << points[0][j].x << endl;
			meanX += (points[0][j].x > 0) ? points[0][j].x : 0;
			meanY += (points[0][j].y > 0) ? points[0][j].y : 0;
		}
		cout << "before updateMean " << meanX << " " << meanY << endl;
		meanX = meanX / points[0].size();
		meanY = meanY / points[0].size();
		width = maxX - minX;
		height = maxY - minY;
		cout << "updateMean " << meanX << " " << meanY << " Points Size "
				<< points[0].size() << endl;
	}
	float getRatioOutBound() {
		int numOfBound = 0;
		for (size_t j = 0; j < points[0].size(); j++) {
			float cX = points[0][j].x;
			float cY = points[0][j].y;
			if ((cX > x && cX < x + w) && (cY > y && cY < y + h)) {
				//in box
			} else {
				//out box
				numOfBound++;
			}
		}
		return (float) numOfBound / points[0].size();
	}

};
FaceLocation getFaceLocation(Mat curFaceImg, int x, int y, int width,
		int height) {
	FaceLocation curFaceLocation;
	curFaceLocation.x = x;
	curFaceLocation.y = y;
	curFaceLocation.w = width;
	curFaceLocation.h = height;

	int count = 0;
	while (curFaceLocation.points[0].size() < minCorners) {
		count++;
		if (count > maxLoopFindCorners)
			break;
		goodFeaturesToTrack(curFaceImg, curFaceLocation.points[0], maxCorners,
				qualityLevel, minDistance);
	}
	LOG("HHQ: FACE TRACKING getPos: POINTS[0] Size: %d, curFaceImg Width %d Height %d", curFaceLocation.points[0].size(), curFaceImg.rows, curFaceImg.cols);
	cornerSubPix(curFaceImg, curFaceLocation.points[0], subPixWinSize,
			Size(-1, -1), termcrit);
	for (size_t idx = 0; idx < curFaceLocation.points[0].size(); idx++) {
		cv::circle(curFaceImg, curFaceLocation.points[0].at(idx), 3,
				Scalar(0, 255, 0), -3, 8);
	}
	Point2f t = Point2f(x, y);
	for (size_t idx = 0; idx < curFaceLocation.points[0].size(); idx++) {
		curFaceLocation.points[0][idx] = curFaceLocation.points[0][idx] + t;
	}
	cout << "getFaceLocation " << endl;
	curFaceLocation.updateMean();
	return curFaceLocation;
}
double calculateDistance(double x1, double x2, double y1, double y2) {
	return sqrt(pow((x1 - x2), 2.0) + pow((y1 - y2), 2.0));
}

void updateFaceLocation(Mat &frame, Mat curFaceImg,
		vector<FaceLocation> &faceLocation, int index, float x, float y,
		float w, float h) {
	faceLocation[index].x = x;
	faceLocation[index].y = y;
	faceLocation[index].w = w;
	faceLocation[index].h = h;

	// Neu ratio nam ngoai face box > 50% -> init
	int ratio = faceLocation[index].getRatioOutBound();
	if (ratio > 0.8) {
		FaceLocation fl = getFaceLocation(curFaceImg, x, y, w, h);
		std::swap(faceLocation[index].points[0], fl.points[0]);
		cout << "UpdateRatio > 0.9 " << faceLocation[index].meanX << " "
				<< faceLocation[index].meanY << endl;
	}
	faceLocation[index].updateTime();
}

// Global Variable
vector<FaceLocation> faceLocation;
Mat prevGray, gray, curFaceImg;
vector<Rect> faces;

/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    getPos
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_robotbase_vision_NativeFaceTracking_getPos(
		JNIEnv * env, jclass clazz, jbyteArray frame, jint width, jint height) {
	jbyte * pNV21FrameData = env->GetByteArrayElements(frame, 0);
	Mat mRgb(height, width, CV_8UC3, (unsigned char *) pNV21FrameData);
	cvtColor(mRgb, gray, CV_RGB2GRAY);

	if (prevGray.empty())
		gray.copyTo(prevGray);
	// Loop each faceLocation
	for (size_t i = 0; i < faceLocation.size(); i++) {
		// Calculate Optical Flow
		vector<uchar> status;
		vector<float> err;
		calcOpticalFlowPyrLK(prevGray, gray, faceLocation[i].points[0],
				faceLocation[i].points[1], status, err, winSize, 3, termcrit, 0,
				0.001);

		// Save information
		std::swap(faceLocation[i].points[1], faceLocation[i].points[0]);
		faceLocation[i].updateMean();
	}
	// Map faceLocation to faces
	for (size_t i = 0; i < faces.size(); i++) {

		float meanFaceX = faces[i].x + faces[i].width / 2;
		float meanFaceY = faces[i].y + faces[i].height / 2;
		float radius = (faces[i].width + faces[i].height) / 4;

		std::vector<int> indexVector;
		for (size_t j = 0; j < faceLocation.size(); j++) {
			float distance = calculateDistance(meanFaceX, faceLocation[j].meanX,
					meanFaceY, faceLocation[j].meanY);
			if (distance < radius) {
				indexVector.push_back(j);
			}
		}
		// Neu khong co faceLocation nao => face moi ==> them faceLocation
		// Neu co >2 faceLocation trong vung tim kiem thi phai kiem tra dac trung cho chinh xac
		// Neu chi co 1 faceLocation thi gan luon
		curFaceImg = gray(faces[i]);

		// Check size of curFaceImg
		if(curFaceImg.rows < winSize.height || curFaceImg.cols < winSize.width)
			break;

		if (indexVector.size() == 1) {
			int index = indexVector[0];
			cout << "1 faceLocation " << faceLocation[index].faceName << endl;
			updateFaceLocation(gray, curFaceImg, faceLocation, index,
					faces[i].x, faces[i].y, faces[i].width, faces[i].height);
		} else if (indexVector.size() == 0) {
			cout << "0 faceLocation" << endl;
			faceLocation.push_back(
					getFaceLocation(curFaceImg, faces[i].x, faces[i].y,
							faces[i].width, faces[i].height));
		}
		else {
			cout << ">2 faceLocation" << endl;
			int bestIndex = -1;
			float bestValue = -1;
			for (int idx = indexVector.size() - 1; idx >= 0; idx--) {
				cout << "idx " << idx << endl;
				int index = indexVector[idx];
				cout << "Index : " << index << " idx " << idx << endl;
				float flWidth = faceLocation[index].width;
				float flHeight = faceLocation[index].height;
				cout << "flWidth height : " << flWidth << " " << flHeight
						<< endl;
				float curValue = abs(faces[i].width - flWidth)
						* abs(faces[i].height - flHeight);
				cout << "curValue " << curValue << endl;
				if (bestValue == -1 || curValue < bestValue) {
					bestIndex = index;
					bestValue = curValue;
				}
			}
			cout << "BestIndex " << bestIndex << " bestValue " << bestValue
					<< endl;
			updateFaceLocation(gray, curFaceImg, faceLocation, bestIndex,
					faces[i].x, faces[i].y, faces[i].width, faces[i].height);
			cout << "indexVector " << indexVector.size() << endl;
			for (int idx = indexVector.size() - 1; idx >= 0; idx--) {
				if (idx != bestIndex) {
					cout << "          Erase: " << idx << endl;
					faceLocation.erase(faceLocation.begin() + idx);
				}
			}
		}
		indexVector.clear();
	}

	// Remove old faceLocation
	for (int j = faceLocation.size() - 1; j >= 0; j--) {
		// cout <<"-------------------Before Remove " << j << endl;
		if (faceLocation[j].getPeriodTime() > FACE_LOCATION_LIFESPAN) {
			faceLocation.erase(faceLocation.begin() + j);
			LOG("HHQ: FACE TRACKING getPos: Remove OLD %d", j);
			cout << "====================================== Remove OLD FL : "
					<< j << endl;
		}else{
			LOG("HHQ: FACE TRACKING getPos: DO NOT OLD %d %d", j, faceLocation[j].getPeriodTime());
		}

	}
	swap(prevGray, gray);
	faces.clear();
	env->ReleaseByteArrayElements(frame, pNV21FrameData, 0);

	if(faceLocation.size() > 0){
		LOG("HHQ: FACE TRACKING getPos %d %d %d %d", faceLocation.size(), faceLocation[0].x,faceLocation[0].y,faceLocation[0].w);
	}else{
		LOG("HHQ: FACE TRACKING getPos %d", faceLocation.size());
	}
	return faceLocation.size();
}

/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    setFaceDetection
 * Signature: (I[F[F[F[F)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeFaceTracking_setFaceDetection(
		JNIEnv *env, jclass clazz, jint len, jintArray xArr, jintArray yArr,
		jintArray wArr, jintArray hArr) {
	jint * arrX = env->GetIntArrayElements(xArr, NULL);
	jint * arrY = env->GetIntArrayElements(yArr, NULL);
	jint * arrW = env->GetIntArrayElements(wArr, NULL);
	jint * arrH = env->GetIntArrayElements(hArr, NULL);

	faces.clear();
	for (int i = 0; i < len; i++) {
		Rect t((int) arrX[i], (int) arrY[i], (int) arrW[i], (int) arrH[i]);
		faces.push_back(t);
	}
	LOG("HHQ: FACE TRACKING setFaceDetection %d, vector Length: %d", len,
			faces.size());

	env->ReleaseIntArrayElements(xArr, arrX, false);
	env->ReleaseIntArrayElements(yArr, arrY, false);
	env->ReleaseIntArrayElements(wArr, arrW, false);
	env->ReleaseIntArrayElements(hArr, arrH, false);
}
/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    getResult
 * Signature: (I[I[I[I[I)V
 */
JNIEXPORT jobjectArray JNICALL Java_robotbase_vision_NativeFaceTracking_getResult
  (JNIEnv *env, jclass clazz){

}
