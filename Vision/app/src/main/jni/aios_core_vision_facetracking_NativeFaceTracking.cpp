#include "aios_core_vision_facetracking_NativeFaceTracking.h"
#include "aios_core_vision_utils.h"
#include <stdio.h>
#include <cstring>
#include <string>
#include <memory>
#include <vector>
using namespace std;

#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;

#include "json/json.h"
#include <android/log.h>

#define LOG_TAG    "VisionNative-FaceTrack"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

const bool isDebug = false;

int IMG_WIDTH = 240;
int IMG_HEIGHT = 320;
jstring result;


// Variable for tracking
const int maxCorners = 30;
const int minCorners = 15;
const int maxLoopFindCorners = 10;
const double qualityLevel = 0.02;
const double minDistance = 3;
const TermCriteria termcrit(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.03);
const Size subPixWinSize(10, 10), winSize(31, 31);
const int FACE_LOCATION_LIFESPAN = 2;

// Global variable
class FaceLocation;

Mat prevGray;
vector<FaceLocation> faceLocation;
vector<Rect> faces;

class FaceLocation {
public:
	int x, y, w, h;
	vector<Point2f> points[2];
	string faceName;
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
		int len = 20;
		string a = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		string r;
		for(int i = 0; i < len; i++) r.push_back(a.at(size_t(rand() % 62)));
		return r;
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

	try{
		int count = 0;
		while (curFaceLocation.points[0].size() < minCorners) {
			count++;
			if (count > maxLoopFindCorners)
				break;
			goodFeaturesToTrack(curFaceImg, curFaceLocation.points[0], maxCorners,
					qualityLevel, minDistance);
		}
		if(isDebug)
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
	}
	catch(cv::Exception &e){
		LOG("HHQ: FACE TRACKING ERROR EXCEPTION");
	}

