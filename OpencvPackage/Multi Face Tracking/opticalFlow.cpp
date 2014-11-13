#include <stdlib.h>
#include <ctime>

#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace std;
using namespace cv;

const int CAMERA_ID = 0;
const int maxCorners = 30;
const int minCorners = 15;
const int maxLoopFindCorners = 10;
const double qualityLevel = 0.01;
const double minDistance = 5;
const TermCriteria termcrit(CV_TERMCRIT_ITER|CV_TERMCRIT_EPS,20,0.03);
const Size subPixWinSize(10,10), winSize(31,31);
const int FACE_LOCATION_LIFESPAN = 2;
/** Global variables */
String face_cascade_name = "/usr/share/opencv/haarcascades/haarcascade_frontalface_alt.xml";
CascadeClassifier face_cascade;

class FaceLocation
{
public:
    int x,y,w,h;
    vector<Point2f> points[2];
    String faceName;
    int meanX, meanY;
    float width, height;  
    time_t lastTime;  
    FaceLocation(){
        faceName = random_string();
        updateTime();
    }
    ~FaceLocation(){

    }
    string random_string( )
    {
        vector<string> a;
        a.push_back("A");
        a.push_back("B");
        a.push_back("C");
        a.push_back("D");
        a.push_back("E");
        a.push_back("F");
        return a[rand() % 6];
    }
    void updateTime(){
        lastTime = time(NULL);
        cout << "UpdateTime " << lastTime << endl;
    }
    double getPeriodTime(){
        time_t curTime = time(NULL);
        double period = difftime(curTime, lastTime);
        // double period = curTime - lastTime;
        cout << "getPeriodTime: lastTime " << lastTime << " curTime " << curTime << " Period " <<  period << endl;
        return period;
    }
    void updateMean(){
        meanX = meanY = 0;
        float minX, maxX , minY, maxY;
        minX = maxX = points[0][0].x;
        minY = maxY = points[0][0].y;
        for( size_t j = 0; j < points[0].size(); j++ )
        {
            minX = (points[0][j].x < minX) ? points[0][j].x : minX;
            maxX = (points[0][j].x > maxX) ? points[0][j].x : maxX;
            minY = (points[0][j].y < minY) ? points[0][j].y : minY;
            maxY = (points[0][j].y > maxY) ? points[0][j].y : maxY;

            if(points[0][j].x < 0 ) cout << "ALERT " << points[0][j].x << endl;
            meanX += (points[0][j].x > 0) ? points[0][j].x : 0;
            meanY += (points[0][j].y > 0) ? points[0][j].y : 0;
        }
        cout <<"before updateMean " << meanX << " " << meanY << endl;
        meanX = meanX / points[0].size();
        meanY = meanY / points[0].size();
        width = maxX - minX;
        height = maxY - minY;
        cout << "updateMean " << meanX << " " << meanY << " Points Size " << points[0].size() << endl;
    }
    float getRatioOutBound(){
        int numOfBound = 0;
        for( size_t j = 0; j < points[0].size(); j++ )
        {
            float cX = points[0][j].x;
            float cY = points[0][j].y;
            if(  (cX > x && cX < x + w) && (cY > y && cY < y + h) ){
                //in box
            }else{
                //out box
                numOfBound++;
            }
        }
        return (float)numOfBound/points[0].size();
    }

};
FaceLocation getFaceLocation(Mat curFaceImg, int x , int y, int width , int height){
    FaceLocation curFaceLocation;
    curFaceLocation.x = x;
    curFaceLocation.y = y;
    curFaceLocation.w = width;
    curFaceLocation.h = height;


    int count = 0;
    while(curFaceLocation.points[0].size() < minCorners){
        count++;
        if(count > maxLoopFindCorners) break;
        goodFeaturesToTrack(curFaceImg, curFaceLocation.points[0], maxCorners, qualityLevel, minDistance);
    }
        
    cornerSubPix(curFaceImg, curFaceLocation.points[0], subPixWinSize, Size(-1,-1), termcrit);
    for (size_t idx = 0; idx < curFaceLocation.points[0].size(); idx++) {
        cv::circle(curFaceImg, curFaceLocation.points[0].at(idx), 3, Scalar(0,255,0), -3, 8);
    }
    Point2f t = Point2f(x , y);
    for (size_t idx = 0; idx < curFaceLocation.points[0].size(); idx++) {
        curFaceLocation.points[0][idx] = curFaceLocation.points[0][idx] + t;
    }
    cout << "getFaceLocation " << endl;
    imshow("oldMask", curFaceImg);
    curFaceLocation.updateMean();
    return curFaceLocation;
}
double calculateDistance(double x1, double x2, double y1, double y2){
    return sqrt( pow((x1 - x2),2.0) + pow((y1 - y2),2.0) );
}

