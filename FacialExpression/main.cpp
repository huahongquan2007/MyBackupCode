#include <iostream>
#include <fstream>
#include <opencv2/opencv.hpp>
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

using namespace cv;
using namespace std;

#include "utility.h"
#include "FacialExpression.h"

int main() {
    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    int num_of_training = 8000;
    int num_of_landmark = 68;

    vector<Mat_<float>> keypoints;

    // -------------- READ KEYPOINTS -------------
    string train_path = "/home/robotbase/github/MyBackupCode/FacialExpression/keypoint_output.txt";
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    for(int i = 0 ; i < keypoints.size() ; i++){
        keypoints[i] = normalizeKeypoint(keypoints[i]);
    }

    vector<int> labels;

    // READ DATASET
    cout << "Read Dataset" << endl;
    ifstream input("/home/robotbase/github/MyBackupCode/FacialExpression/emotion_output.txt", std::ifstream::in);

    string str;

    while( getline(input, str) ){
        string ex_code = str;

        int cur_code = stoi(str);
        // "Angry", "Disgusted", "Fear", "Happy", "Sad", "Surprised", "Neural"
//        if(ex_code == "0"){
//            cur_code = 0;
//        } else if(ex_code == "1"){
//            cur_code = 1;
//        } else if(ex_code == "2"){
//            cur_code = 2;
//        } else if(ex_code == "3"){
//            cur_code = 3;
//        } else if(ex_code == "4"){
//            cur_code = Sad;
//        } else if(ex_code == "5"){
//            cur_code = Surprised;
//        } else if(ex_code == "6"){
//            cur_code = Neural;
//        }

        labels.push_back( cur_code );
    }

    vector<string> listImagePath;
    ifstream inputPath("/home/robotbase/github/MyBackupCode/FacialExpression/path_output.txt", ifstream::in);
    while( getline(inputPath, str) ) {
        listImagePath.push_back(str);
    }
    cout << "Read Images " << listImagePath.size() << endl;

    for(int i = 0 ; i < listImagePath.size(); i++){
        Mat_<unsigned char> img = imread(listImagePath[i] , CV_LOAD_IMAGE_GRAYSCALE);
        equalizeHist( img, img );

        cout << listImagePath[i] << endl;
        putText(img, EXPRESSION_NAME[labels[i]], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        putText(img, listImagePath[i], Point(0, 100), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        visualizeImage(img, keypoints[i], 0 , false, "result");
    }


    cout << "Start training: " << keypoints.size() << " images." << endl;

    FacialExpression facialExpression;
    facialExpression.Train(labels, keypoints);


    int num_of_testing = 100;
    // -------------- READ IMAGE ---------------
    vector<Mat_<unsigned char>> images;
    string img_path = "";
    Mat img_data;
    float EXPECT_NEED = 500;
    vector<float> scaleImage;
    ifstream finBox( "/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/IBUG/images.txt" , ios_base::in );
    for(int i = 0;i < num_of_testing; i++){
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

    // READ TEST
    string test_path = "/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/IBUG/keypoints.txt";

    vector<Mat_<float>> keypoints_test;
    vector<Mat_<float>> keypoints_test_normalize;
    readKeypoints(num_of_testing, num_of_landmark, keypoints_test, test_path);

    // -------------- SCALE BOX , Keypoints ----------
    for(int i = 0 ; i < num_of_testing ; i++){
        keypoints_test[i] = scaleImage[i] * keypoints_test[i];
    }
    keypoints_test_normalize.resize(keypoints_test.size());
    for(int i = 0 ; i < keypoints_test.size() ; i++){
        keypoints_test_normalize[i] = normalizeKeypoint(keypoints_test[i]);
    }

    int result[] = {0, 0, 0, 0, 0, 0};
    cout << "Predict: " ;
    for(int i = 0 ; i < num_of_testing; i++){
        int predict = facialExpression.Test(keypoints_test_normalize[i]);
        cout << predict;
        result[predict]++;

        putText(images[i], EXPRESSION_NAME[predict], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        visualizeImage(images[i], keypoints_test[i], 1000, false, "predict");
    }
    cout << endl;
    cout << "Result: " << endl;
    for(int i = 0 ; i < 6 ; i++ ){
        cout << result[i] << " ";
    }
    cout << endl;

    waitKey(0);
    return 0;
}