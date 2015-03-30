#include <iostream>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;
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




    // for each frame
    for(;;){
        // Read new frame
        cap >> frame;
        cvtColor(frame, frameGray, COLOR_RGB2GRAY);

        // Car detection for initialization
        std::vector<cv::Rect> cars;
        carDetector.detectMultiScale( frameGray, cars, 1.2, 2, 0|CV_HAAR_SCALE_IMAGE, Size(min_car_size, min_car_size),Size(max_car_size, max_car_size) );


        if( cars.size() > 0 && !isStart ){
            isStart = true;
            cout << "Car detected: " << cars.size() << endl;
            cout << "Initialize tracker" << endl;
        }
        // Car tracking


        // show frame
        imshow("frame", frame);
        // quit for loop
        char key = (char)cv::waitKey(10);
        if( key == 27 || key == 'q' || key == 'Q' ) // 'ESC'
        {
            cout << "Finish!" << endl;
            break;
        }
    }




    return 0;
}