	return curFaceLocation;
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
JNIEXPORT jstring JNICALL Java_aios_core_vision_facetracking_NativeFaceTracking_update
  (JNIEnv *env, jclass clazz, jbyteArray frameArray, jint width, jint height, jstring faceDetectionJSONString){
    try {
        // ----------------------------------------
        // if faceDetection isEmpty => return ""
        const char *faceDetectionChar = env->GetStringUTFChars(faceDetectionJSONString,0);
        string faceDetectionString(faceDetectionChar);

        Json::Value jsonFDResult;
        Json::Reader jsonReader;
        bool parseSuccessful = jsonReader.parse(faceDetectionString, jsonFDResult);
        if( parseSuccessful ){
            if(isDebug) LOG("Parse OK %d", jsonFDResult.size());
            if(jsonFDResult.size() == 0)
                parseSuccessful = false;
        }

        if( parseSuccessful == false){
            LOG("Parse Error");
            return env->NewStringUTF("");
        }

        // ----------------------------------------
        // convert RGB to GRAY

        jbyte * pFrameData = env->GetByteArrayElements(frameArray, 0);
        Mat mRgb(height, width, CV_8UC3, (unsigned char *) pFrameData);

        Mat mGray;
        cvtColor(mRgb, mGray, CV_RGB2GRAY);

        float scale_w = float(IMG_WIDTH) / mGray.cols;
        float scale_h = float(IMG_HEIGHT) / mGray.rows;
        resize(mGray, mGray, Size(), scale_w, scale_h);
        equalizeHist(mGray, mGray);

        // ----------------------------------------
        // Extract Face Detection Result
        faces.clear();

        int x, y , w, h;
        if(isDebug) LOG("-----------------");
        for (int i = 0 ; i < jsonFDResult.size(); i++){
            x = jsonFDResult[i]["position"][0].asInt();
            y = jsonFDResult[i]["position"][1].asInt();
            w = jsonFDResult[i]["position"][2].asInt();
            h = jsonFDResult[i]["position"][3].asInt();
            Rect t((int) x * scale_w, (int) y * scale_h, (int) w * scale_w, (int) h * scale_h);
            faces.push_back(t);
            if(isDebug) LOG("FACE POSITION: %d %d %d %d", x, y , w , h);
        }
        if(isDebug) LOG("-----------------");

        // -------------------------------------------
        // Use optical flow to update new location of old faces
        if (prevGray.empty())
        		mGray.copyTo(prevGray);
        // Loop each faceLocation
        for (int i = 0; i < faceLocation.size(); i++) {
            // Calculate Optical Flow
            vector<uchar> status;
            vector<float> err;
            calcOpticalFlowPyrLK(prevGray, mGray, faceLocation[i].points[0],
                    faceLocation[i].points[1], status, err, winSize, 3, termcrit, 0,
                    0.001);

            // Save information
            std::swap(faceLocation[i].points[1], faceLocation[i].points[0]);
            faceLocation[i].updateMean();
        }
        // -------------------------------------------
        // Use faceLocation to update faces. Add new face if can't map location and face
        Mat curFaceImg;
        for (int i = 0; i < faces.size(); i++) {

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
            curFaceImg = mGray(faces[i]);

            // Check size of curFaceImg
            if(curFaceImg.rows < winSize.height || curFaceImg.cols < winSize.width)
                break;

            if (indexVector.size() == 1) {
                int index = indexVector[0];
                if(isDebug) LOG( "1 faceLocation %s " , faceLocation[index].faceName.c_str());
                updateFaceLocation(mGray, curFaceImg, faceLocation, index,
                        faces[i].x, faces[i].y, faces[i].width, faces[i].height);
            } else if (indexVector.size() == 0) {
                if(isDebug) LOG( "0 faceLocation" );
                faceLocation.push_back(
                        getFaceLocation(curFaceImg, faces[i].x, faces[i].y,
                                faces[i].width, faces[i].height));
            }
            else {
                LOG( ">2 faceLocation" );
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
                updateFaceLocation(mGray, curFaceImg, faceLocation, bestIndex,
                        faces[i].x, faces[i].y, faces[i].width, faces[i].height);
                cout << "indexVector " << indexVector.size() << endl;
                for (int idx = indexVector.size() - 1; idx >= 0; idx--) {
                    if (idx != bestIndex) {
                        cout << "  Erase: " << idx << endl;
                        faceLocation.erase(faceLocation.begin() + idx);
                    }
                }
            }
            indexVector.clear();
        }
        //----------------------------------------
        // Remove old faceLocation
        for (int j = faceLocation.size() - 1; j >= 0; j--) {
            // cout <<"-------------------Before Remove " << j << endl;
            if (faceLocation[j].getPeriodTime() > FACE_LOCATION_LIFESPAN) {
                faceLocation.erase(faceLocation.begin() + j);

                if(isDebug)
                    LOG("Position %d Remove OLD", j);
                cout << "====================================== Remove OLD FL : "
                        << j << endl;
            }else{
                if(isDebug)
                    LOG("Position %d NOT OLD. Last seen %f ago", j, faceLocation[j].getPeriodTime());
            }

        }
        swap(prevGray, mGray);

        // -----------------------------------------
        // generate final result to broadcast
        Json::Value jsonResult;
        if(faceLocation.size() > 0){
            for (int i = 0; i < faceLocation.size(); i++) {
                Json::Value jsonObject;
                jsonObject["time"] = now_ms();
                jsonObject["face_id"] = faceLocation[i].faceName.data();
                jsonObject["position"][0] = faceLocation[i].x / scale_w;
                jsonObject["position"][1] = faceLocation[i].y / scale_h;
                jsonObject["position"][2] = faceLocation[i].w / scale_w;
                jsonObject["position"][3] = faceLocation[i].h / scale_h;

                jsonResult[i] = jsonObject;
            }


            const char* json_str = jsonResult.toStyledString().c_str();
            result = env->NewStringUTF(json_str);
            if(isDebug) LOG("Final Result: %s" , json_str );
        } else {
            if(isDebug) LOG("Final Result: NOT FOUND ANYTHING");
            result = env->NewStringUTF("");
        }

      // Clear var:
        mRgb.release();
	    mGray.release();
        env->ReleaseByteArrayElements(frameArray, pFrameData, 0);
    } catch (char* e) {
        LOG("FaceTracking Error %s", e);
    }

    // remember to set result = "" if don't want to broadcast
    return result;
}