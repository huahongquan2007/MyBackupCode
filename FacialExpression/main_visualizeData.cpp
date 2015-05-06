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
    cout << "Facial Expression Projects" << endl;
    cout << "Visualization: " << endl;


    // =========================================
    // Read Configuration
    int num_of_training = 8000;
    int num_of_landmark = 68;


    // Read labels
    vector<int> labels;

    ifstream inputLabels("/home/robotbase/github/MyBackupCode/FacialExpression/emotion_output.txt", std::ifstream::in);
    // Read keypoints
    vector<Mat_<double>> keypoints;
    ifstream inputKeypoints( "/home/robotbase/github/MyBackupCode/FacialExpression/keypoint_output.txt" , std::ifstream::in );

    vector<string> listImagePath;
    ifstream inputPath("/home/robotbase/github/MyBackupCode/FacialExpression/path_output.txt", ifstream::in);

    for(int i = 0;i < num_of_training; i++){
        // Read keypoints
        Mat_<double> temp(num_of_landmark,2);
        double val;
        for(int j = 0; j < num_of_landmark; j++){
            inputKeypoints >> val;
            temp.at<double>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            inputKeypoints >> val;
            temp.at<double>(j, 1) = val;
        }

        // Read labels
        string ex_code;
        getline(inputLabels, ex_code);
        int cur_code = stoi(ex_code);

        string img_path;
        getline(inputPath, img_path);
//        "Angry", "Disgusted", "Fear", "Happy", "Sad", "Surprised", "Neural"
        if(cur_code == 3 || cur_code == 6){
            if(cur_code == 3)
                labels.push_back( 0 );
            else
                labels.push_back( 1 );
            keypoints.push_back(temp);
            listImagePath.push_back(img_path);
        }
    }

    inputLabels.close();
    inputKeypoints.close();

//    for(int i = 0 ; i < keypoints.size() ; i++){
//        keypoints[i] = normalizeKeypoint(keypoints[i]);
//    }

//    for(int i = 0 ; i < listImagePath.size(); i++){
//        Mat_<unsigned char> img = imread(listImagePath[i] , CV_LOAD_IMAGE_GRAYSCALE);
//        equalizeHist( img, img );
//
//        cout << listImagePath[i] << endl;
//        putText(img, EXPRESSION_NAME[labels[i]], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
//        putText(img, listImagePath[i], Point(0, 100), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
//        visualizeImage(img, keypoints[i], 0 , false, "result");
//    }

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

        if(img_data.cols > EXPECT_NEED){
            scale = EXPECT_NEED / img_data.cols;
            resize(img_data, img_data, Size( (int) (scale * img_data.cols), (int)(scale * img_data.rows)) );
        }

        scaleImage.push_back(scale);

        images.push_back(img_data);
    }
    finBox.close();

    facialExpression.Save("/home/robotbase/github/MyBackupCode/FacialExpression/expressionModel.txt");

    // READ TEST
    string test_path = "/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/IBUG/keypoints.txt";

    vector<Mat_<double>> keypoints_test;
    vector<Mat_<double>> keypoints_test_normalize;
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
    for(int i = 0 ; i < num_of_testing; i++){
        int predict = facialExpression.Test(keypoints_test_normalize[i]);
        cout << "Predict: " << predict << endl;
        result[predict]++;

        putText(images[i], EXPRESSION_NAME[predict], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        visualizeImage(images[i], keypoints_test[i], 1000, false, "predict");
    }

    cout << "Result: " << endl;
    for(int i = 0 ; i < 6 ; i++ ){
        cout << result[i] << " ";
    }
    cout << endl;

    waitKey(0);
    return 0;
}