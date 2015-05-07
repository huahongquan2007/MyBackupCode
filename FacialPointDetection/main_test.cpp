#include <iostream>
#include <opencv2/opencv.hpp>
#include <vector>
#include <fstream>
using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"

void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path);
void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<float>> &keypoints, string &train_path);

int main(){
    cout << "TEST" << endl;
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config.txt");

    // =========================================
    // Training
    const int num_of_landmark = options.getNumOfLandmark();
    const int num_of_training = options.getNumOfTraining();
    vector<Mat> images;
    vector<Mat_<float>> keypoints;
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
    // -------------- READ KEYPOINTS -------------
    string train_path = options.getTrainKeypointsPath();
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    cout << keypoints[0] << endl;
    cout << keypoints[1] << endl;




    Mat curImg ( 500, 500 , CV_32FC3);
    Mat prevImg ( 500, 500 , CV_32FC3);
//    cvtColor(images[0], curImg, CV_GRAY2BGR);

    // -------------- READ BOUNDING BOX ----------
    string bounding_box_train_path = options.getTrainBoundingBoxPath();
    readBoundingBoxes(num_of_training, bounding_boxes, bounding_box_train_path);

    Mat_<float> key1, key2;
//
    int img_1 = 0;
    key1 = keypoints[ img_1 ];
    int img_2 = 50;
    key1 = ProjectToBoxCoordinate(keypoints[img_1], bounding_boxes[img_1]);
    key2 = ProjectToBoxCoordinate(keypoints[img_2], bounding_boxes[img_2]);

    Mat_<float> rotation;
    float scale = 0;
    similarity_transform(key1, key2, rotation, scale);

    cout << "ROTATION : " << rotation << endl;
    cout << "SCALE: " << scale << endl;
    Mat_<float> curKey = key1;
    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) ( curKey.at<float>(j, 0) * 100 ) + 250;
        int y = (int) ( curKey.at<float>(j, 1) * 100 ) + 250;

        circle(curImg, Point(x, y), 2, Scalar(255, 0, 0), -1);
        circle(prevImg, Point(x, y), 2, Scalar(255, 0, 0), -1);
    }

    curKey = key2;
    transpose(rotation, rotation);
    Mat_<float> rotateKey = scale * curKey * rotation;

    similarity_transform(key2, key1, rotation, scale);
    transpose(rotation, rotation);
    Mat_<float> reconstructKey = scale * rotateKey * rotation;
    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) ( curKey.at<float>(j, 0) * 100 ) + 250;
        int y = (int) ( curKey.at<float>(j, 1) * 100 ) + 250;

        circle(prevImg, Point(x, y), 2, Scalar(0, 255, 0), -1);
        circle(curImg, Point(x, y), 2, Scalar(0, 255, 0), -1);

        x = (int) ( rotateKey.at<float>(j, 0) * 100 ) + 250;
        y = (int) ( rotateKey.at<float>(j, 1) * 100 ) + 250;

        circle(curImg, Point(x, y), 3, Scalar(0, 0, 255), -1);

        x = (int) ( reconstructKey.at<float>(j, 0) * 100 ) + 250;
        y = (int) ( reconstructKey.at<float>(j, 1) * 100 ) + 250;

        circle(curImg, Point(x, y), 3, Scalar(0, 255, 255), -1);

//        pos = rotation.t() * pos / scale;
//
//        x = pos.at<float>(0, 0);
//        y = pos.at<float>(1, 0);
//
//        cout << x << " " << y << endl;
//        circle(curImg, Point(x, y), 2, Scalar(0, 0, 255), -1);
    }

    namedWindow("img", WINDOW_NORMAL);
    imshow("img", curImg);

    namedWindow("previmg", WINDOW_NORMAL);
    imshow("previmg", prevImg);

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

void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<float>> &keypoints, string &train_path) {
    ifstream finKey( train_path , ios_base::in );
    for(int i = 0;i < num_of_training; i++){
        Mat_<float> temp(num_of_landmark,2);
        float val;
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<float>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<float>(j, 1) = val;
        }
        keypoints.push_back(temp);
    }
    finKey.close();
}