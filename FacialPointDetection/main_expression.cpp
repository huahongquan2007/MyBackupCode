#include <iostream>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"
#include "ShapeAlignment.h"


int main() {

    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config.txt");
//    Configuration options("/Users/quanhua92/workspace/MyBackupCode/FacialPointDetection/config.txt");
    const int num_of_landmark = options.getNumOfLandmark();
    int first_level = options.getNumOfFirstLevel();
    int second_level = options.getNumOfSecondLevel();
    int feature_per_fern = options.getNumOfFeaturePerFern();
    const int num_of_training = options.getNumOfTraining();
    vector<Mat_<double>> keypoints;
    vector<Rect_<int>> bounding_boxes;

    // -------------- READ BOUNDING BOX ----------
    string bounding_box_train_path = options.getTrainBoundingBoxPath();
    readBoundingBoxes(num_of_training, bounding_boxes, bounding_box_train_path);
    // -------------- READ KEYPOINTS -------------
    string train_path = options.getTrainKeypointsPath();
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

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

    ShapeAlignment shapeAlignmentTest(first_level, second_level, feature_per_fern);
    shapeAlignmentTest.addKeyPoints(keypoints);
    shapeAlignmentTest.addBoundingBoxes(bounding_boxes);
    shapeAlignmentTest.Load(options.getModelPath());

    int start_position = 0;
    for(int i = start_position ; i < images_test.size() + start_position; i++){

        Mat_<double> prediction = shapeAlignmentTest.Test(images_test[i], bounding_boxes_test[i]);
        visualizeImageCompare(images_test[i], prediction, keypoints_test[i], 0);

        cout << "==============================" << endl;
        cout << "FINISH " << i << endl;
        cout << prediction << endl;
    }

    waitKey(0);
    return 0;
}