void updateFaceLocation(Mat &frame, Mat curFaceImg, vector<FaceLocation> &faceLocation, int index, float x, float y, float w, float h){
    faceLocation[index].x = x;
    faceLocation[index].y = y;
    faceLocation[index].w = w;
    faceLocation[index].h = h;
    // Draw on frame
    putText(frame, faceLocation[index].faceName, Point(faceLocation[index].x,faceLocation[index].y), FONT_HERSHEY_SIMPLEX, 1.2, Scalar(0,255,0));
    // Neu ratio nam ngoai face box > 50% -> init
    int ratio = faceLocation[index].getRatioOutBound();
    if(ratio > 0.8){
        FaceLocation fl = getFaceLocation(curFaceImg, x, y, w, h);
        std::swap(faceLocation[index].points[0], fl.points[0]);
        cout << "UpdateRatio > 0.9 " << faceLocation[index].meanX << " " << faceLocation[index].meanY << endl;
    }
    faceLocation[index].updateTime();
}
int main()
{
	VideoCapture cap(CAMERA_ID); // open the default camera
    if(!cap.isOpened())  // check if we succeeded
        return -1;
    // cap.set(CV_CAP_PROP_FRAME_WIDTH, 320);
    // cap.set(CV_CAP_PROP_FRAME_HEIGHT, 240);
    // Init variable
    vector<FaceLocation> faceLocation;
    //
    if( !face_cascade.load( face_cascade_name ) ){ cout << "--(!)Error loading\n"; return -1; };

    Mat frame, prevGray, gray, curFaceImg;
    vector<Rect> faces;

    bool needToInit = true;
    clock_t prevTime, curTime1, curTime2, curTime3, curTime4, curTime5;
    for(;;)
    {
        // Time++
        prevTime = std::clock();

        Mat frame;
        cap >> frame; // get a new frame from camera

        cvtColor(frame, gray, CV_BGR2GRAY);
        equalizeHist(gray,gray);

        // Face detection
		face_cascade.detectMultiScale( gray, faces, 1.1, 3, 0|CV_HAAR_SCALE_IMAGE , Size(80, 80) );
        // face_cascade.detectMultiScale( gray, faces, 1.1, 2, 0|CV_HAAR_FIND_BIGGEST_OBJECT , Size(30, 30) );
        // Calc OpticalFlow

        if(prevGray.empty())
        	gray.copyTo(prevGray);

        // Time++
        curTime1 = std::clock();

        cout << "Before loop " << faceLocation.size() << endl;
        // Loop each faceLocation
        for( size_t i = 0; i < faceLocation.size(); i++ )
        {
            // Calculate Optical Flow
            vector<uchar> status;
            vector<float> err;
            calcOpticalFlowPyrLK(prevGray, gray, faceLocation[i].points[0], faceLocation[i].points[1], status, err, winSize, 3, termcrit, 0, 0.001);

            for( size_t j = 0; j < faceLocation[i].points[1].size(); j++ )
            {
                circle( frame, faceLocation[i].points[1][j], 3, Scalar(0,255,0), -1, 8);
            }
            // Old points
            for( size_t j = 0; j < faceLocation[i].points[0].size(); j++ )
            {
                circle( frame, faceLocation[i].points[0][j], 3, Scalar(255,255,0), -1, 8);
            }
            // Save information
            std::swap(faceLocation[i].points[1], faceLocation[i].points[0]);
            faceLocation[i].updateMean();
            circle( frame, Point(faceLocation[i].meanX, faceLocation[i].meanY), 10, Scalar(255,255,255), -1, 8);
        }

        // Time++
        curTime2 = std::clock();

        // Map faceLocation to faces
        for(size_t i = 0 ; i < faces.size(); i++)
        {

            float meanFaceX = faces[i].x + faces[i].width / 2;
            float meanFaceY = faces[i].y + faces[i].height / 2;
            float radius = (faces[i].width + faces[i].height) / 4;
            circle( frame, Point(meanFaceX, meanFaceY), radius, Scalar(125,125,0), 3);
            rectangle(frame, Point(faces[i].x, faces[i].y), Point(faces[i].x + faces[i].width, faces[i].y + faces[i].height), Scalar(0,255,125), 5);
            std::vector<int> indexVector;
            for(size_t j = 0 ; j < faceLocation.size(); j++){
                float distance = calculateDistance(meanFaceX, faceLocation[j].meanX, meanFaceY, faceLocation[j].meanY);
                if(distance < radius){
                    indexVector.push_back(j);
                    cout << "push_back " << j << endl;
                }
                cout << "DISTANCE: " << distance  << " radius " << radius << " meanFaceX,Y " << meanFaceX << " " << meanFaceY << " fLX,Y " << faceLocation[j].meanX << " " << faceLocation[j].meanY <<  endl;
            }
            // Neu khong co faceLocation nao => face moi ==> them faceLocation
            // Neu co >2 faceLocation trong vung tim kiem thi phai kiem tra dac trung cho chinh xac
            // Neu chi co 1 faceLocation thi gan luon
            curFaceImg = gray(faces[i]); 
            cout << "indexVector size : " << indexVector.size() << " faceLocation size: " << faceLocation.size() <<  endl;
            if(indexVector.size() == 1 ){
                int index = indexVector[0];
                cout << "1 faceLocation " << faceLocation[index].faceName << endl;
                updateFaceLocation(frame, curFaceImg, faceLocation, index, faces[i].x, faces[i].y, faces[i].width, faces[i].height);
            }else if(indexVector.size() == 0){
                cout << "0 faceLocation" << endl;
                faceLocation.push_back(getFaceLocation(curFaceImg, faces[i].x, faces[i].y, faces[i].width, faces[i].height));
            }else{
                cout << ">2 faceLocation" << endl;
                int bestIndex = -1;
                float bestValue = -1;
                for(int idx = indexVector.size() - 1 ; idx >= 0 ; idx--){
                    cout << "idx " << idx << endl;  
                    int index = indexVector[idx];
                    cout << "Index : " << index << " idx " << idx << endl;
                    float flWidth = faceLocation[index].width;
                    float flHeight = faceLocation[index].height;
                    cout << "flWidth height : " << flWidth << " " << flHeight << endl;
                    float curValue = abs(faces[i].width - flWidth)*abs(faces[i].height - flHeight);
                    cout << "curValue " << curValue << endl;
                    if( bestValue == -1 || curValue < bestValue ){
                        bestIndex = index;
                        bestValue = curValue;
                    }
                }
                cout << "BestIndex " << bestIndex << " bestValue " << bestValue << endl;
                updateFaceLocation(frame, curFaceImg, faceLocation, bestIndex, faces[i].x, faces[i].y, faces[i].width, faces[i].height);
                cout << "indexVector " << indexVector.size() << endl;
                for(int idx = indexVector.size() - 1 ; idx >= 0 ; idx--){
                    if(idx != bestIndex){
                        cout << "          Erase: " << idx << endl;
                        faceLocation.erase(faceLocation.begin() + idx);
                    }
                }
            }
        }

        // Time++
        curTime3 = std::clock();

        // Remove old faceLocation
        for(int j = faceLocation.size() - 1 ; j >= 0; j--){
            // cout <<"-------------------Before Remove " << j << endl;
            if(faceLocation[j].getPeriodTime() > FACE_LOCATION_LIFESPAN){
                faceLocation.erase(faceLocation.begin() + j);
                cout << "====================================== Remove OLD FL : " << j << endl;
            }
                
        }
        swap(prevGray, gray);

        // Time++
        curTime4 = std::clock();

        cout << "Time1: "  << (curTime1 - prevTime)/(double)(CLOCKS_PER_SEC / 1000)
            << "Time2: " << (curTime2 - curTime1)/(double)(CLOCKS_PER_SEC / 1000)
            << "Time3: " << (curTime3 - curTime2)/(double)(CLOCKS_PER_SEC / 1000)
            << "Time4: " << (curTime4 - curTime3)/(double)(CLOCKS_PER_SEC / 1000)
            << "Total: " << (curTime4 - prevTime)/(double)(CLOCKS_PER_SEC / 1000)
            << endl;

        // End calc Opticalflow
        imshow("curFrame", frame);
        if(waitKey(30) >= 0) break;
    }
    cap.release();
    return 0;
}