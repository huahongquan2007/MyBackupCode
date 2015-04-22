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
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config_ibug.txt");
//    Configuration options("/Users/quanhua92/workspace/MyBackupCode/FacialPointDetection/config.txt");
    const int num_of_landmark = options.getNumOfLandmark();

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
    float EXPECT_NEED = 500;
    vector<float> scaleImage;                              
    ifstream finBox( "/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/IBUG/images.txt" , ios_base::in );
    for(int i = 0;i < num_of_training; i++){
        string img_path;
        finBox >> img_path;
        img_data = imread(img_path, CV_LOAD_IMAGE_GRAYSCALE);

        float scale = 1.0;

        cout << img_data.size() << endl;
        if(img_data.cols > EXPECT_NEED){
            scale = EXPECT_NEED / img_data.cols;
            cout << "New size: " << Size( (int) (scale * img_data.cols), (int)(scale * img_data.rows)) << endl;
            cout << "Size: " << img_data.size() << " ";
            resize(img_data, img_data, Size( (int) (scale * img_data.cols), (int)(scale * img_data.rows)) );
            cout << img_data.size() << endl;
        }


        scaleImage.push_back(scale);                                                                                                                                                                                                                                                

        images.push_back(img_data);
        cout << "train_img: " << img_path << endl;
    }
    finBox.close();

    // -------------- READ BOUNDING BOX ----------
    string bounding_box_train_path = options.getTrainBoundingBoxPath();
    readBoundingBoxes(num_of_training, bounding_boxes, bounding_box_train_path);
    // -------------- READ KEYPOINTS -------------
    string train_path = options.getTrainKeypointsPath();
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    // -------------- SCALE BOX , Keypoints ----------
    for(int i = 0 ; i < num_of_training ; i++){
        keypoints[i] = scaleImage[i] * keypoints[i];
        bounding_boxes[i].x = scaleImage[i] * bounding_boxes[i].x;
        bounding_boxes[i].y = scaleImage[i] * bounding_boxes[i].y;
        bounding_boxes[i].width = scaleImage[i] * bounding_boxes[i].width;
        bounding_boxes[i].height = scaleImage[i] * bounding_boxes[i].height;
        //cout << images[i].size() << endl;
        //visualizeImage(images[i], keypoints[i], 0 , false, "after scale");
    }                                                                                                                                                                                                                                                                                           

    cout << "Start Training: numOfImages: " << images.size() << endl;

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

    int start_position = 100;
    for(int i = start_position ; i < images_test.size() + start_position; i++){

        Mat_<double> prediction = shapeAlignmentTest.Test(images[i], bounding_boxes[i]);
        visualizeImageCompare(images[i], prediction, keypoints[i], 0);

        cout << "==============================" << endl;
        cout << "FINISH " << i << endl;
        cout << prediction << endl;
    }

    waitKey(0);
    return 0;
}
