#include <iostream>
#include <opencv2/opencv.hpp>

#include <vector>
#include <fstream>
using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"
#include "ShapeAlignment.h"

//void test(){
//    cv::Size img_size(500, 500);
//    cv::Mat img = cv::Mat::zeros(img_size, CV_8UC3);
//
//    int rand_num = 10;
//    cv::Mat_<float> points(rand_num, 3);
//    cv::Mat src_points(points, cv::Rect(0,0,2,rand_num));
//    cv::randu(points, cv::Scalar(100), cv::Scalar(400));
//    for(int i=0; i<rand_num; ++i) {
//        points(i, 2) = 1.0;
//        cv::circle(img, cv::Point(points(i,0), points(i,1)), 2, cv::Scalar(200,200,0), -1, CV_AA);
//    }
//
//    cv::Mat affine_matrix = (cv::Mat_<float>(2, 3) << 0.7660,-0.8428,214.1616,0.6428,0.9660,-108.4043);
//    cv::Mat dst_points = points * affine_matrix.t();
//    for(int i=0; i<rand_num; ++i) {
//        cv::circle(img, cv::Point(dst_points.at<float>(i,0), dst_points.at<float>(i,1)), 2, cv::Scalar(50,50,255), -1, CV_AA);
//    }
//
//    cv::Mat est_matrix  = cv::estimateRigidTransform(src_points.reshape(2), dst_points.reshape(2), false);
//    cv::Mat est_matrix_full  = cv::estimateRigidTransform(src_points.reshape(2), dst_points.reshape(2), true);
//
//    std::cout << "Affine Transformation Matrix:" << std::endl;
//    std::cout << affine_matrix << std::endl << std::endl;
//    std::cout << "Estimated Matrix:" << std::endl;
//    std::cout << est_matrix << std::endl << std::endl;
//    std::cout << "Estimated Matrix (full):" << std::endl;
//    std::cout << est_matrix_full << std::endl << std::endl;
//
//    waitKey(0);
//    cv::Mat est_matrixF, est_matrixF_full;
//    est_matrix.convertTo(est_matrixF, CV_32F);
//    est_matrix_full.convertTo(est_matrixF_full, CV_32F);
//    cv::Mat_<float> est_points = points * est_matrixF.t();
//    cv::Mat_<float> est_points_full = points * est_matrixF_full.t();
//    for(int i=0; i<rand_num; ++i) {
//        cv::circle(img, cv::Point(est_points(i,0), est_points(i,1)), 5, cv::Scalar(50,255,50), 1, CV_AA);
//        cv::circle(img, cv::Point(est_points_full(i,0), est_points_full(i,1)), 5, cv::Scalar(50,255,255), 1, CV_AA);
//    }
//
//    cv::namedWindow("image", CV_WINDOW_AUTOSIZE|CV_WINDOW_FREERATIO);
//    cv::imshow("image", img);
//    cv::waitKey(0);
//}
int main() {

//    test();

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
    ifstream finBox( options.getTrainBoundingBoxPath() , ifstream::in );
    Rect_<int> rect;
    for(int i = 0;i < num_of_training; i++){
        finBox >> rect.x >> rect.y >> rect.width >> rect.height;
        cout << rect << endl;
        bounding_boxes.push_back(rect);
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

    // =========================================
    // Testing
    cout << "Start Testing" << endl;
    waitKey(0);
    return 0;
}