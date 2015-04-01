#include <iostream>
#include <fstream>

#include "opencv2/opencv.hpp"
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

int main( int argc, const char** argv )
{
    cout << "Test Haar" << endl;

    CascadeClassifier detector("/home/robotbase/VehicleDetectionTrain/haar/cascade.xml");

    if(detector.empty()){
        cerr << "The model could not be loaded." << endl;
    }

    Mat current_image, grayscale, equalize, resize_image;
    float IMG_WIDTH = 640;
    float IMG_HEIGHT = 480;
    ifstream file ("/home/robotbase/DataDrive/Dataset/motorway/dataset/tmeMotorwayDataset_daylight/tme08/test.txt");

    std::string str;


    // write avi
    VideoWriter outputVideo;                                        // Open the output
    outputVideo.open("/home/robotbase/VehicleDetectionTrain/video.avi", CV_FOURCC('P','I','M','1'), 24, Size(1024, 768), true);

    int count = 0;

    while (std::getline(file, str))
    {
        count++;
        // Process str
        cout << count << " " << str ;
        current_image = imread("/home/robotbase/DataDrive/Dataset/motorway/dataset/tmeMotorwayDataset_daylight/tme08/Right/" + str);
        cvtColor(current_image, grayscale, CV_BGR2GRAY);

        grayscale = grayscale.rowRange(current_image.rows / 4, current_image.rows - current_image.rows / 4);

        float scale_w(1), scale_h(1);
//        float scale_w = IMG_WIDTH / grayscale.cols;
//        float scale_h = IMG_HEIGHT/ grayscale.rows;

        cv::resize(grayscale, resize_image, Size(), scale_w, scale_h);
//        resize(grayscale, resize, Size() );
        equalizeHist(resize_image, equalize);

        // Perform detection
        // Retrieve detection certainty scores
        // Perform non maxima suppression
        vector<Rect> objects;
        detector.detectMultiScale(equalize, objects, 1.1, 3, 0, Size(24, 24));

        Rect curPos;
        for(Rect object : objects){

            curPos.x = object.x / scale_w ;
            curPos.y = object.y / scale_h + current_image.rows / 4;
            curPos.width = object.width / scale_w;
            curPos.height = object.height / scale_h;
            rectangle(current_image, curPos, (255, 255, 255), 4);
        }

        imshow("img", current_image);
        outputVideo << current_image;
        cout << " DETECT: " << objects.size() << endl;
        waitKey(2);

    }

//    current_image = imread("/home/robotbase/DataDrive/Dataset/motorway/dataset/tmeMotorwayDataset_daylight/tme08/Right/011131-R.png");


}