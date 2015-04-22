#include <iostream>
#include <opencv2/opencv.hpp>
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"
#include "ShapeAlignment.h"

String face_cascade_name = "/home/robotbase/github/MyBackupCode/FacialPointDetection/haarcascade_frontalface_alt.xml";
CascadeClassifier face_cascade;

int main() {
    // Load face cascade
    if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n"); return -1; };

    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config_ibug.txt");
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

    // READ DATSET
    ifstream input("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/listJAFFE.txt", ifstream::in);

    string str;
    vector<string> listImagePath;
    while( getline(input, str) ){
        listImagePath.push_back(str);
    }

    vector<Mat_<unsigned char>> images_test;
    vector<Rect_<int>> bounding_boxes_test;

    for(int i = 0 ; i < listImagePath.size(); i++){
        Mat_<unsigned char> img = imread("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/JAFFE/" + listImagePath[i] , CV_LOAD_IMAGE_GRAYSCALE);
        equalizeHist( img, img );

        std::vector<Rect_<int>> faces;
        //-- Detect faces
        face_cascade.detectMultiScale( img, faces, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT, Size(60, 60) );

        if(faces.size() > 0){
            bounding_boxes_test.push_back(faces[0]);
        }

        Mat testMat = img.clone();
        for(int j = 0 ; j < faces.size(); j++){
            rectangle(testMat, faces[j], (255,255,255), 2);
        }

        imshow("box", testMat);
        waitKey(10);
        images_test.push_back(img);
    }
    //======================================


    cout << "Start Testing" << endl;
    const int num_of_testing = listImagePath.size();

    cout << "Start Testing: numOfImages: " << images_test.size() << endl;

    ShapeAlignment shapeAlignmentTest(first_level, second_level, feature_per_fern);
    shapeAlignmentTest.addKeyPoints(keypoints);
    shapeAlignmentTest.addBoundingBoxes(bounding_boxes);
    shapeAlignmentTest.Load(options.getModelPath());


//    Mat cur_img = imread("/home/robotbase/Desktop/hinh2.jpg", CV_LOAD_IMAGE_COLOR);
//
//    Mat_<unsigned char> eqImg;
//    cvtColor(cur_img, eqImg, CV_RGB2GRAY);
//    equalizeHist( eqImg, eqImg );
//    std::vector<Rect_<int>> faces;
//    //-- Detect faces
//    face_cascade.detectMultiScale( eqImg, faces, 1.1, 4, CV_HAAR_FIND_BIGGEST_OBJECT, Size(30, 30) );
//
//    cout << faces.size() << endl;
//    if(faces.size() > 0){
//        Mat_<double> result = shapeAlignmentTest.Test(eqImg, faces[0]);
//        visualizeImage(cur_img, result, 0, false, "result", true);
//        waitKey(0);
//        waitKey(0);
//        waitKey(0);
//    }
//    return 10;

    // --------------- SAVE KEYPOINTS -------------
    ofstream out("/home/robotbase/github/MyBackupCode/FacialPointDetection/save_result.txt", ofstream::out);

    int start_position = 0;
    for(int i = start_position ; i < images_test.size() + start_position; i++){

        Mat_<double> prediction = shapeAlignmentTest.Test(images_test[i], bounding_boxes_test[i]);


            for(int j = 0 ; j < prediction.rows; j++){
                out << prediction.at<double>(j, 0) << " ";
            }
            for(int j = 0 ; j < keypoints[i].rows; j++){
                out << prediction.at<double>(j, 1) << " ";
            }
            out << endl;

        visualizeImage(images_test[i], prediction, 10, false, "result");

        cout << "==============================" << endl;
        cout << "FINISH " << i << endl;
        cout << prediction << endl;
    }

    waitKey(0);
    return 0;
}
