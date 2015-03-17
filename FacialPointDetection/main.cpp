#include <iostream>
#include <opencv2/opencv.hpp>

#include <vector>
#include <fstream>
using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"

int main() {
    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config.txt");

    // =========================================
    // Training
    const int num_of_training = options.getNumOfTraining();
    const int num_of_landmark = options.getNumOfLandmark();
    vector<Mat_<unsigned char>> images;
    vector<Mat_<double>> keypoints;
    vector<Rect_<int>> bounding_boxs;
    const string train_data = options.getDatasetTrainPath();

    // -------------- READ IMAGE ---------------
    string img_path = "";
    Mat img_data;
    for(int i = 0; i < num_of_training; i++){
        img_path = train_data + to_string(i+1) + ".jpg";

        img_data = imread(img_path, CV_LOAD_IMAGE_GRAYSCALE);
        images.push_back(img_data);
        cout << "train_img: " << img_path << endl;
    }

    // -------------- READ BOUNDING BOX ----------
    ifstream finBox( options.getTrainBoundingBoxPath() , ifstream::in );
    Rect_<int> rect;
    for(int i = 0;i < num_of_training; i++){
        finBox >> rect.x >> rect.y >> rect.width >> rect.height;
        cout << rect << endl;
        bounding_boxs.push_back(rect);
    }
    finBox.close();
    // -------------- READ KEYPOINTS -------------
    ifstream finKey( options.getTrainKeypointsPath() , ifstream::in );
    for(int i = 0;i < num_of_training; i++){
        Mat_<double> temp(num_of_landmark,2);
        double val;
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<double>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<double>(j, 1) = val;
        }
        keypoints.push_back(temp);
    }
    finKey.close();

    cout << "Start Training: numOfImages: " << images.size() << endl;

    visualizeImage(images.at(0), keypoints.at(0), num_of_landmark);




    // =========================================
    // Testing
    cout << "Start Testing" << endl;

    return 0;
}