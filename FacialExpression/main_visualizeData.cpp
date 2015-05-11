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
    int num_of_landmark = 19;


    // Read labels
    vector<int> labels;

    ifstream inputLabels("/home/robotbase/github/MyBackupCode/FacialExpression/emotion_output.txt", std::ifstream::in);
    // Read keypoints
    vector<Mat_<float>> keypoints;
    ifstream inputKeypoints( "/home/robotbase/github/MyBackupCode/FacialExpression/keypoint_output.txt" , std::ifstream::in );

    vector<string> listImagePath;
    ifstream inputPath("/home/robotbase/github/MyBackupCode/FacialExpression/path_output.txt", ifstream::in);

    for(int i = 0;i < num_of_training; i++){
        // Read keypoints
        Mat_<float> temp(num_of_landmark,2);
        float val;
        for(int j = 0; j < num_of_landmark; j++){
            inputKeypoints >> val;
            temp.at<float>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            inputKeypoints >> val;
            temp.at<float>(j, 1) = val;
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


    // wrapAffine keypoints
    Mat_<float> meanShape = keypoints[0];

    for(int i = 0 ; i < listImagePath.size(); i++){
        Mat_<unsigned char> img = imread(listImagePath[i] , CV_LOAD_IMAGE_GRAYSCALE);
//        equalizeHist( img, img );

        cout << listImagePath[i] << endl;

        int cur_label = 0;
        if(labels[i] == 0)
            cur_label = 3;
        else
            cur_label = 6;
//
//        putText(img, EXPRESSION_NAME[cur_label], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
//        putText(img, listImagePath[i], Point(0, 100), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
//        visualizeImage(img, keypoints[i], 10 , false, "result");
        cout << keypoints[i].t() << endl;

        Mat_<float> rotation;
        float scale;

        similarity_transform(meanShape, keypoints[i], rotation, scale);
////        transpose(rotation, rotation);
//
        Point2f src_center(img.cols/2.0F, img.rows/2.0F);
        float angle = atan(rotation.at<float>(1,0) / rotation.at<float>(0,0));
        cout << angle << " " << -angle * 180 / M_PI << endl;
        Mat M = getRotationMatrix2D(src_center, -angle * 180 / M_PI, 1.0);
        Mat rotatedImg;
        Mat_<float> rotatedPoint = keypoints[i];
        cout << M << endl;
        cout << rotatedPoint.t() << endl;
        cout << keypoints[i].t() << endl;
        Mat prediction_keypoint = keypoints[i];
        for(int j = 0 ; j < prediction_keypoint.rows; j++){
            rotatedPoint.at<float>(j, 0) = M.at<double>(0,0)*prediction_keypoint.at<float>(j, 0) + M.at<double>(0,1)*prediction_keypoint.at<float>(j, 1) + M.at<double>(0,2);
            rotatedPoint.at<float>(j, 1) = M.at<double>(1,0)*prediction_keypoint.at<float>(j, 0) + M.at<double>(1,1)*prediction_keypoint.at<float>(j, 1) + M.at<double>(1,2);
        }

        cout << rotatedPoint.t() << endl;
        warpAffine(img, rotatedImg, M, img.size(), INTER_CUBIC);

        double minX, minY;
        double maxX, maxY;
        Point minLoc;
        Point maxLoc;
        minMaxLoc( rotatedPoint.col(0), &minX, &maxX, &minLoc, &maxLoc );
        minMaxLoc( rotatedPoint.col(1), &minY, &maxY, &minLoc, &maxLoc );

        cout << "MinX: "<< (float)minX << endl;
        cout << "MaxX: "<< (float)maxX << endl;
        int padding = (maxX - minX) / 10;
        if(minX - padding > 0) minX -= padding;
        if(maxX + padding < img.cols) maxX += padding;
        if(minY - padding > 0) minY -= padding;
        if(maxY + padding < img.rows) maxY += padding;

        if(minX < 0) minX = 0;
        if(minY < 0) minY = 0;
        if(maxX > img.cols) maxX = img.cols - 1;
        if(maxY > img.rows) maxY = img.rows - 1;

        Mat cropped = rotatedImg.colRange(minX, maxX).rowRange(minY, maxY);
        float imgScale = 255.0f / cropped.rows;
        Mat_<float> croppedPoint = rotatedPoint.clone();
        for(int j = 0 ; j < croppedPoint.rows; j++){
            croppedPoint.at<float>(j, 0) = (croppedPoint.at<float>(j, 0) - minX) * imgScale;
            croppedPoint.at<float>(j, 1) = (croppedPoint.at<float>(j, 1) - minY) * imgScale;
        }
        resize(cropped, cropped, Size(), imgScale, imgScale);

        visualizeImage(rotatedImg, rotatedPoint, 10, false,  "rotate", false);
        visualizeImage(cropped, croppedPoint, 0, false,  "Cropped", false);

    }

    cout << "Start training: " << keypoints.size() << " images." << endl;

    FacialExpression facialExpression;
    facialExpression.Train(labels, keypoints);


    return 0;

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

    cout << "Done training. Wait 10s and test" << endl;

    waitKey(10000);
    cout << "Predict: " ;
    int result[] = {0, 0, 0, 0, 0, 0};
    for(int i = 0 ; i < num_of_testing; i++){
        int predict = facialExpression.Test(keypoints_test_normalize[i]);
        cout << " " << predict;
        result[predict]++;

        if(predict == 0)
            predict = 3;
        else
            predict = 6;

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