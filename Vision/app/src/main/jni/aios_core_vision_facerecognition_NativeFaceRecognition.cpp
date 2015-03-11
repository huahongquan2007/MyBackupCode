#include "aios_core_vision_facerecognition_NativeFaceRecognition.h"
#include "aios_core_vision_utils.h"

#include <vector>
using namespace std;

#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;

#include "json/json.h"
#include <android/log.h>

#define LOG_TAG    "VisionNative-FaceRec"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

const bool isDebug = false;

int IMG_WIDTH = 480;
int IMG_HEIGHT = 640;
jstring result;

// FaceBatch Constant
static int MAX_IMAGE_PER_FACEBATCH = 6;
static int DELAY_IMAGE_ADD_FACEBATCH = 100;
static int MAX_TIME_WAIT_FOR_IMAGE = 5000;
static int MAX_TIME_WAIT_FOR_RESULT = 50000;
static int FACEBATCH_IMG_WIDTH = 200;
static int FACEBATCH_IMG_HEIGHT = 200;

typedef enum {
    PENDING,
    SUCCESS
} INFO_STATE;

class FaceRecognitionInfo{
public:
    string face_id;
    string face_name;
    Mat face_img;
    double lastTimeRequest;
    INFO_STATE face_state;
    FaceRecognitionInfo(string id, string name, Mat img){
        face_id = id;
        face_name = name;
        face_img = img;
        face_state = PENDING;
    }
    void setName( String name ){
        face_name = name;
        face_state = SUCCESS;
    }
    INFO_STATE getState(){
        return face_state;
    }
    void requestServer(JNIEnv * env, jobject serviceObject){

        int len = face_img.total() * face_img.channels();
        jbyteArray data = env->NewByteArray( len );
        LOG("LEN : %d", len);
        env->SetByteArrayRegion( data, 0, len, (jbyte*) face_img.data );

        jclass stringClass = env->GetObjectClass(serviceObject);
        jmethodID methodId = env->GetMethodID(stringClass, "sendServer", "(Ljava/lang/String;II[B)V");
        env->CallVoidMethod(serviceObject, methodId, env->NewStringUTF(face_id.c_str()), face_img.cols, face_img.rows, data);

        lastTimeRequest = now_ms();
        if(isDebug) LOG("SEND REQUEST %s AND WAIT FOR %d", face_id.c_str(), MAX_TIME_WAIT_FOR_RESULT);
    }
    bool expire(){
        if(now_ms() - lastTimeRequest > MAX_TIME_WAIT_FOR_RESULT){
            if(isDebug) LOG("EXPIRE RETURN TRUE %f %s", now_ms() - lastTimeRequest, face_id.c_str());
            return true;
        }
    }
};
class FaceBatch{
    vector<Mat> listMat;
    double lastTimeFaceAdd;
    int x, y , w , h;
public:
    string face_id;
    FaceBatch(string id){
        lastTimeFaceAdd = 0;
        face_id = id;
    }
    ~FaceBatch(){
    // to do: release Mat in listMat
    }
    void addFace(Mat img){
        if( lastTimeFaceAdd > 0 && now_ms() - lastTimeFaceAdd < DELAY_IMAGE_ADD_FACEBATCH){
            return;
        }
        Mat resizeImg;
        resize(img, resizeImg, Size(FACEBATCH_IMG_WIDTH, FACEBATCH_IMG_HEIGHT));
        listMat.push_back(resizeImg);
        lastTimeFaceAdd = now_ms();
        if(needMore() == false){
            // full
            if(isDebug) LOG("Batch %s complete", face_id.c_str());
        } else {
            if(isDebug) LOG("Batch %s add Face. Size: %d", face_id.c_str(), listMat.size());
        }
    }

    void updatePosition(int tx, int ty, int tw, int th){
        x = tx;
        y = ty;
        w = tw;
        h = th;
    }
    bool needMore(){
        // check if we need add more image
        if(listMat.size() < MAX_IMAGE_PER_FACEBATCH)
            return true;
        return false;
    }
    bool expire(){
        // check if current time is too large
        if(needMore() && now_ms() - lastTimeFaceAdd > MAX_TIME_WAIT_FOR_IMAGE){
            if(isDebug) LOG("EXPIRE RETURN TRUE %f %s", now_ms() - lastTimeFaceAdd, face_id.c_str());
            return true;
        }
        if(isDebug) LOG("EXPIRE RETURN FALSE %f %s", now_ms() - lastTimeFaceAdd, face_id.c_str());
        return false;
    }
    Mat combine(){
        int imgPerRow = MAX_IMAGE_PER_FACEBATCH / 2;
        Mat row1;
        Mat row2;
        Mat final;

        try{
            vector<Mat> r1( listMat.begin(), listMat.begin() + imgPerRow);
            hconcat(r1, row1);

            vector<Mat> r2( listMat.begin() + imgPerRow, listMat.end());
            hconcat(r2, row2);

            vconcat(row1, row2, final);
        }catch( char * e ){
            LOG("ERROR: %s", e);
        }

        return final;
    }
};

