#include <iostream>
#include <opencv2/opencv.hpp>
using namespace std;
using namespace cv;

#include "TLD.h"
using namespace tld;

int main() {
    cout << "Vehicle Detection & Tracking!" << endl;

    // Init variable
    VideoCapture cap(0);
    cap.set(CV_CAP_PROP_FRAME_WIDTH, 320);
    cap.set(CV_CAP_PROP_FRAME_HEIGHT, 240);

    Mat frame, frameGray;
    bool isStart = false;
    int min_car_size=20;
    int max_car_size=200;

    CascadeClassifier carDetector;
    carDetector.load("haarcascade_frontalface_alt.xml");


    TLD *tld = new TLD();
    // for each frame
    for(int i = 0 ; i <= i; i++) {

        // Read new frame
        cap >> frame;

        if(i < 100){
            waitKey(10);
            continue;
        }

        if (frame.empty()) {
            std::cerr << "Frame data error.\n";
        }
        cvtColor(frame, frameGray, COLOR_RGB2GRAY);

        // Car detection for initialization
        std::vector<cv::Rect> cars;
        if(!isStart){
            carDetector.detectMultiScale(frameGray, cars, 1.2, 2, 0 | CV_HAAR_SCALE_IMAGE, Size(min_car_size, min_car_size), Size(max_car_size, max_car_size));
        }

        if (cars.size() > 0 && !isStart) {
            isStart = true;
            rectangle(frameGray , cars[0] ,Scalar(0,0,0,255),5);
            imshow("test", frameGray);

            cout << "Car detected: " << cars.size() << endl;
            cout << "Initialize tracker" << endl;

            tld->detectorCascade->imgWidth =frameGray.cols;
            tld->detectorCascade->imgHeight = frameGray.rows;
            tld->detectorCascade->imgWidthStep = frameGray.step;

            tld->selectObject(frameGray,&cars[0]);
        }

        // car tracking
        tld->processImage(frame);
        if (tld->currBB!=NULL)
        {
            Rect r;
            r.x=tld->currBB->x;
            r.y=tld->currBB->y;
            r.width= tld->currBB->width;
            r.height= tld->currBB->height;

            rectangle(frame ,r,Scalar(0,0,255,0),5);
            /*   for(size_t i = 0; i < etld->detectorCascade->detectionResult->fgList->size(); i++)
                            {
                                Rect r = etld->detectorCascade->detectionResult->fgList->at(i);
                                rectangle(mRgb, r, Scalar(255,0,0,0), 1);
                            }
                            */
        }
        else
        {
            Rect r;
            r.x=frame.size().width/2;
            r.y=frame.size().height/2;;
            r.width= 100;
            r.height= 100;
            rectangle(frame ,r,Scalar(0,0,0,255),5);
        }

        // show frame
        imshow("frame", frame);
        // quit for loop
        char key = (char) cv::waitKey(10);
        if (key == 27 || key == 'q' || key == 'Q') // 'ESC'
        {
            cout << "Finish!" << endl;

            break;
        }
    }

    cap.release();

    return 0;
}