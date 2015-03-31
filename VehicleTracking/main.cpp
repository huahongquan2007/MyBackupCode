#include <iostream>
#include <opencv2/opencv.hpp>

#include "CMT.h"

using namespace std;
using namespace cv;
int main() {
    cout << "Vehicle Detection & Tracking!" << endl;

    // Init variable
    VideoCapture cap(0);
    cap.set(CV_CAP_PROP_FRAME_WIDTH, 800);
    cap.set(CV_CAP_PROP_FRAME_HEIGHT, 600);

    Mat frame, frameGray;
    bool isStart = false;
    int min_car_size=20;
    int max_car_size=200;

    CascadeClassifier carDetector;
    carDetector.load("haarcascade_frontalface_alt.xml");

    CMT cmt;
    // for each frame
    for(int i = 0 ; i <= i; i++) {

        // Read new frame
        cap >> frame;

        if(i < 100){
            waitKey(5);
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
            imshow("test", frameGray);

            cout << "Car detected: " << cars.size() << endl;
            cout << "Initialize tracker" << endl;

            Point2f topLeft(cars.front().x, cars.front().y);
            Point2f bottomRight(cars.front().x + cars.front().width, cars.front().y + cars.front().height);

            imshow("test", frameGray);
            cmt.initialise(frameGray, topLeft, bottomRight);
        }

        if(isStart){
            // Car tracking
            cmt.processFrame(frameGray);

            // Draw keypoints & tracked box
            for (int i = 0; i < cmt.trackedKeypoints.size(); i++)
                cv::circle(frame, cmt.trackedKeypoints[i].first.pt, 3, cv::Scalar(255, 255, 255));
            cv::line(frame, cmt.topLeft, cmt.topRight, cv::Scalar(255, 255, 255));
            cv::line(frame, cmt.topRight, cmt.bottomRight, cv::Scalar(255, 255, 255));
            cv::line(frame, cmt.bottomRight, cmt.bottomLeft, cv::Scalar(255, 255, 255));
            cv::line(frame, cmt.bottomLeft, cmt.topLeft, cv::Scalar(255, 255, 255));
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