// global variable
vector<FaceRecognitionInfo> faceRecInfoVector;
vector<FaceBatch> faceBatchVector;


JNIEXPORT jstring JNICALL Java_aios_core_vision_facerecognition_NativeFaceRecognition_update
  (JNIEnv * env, jclass clazz, jobject serviceObject, jbyteArray frameArray, jint width, jint height, jstring faceTrackingJSONString, jstring faceRecJSONString){

    Json::Reader jsonReader;
    bool parseSuccessful;
  // ----------------------------------------
  // if faceTracking isEmpty => return ""
    const char *faceTrackingChar = env->GetStringUTFChars(faceTrackingJSONString,0);
    string faceTrackingString(faceTrackingChar);

    Json::Value jsonFTResult;

    parseSuccessful = jsonReader.parse(faceTrackingString, jsonFTResult);
    if( parseSuccessful && jsonFTResult.size() == 0) parseSuccessful = false;

    if( parseSuccessful == false){
        LOG("Parse Error");
        return env->NewStringUTF("");
    }
    env->ReleaseStringUTFChars(faceTrackingJSONString, faceTrackingChar);
    // ----------------------------------------
    // parse faceRecJSONString
    const char *faceRecChar = env->GetStringUTFChars(faceRecJSONString,0);
    string faceRecString(faceRecChar);

    Json::Value jsonFRResult;
    parseSuccessful = jsonReader.parse(faceRecString, jsonFRResult);
    if( parseSuccessful ){
        if(isDebug) LOG("PARSE FACE REC OK %d %s", jsonFRResult.size(), faceRecChar);
    }

    // update faceRecInfo vector
    string face_id, face_name;
    for (int i = 0 ; i < jsonFRResult.size(); i++){
      face_id = jsonFRResult[i]["face_id"].asString();
      face_name = jsonFRResult[i]["face_name"].asString();

      // check if cur_id exists in faceRecInfoVector
      for(int j = 0 ; j < faceRecInfoVector.size() ; j ++){
        if(faceRecInfoVector[j].face_id == face_id){
           faceRecInfoVector[j].setName( face_name );

           break;
        }
      }
    }

    env->ReleaseStringUTFChars(faceRecJSONString, faceRecChar);
    // ----------------------------------------
    // convert RGB to GRAY

    jbyte * pFrameData = env->GetByteArrayElements(frameArray, 0);
    Mat mRgb(height, width, CV_8UC3, (unsigned char *) pFrameData);

//    Mat mGray;
//    cvtColor(mRgb, mGray, CV_RGB2GRAY);

    float scale_w = float(IMG_WIDTH) / mRgb.cols;
    float scale_h = float(IMG_HEIGHT) / mRgb.rows;
    resize(mRgb, mRgb, Size(), scale_w, scale_h);

//    equalizeHist(mGray, mGray);

    // ----------------------------------------
    // Extract Face Detection Result
    int x, y , w, h;
    string cur_id;
    if(isDebug) LOG("-----------------");
    for (int i = 0 ; i < jsonFTResult.size(); i++){
      cur_id = jsonFTResult[i]["face_id"].asString();
      x = jsonFTResult[i]["position"][0].asInt() * scale_w;
      y = jsonFTResult[i]["position"][1].asInt() * scale_h;
      w = jsonFTResult[i]["position"][2].asInt() * scale_w;
      h = jsonFTResult[i]["position"][3].asInt() * scale_h;


      // check if cur_id exists in faceRecInfoVector
      int recInfo = -1;

      for(int j = 0 ; j < faceRecInfoVector.size() ; j ++){
        if(faceRecInfoVector[j].face_id == cur_id){
           recInfo = j;
           break;
        }
      }

      if(recInfo >= 0){
        if(isDebug) LOG("Found %s in faceRecInfoVector. Index: %d", cur_id.c_str() , recInfo);
        continue;
      }else{
        if(isDebug) LOG("Not Found %s in faceRecInfoVector. Size %d", cur_id.c_str() , faceRecInfoVector.size());
      }
      // check if cur_id exists in faceBatchVector
      // true:
      //     - Add if faceBatch need images
      //     - Update position
      // false:
      //     - Add new faceBatch
      //     - Add images
      //     - Update position
      int batch_id = -1;
      for(int j = 0 ; j < faceBatchVector.size() ; j ++){
         if(faceBatchVector[j].face_id == cur_id){
            batch_id = j;
            break;
         }
      }

      if(batch_id == -1){
        // not found
        if(isDebug) LOG("Not found. Add new batch %s", cur_id.c_str());
        FaceBatch batch(cur_id);
        faceBatchVector.push_back(batch);
        batch_id = faceBatchVector.size() - 1;
      } else {
        // found
        if(isDebug) LOG("Found batch %s", cur_id.c_str());
      }
      faceBatchVector[batch_id].updatePosition(x, y, w, h);
      if(faceBatchVector[batch_id].needMore()){
        Rect roi( x, y, w, h );
        Mat sub (mRgb, roi);
        //faceBatchVector[batch_id].addFace(mRgb.rowRange(y, y + h).colRange(x, x + w));
        faceBatchVector[batch_id].addFace( sub );
      }


      if(isDebug) LOG("FACE POSITION: %d %d %d %d %s", x, y , w , h, cur_id.c_str());
    }

    if(isDebug) LOG("-----------------");

    // Send completed faceBatch to server
    for(int i = faceBatchVector.size() - 1 ; i >= 0 ; i--)
    {
        if(faceBatchVector[i].needMore() == false){
            if(isDebug) LOG("Send batch %s", faceBatchVector[i].face_id.c_str());
            FaceRecognitionInfo faceInfo( faceBatchVector[i].face_id, "pending", faceBatchVector[i].combine()  );
            faceRecInfoVector.push_back(faceInfo);
            faceBatchVector.erase( faceBatchVector.begin() + i );

            faceRecInfoVector.back().requestServer(env, serviceObject);
        }
    }

    // remove old batch
    for(int i = faceBatchVector.size() - 1 ; i >= 0 ; i--)
    {
        if(faceBatchVector[i].expire()){
            faceBatchVector.erase( faceBatchVector.begin() + i );
            if(isDebug) LOG("Remove batch %d %s", i, faceBatchVector[i].face_id.c_str());
        }
    }
    // Clear var:
    mRgb.release();
//    mGray.release();
    env->ReleaseByteArrayElements(frameArray, pFrameData, 0);
    // -------------------------
    // create result String based on jsonFTResult and recInfoVector
    for (int i = 0 ; i < jsonFTResult.size(); i++){
          cur_id = jsonFTResult[i]["face_id"].asString();
          x = jsonFTResult[i]["position"][0].asInt() / scale_w;
          y = jsonFTResult[i]["position"][1].asInt() / scale_h;
          w = jsonFTResult[i]["position"][2].asInt() / scale_w;
          h = jsonFTResult[i]["position"][3].asInt() / scale_h;
    }
    // -----------------------------------------
    // generate final result to broadcast
    Json::Value jsonResult;
    if(jsonFTResult.size() > 0 && faceRecInfoVector.size() > 0 ){
        for (int i = 0 ; i < jsonFTResult.size(); i++){
            string face_name = "";
            string cur_id = jsonFTResult[i]["face_id"].asString();


            for(int j = 0 ; j < faceRecInfoVector.size() ; j ++){
              if(faceRecInfoVector[j].face_id == cur_id){
                   if(faceRecInfoVector[j].getState() == SUCCESS){
                        face_name = faceRecInfoVector[j].face_name;
                   }
                   break;
              }
            }

            if(face_name == "")
                continue;
            Json::Value jsonObject;
            jsonObject["time"] = now_ms();
            jsonObject["face_id"] = cur_id;
            jsonObject["face_name"] = face_name;
            jsonObject["position"][0] = jsonFTResult[i]["position"][0].asInt() / scale_w;
            jsonObject["position"][1] = jsonFTResult[i]["position"][1].asInt() / scale_h;
            jsonObject["position"][2] = jsonFTResult[i]["position"][2].asInt() / scale_w;
            jsonObject["position"][3] = jsonFTResult[i]["position"][3].asInt() / scale_h;

            jsonResult[i] = jsonObject;
        }

        const char* json_str = jsonResult.toStyledString().c_str();
        if( jsonResult.size() > 0 ){
            result = env->NewStringUTF(json_str);
            if(isDebug) LOG("Final Result: %s" , json_str );
            return result;
        }
    }
    if(isDebug) LOG("Final Result: NOT FOUND ANYTHING");
    result = env->NewStringUTF("");

    return result;
}