#include <iostream>
#include <opencv2/opencv.hpp>

#include <vector>
#include <fstream>
using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"
#include "ShapeAlignment.h"

void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<double>> &keypoints, string &train_path);

void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path);

int main() {

    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config.txt");

    const int num_of_landmark = options.getNumOfLandmark();

    bool isTraining = true;
//    if(isTraining){

    // =========================================
    // Training
    const int num_of_training = options.getNumOfTraining();
    vector<Mat_<unsigned char>> images;
    vector<Mat_<double>> keypoints;
    vector<Rect_<int>> bounding_boxes;
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
    string bounding_box_train_path = options.getTrainBoundingBoxPath();
    readBoundingBoxes(num_of_training, bounding_boxes, bounding_box_train_path);
    // -------------- READ KEYPOINTS -------------
    string train_path = options.getTrainKeypointsPath();
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    cout << "Start Training: numOfImages: " << images.size() << endl;

//    visualizeImage(images.at(0), keypoints.at(0), num_of_landmark);

    // --------------- TRAIN FIRST LEVEL --------------
    int first_level = options.getNumOfFirstLevel();
    int second_level = options.getNumOfSecondLevel();
    int feature_per_fern = options.getNumOfFeaturePerFern();
    int num_of_random_pixel = options.getNumOfRandomPixel();

    ShapeAlignment shapeAlignment(first_level, second_level, feature_per_fern, num_of_random_pixel);
    shapeAlignment.addImages(images);
    shapeAlignment.addBoundingBoxes(bounding_boxes);
    shapeAlignment.addKeyPoints(keypoints);
    shapeAlignment.Train();
    shapeAlignment.Save(options.getModelPath());
    shapeAlignment.Load(options.getModelPath());


    waitKey(0);
//    }//isTraining == true

    // =========================================
    // Testing
    cout << "Start Testing" << endl;
    const int num_of_testing = options.getNumOfTesting();
    vector<Mat_<unsigned char>> images_test;
    vector<Mat_<double>> keypoints_test;
    vector<Rect_<int>> bounding_boxes_test;
    const string test_data = options.getDatasetTestPath();

    // -------------- READ IMAGE ---------------
    string img_path_test = "";
    Mat img_data_test;
    for(int i = 0; i < num_of_testing; i++){
        img_path_test = test_data + to_string(i+1) + ".jpg";

        img_data_test = imread(img_path_test, CV_LOAD_IMAGE_GRAYSCALE);
        images_test.push_back(img_data_test);
        cout << "test_img: " << img_path_test << endl;
    }
    // -------------- READ BOUNDING BOX ----------
    string bounding_box_test_path = options.getTestBoundingBoxPath();
    readBoundingBoxes(num_of_testing, bounding_boxes_test, bounding_box_test_path);
    // -------------- READ KEYPOINTS -------------
    string test_path = options.getTestKeypointsPath();
    readKeypoints(num_of_testing, num_of_landmark, keypoints_test, test_path);

    cout << "Start Testing: numOfImages: " << images_test.size() << endl;

//    int first_level = options.getNumOfFirstLevel();
//    int second_level = options.getNumOfSecondLevel();
//    int feature_per_fern = options.getNumOfFeaturePerFern();
//    ShapeAlignment shapeAlignment(first_level, second_level, feature_per_fern);

    for(int i = 0 ; i < images_test.size(); i++){
//        Mat_<double> prediction = shapeAlignment.Test(images_test[i], bounding_boxes_test[i]);
//        visualizeImageCompare(images_test[i], prediction, keypoints_test[i], 10);

        Mat_<double> prediction = shapeAlignment.Test(images[i], bounding_boxes[i]);
        visualizeImageCompare(images[i], prediction, keypoints[i], 10);

        cout << "==============================" << endl;
        cout << "FINISH " << i << endl;
        cout << prediction << endl;
    }

    waitKey(0);
    return 0;
}

void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path) {
    ifstream finBox( bounding_box_train_path , ios_base::in );
    Rect_<int> rect;
    for(int i = 0;i < num_of_training; i++){
        finBox >> rect.x >> rect.y >> rect.width >> rect.height;
        cout << rect << endl;
        bounding_boxes.push_back(rect);
    }
    finBox.close();
}

void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<double>> &keypoints, string &train_path) {
    ifstream finKey( train_path , ios_base::in );
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
}