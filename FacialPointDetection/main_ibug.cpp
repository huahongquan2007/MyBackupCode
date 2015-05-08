#include <iostream>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"
#include "ShapeAlignment.h"


int main() {

    // ========================================
    // Test multi Mat_<float>
//    cv::Mat mean = cv::Mat::zeros(1,1,CV_64FC1);
//    cv::Mat sigma= cv::Mat::ones(1,1,CV_64FC1);
//    cv::RNG rng;
//    cv::Mat_<float> a(5,5,CV_32FC1);
//    cv::Mat_<float> b(5,5,CV_32FC1);
//    rng.fill(a, cv::RNG::NORMAL, mean, sigma);
//    rng.fill(b, cv::RNG::NORMAL, mean, sigma);
//
//    int64 t0 = getTickCount();
//    Mat_<float> c = a * b;
//    int64 t1 = getTickCount();
//    cout << c << endl;
//    cout << "Time opencv: " << (t1 - t0) / getTickFrequency() << endl;
//
//    {
//        int64 max = 1;
//        int64 t2 = getTickCount();
//        Mat_<float> c1(5, 5, CV_32SC1);
//        for(int i = 0 ; i < 5 ; i ++){
//            for(int j = 0 ; j < 5 ; j++){
//                float t = 0;
//                for(int k = 0 ; k < 5 ; k++){
//                    t += a.at<float>(i, k) * b.at<float>(k, j) ;
//                }
//
//                c1.at<float>(i, j) = t;
////            c1.at<float>(i, j) = ( a.at<float>(i, j) * max ) * ( b.at<float>(i, j) * max ) / max / max;
//            }
//        }
//
//        int64 t3 = getTickCount();
//        cout << c1 << endl;
//        cout << "Time float: " << (t3 - t2) / getTickFrequency() << endl;
//    }
//    int64 max = 1;
//    int64 t2 = getTickCount();
//    Mat_<float> c1(5, 5, CV_32SC1);
//    for(int i = 0 ; i < 5 ; i ++){
//        for(int j = 0 ; j < 5 ; j++){
//            float t = 0;
//            for(int k = 0 ; k < 5 ; k++){
//                t += heuristicMult(a.at<float>(i, k) , b.at<float>(k, j) );
//            }
//
//            c1.at<float>(i, j) = t;
////            c1.at<float>(i, j) = ( a.at<float>(i, j) * max ) * ( b.at<float>(i, j) * max ) / max / max;
//        }
//    }
//
//    int64 t3 = getTickCount();
//    cout << c1 << endl;
//    cout << "Time int64: " << (t3 - t2) / getTickFrequency() << endl;
//
//    return -1;

    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
//    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config_ibug.txt");
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config_ibug_reduce.txt");
//    Configuration options("/Users/quanhua92/workspace/MyBackupCode/FacialPointDetection/config_ibug_reduce.txt");
    const int num_of_landmark = options.getNumOfLandmark();

    cout << "Num of landmark: " << num_of_landmark << endl;
    // =========================================
    // Training
    const int num_of_training = options.getNumOfTraining();
    vector<Mat_<unsigned char>> images;
    vector<Mat_<float>> keypoints;
    vector<Rect_<int>> bounding_boxes;
    const string train_data = options.getDatasetTrainPath();

    // -------------- READ IMAGE ---------------
    string img_path = "";
    Mat img_data;
    float EXPECT_NEED = 500;
    vector<float> scaleImage;
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


        scaleImage.push_back(scale);

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
    vector<Mat_<float>> keypoints_test;
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

        int64 t0 = getTickCount();
        Mat_<float> prediction = shapeAlignmentTest.Test(images[i], bounding_boxes[i]);

        int64 t1 = getTickCount();



        visualizeImageCompare(images[i], prediction, keypoints[i], 0);

        cout << "==============================" << endl;
        cout << "FINISH " << i << " in " << (t1 - t0) / getTickFrequency() << endl;
        cout << prediction.t() << endl;
    }

    waitKey(0);
    return 0;
}
