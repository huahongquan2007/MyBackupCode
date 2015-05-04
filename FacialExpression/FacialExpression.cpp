#include "FacialExpression.h"
#include <iostream>
#include <opencv2/ml/ml.hpp>

using namespace std;
using namespace cv;

void FacialExpression::Train(std::vector<EXPRESSION_CODE> labels, std::vector<cv::Mat_<double>> keypoints){
    cout << "Train Facial Expression" << endl;

    int num_of_data = labels.size();
    int num_of_landmark = keypoints[0].rows;
    Mat labelMat (num_of_data, 1 , CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        labelMat.at<float>(i, 0) = (float) labels[i];
    }
    cout << labelMat.size() << endl;

    Mat trainingDataMat(num_of_data, num_of_landmark * 2, CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        for(int j = 0 ; j < num_of_landmark; j ++){
            trainingDataMat.at<float>(i, j) = (float) keypoints[i].at<double>(j, 0);
        }
        for(int j = 0 ; j < num_of_landmark; j ++){
            trainingDataMat.at<float>(i, j + num_of_landmark) = (float) keypoints[i].at<double>(j, 1);
        }
    }
    cout << trainingDataMat.size() << endl;
    cout << trainingDataMat.row(0) << endl;

    // ------------------------------ SVM ----------------------------
//    SVMParams params;
//    params.svm_type    = SVM::C_SVC;
//    params.kernel_type = SVM::RBF;
//    params.term_crit   = cvTermCriteria(CV_TERMCRIT_ITER, 100, 1e-6);
//
//    svm.train_auto(trainingDataMat, labelMat, Mat(), Mat(), params);
//
//    cout << "SV COUNT: " << svm.get_support_vector_count() << endl;
//
//
//    int count = 0;
//    for(int i = 0 ; i < num_of_data ; i++){
//        int predict = svm.predict(trainingDataMat.row(i));
//        cout << "PREDICT: " << predict << " TRUTH : " << labels[i] << endl;
//        if( predict == labels[i])
//            count++;
//    }

    // ------------------------------- ANN ---------------------------
    cv::Mat layers = cv::Mat(4, 1, CV_32SC1);

    layers.row(0) = cv::Scalar(136);
    layers.row(1) = cv::Scalar(200);
    layers.row(2) = cv::Scalar(150);
    layers.row(3) = cv::Scalar(1);

    CvANN_MLP mlp;
    CvANN_MLP_TrainParams params;
    CvTermCriteria criteria;
    criteria.max_iter = 100;
    criteria.epsilon = 0.00001f;
    criteria.type = CV_TERMCRIT_ITER | CV_TERMCRIT_EPS;
    params.train_method = CvANN_MLP_TrainParams::BACKPROP;
    params.bp_dw_scale = 0.05f;
    params.bp_moment_scale = 0.05f;
    params.term_crit = criteria;

    cout << "Before create" << endl;

    mlp.create(layers);

    cout << "Before train" << endl;
    // train
    mlp.train(trainingDataMat, labelMat, cv::Mat(), cv::Mat(), params);

    cout << "After train" << endl;

    cv::Mat response(1, 1, CV_32FC1);

    int count = 0;
    for(int i = 0 ; i < num_of_data ; i++){
        cv::Mat response(1, 1, CV_32FC1);
        mlp.predict(trainingDataMat.row(i), response);
        int predict = response.at<float>(0,0);
        cout << "PREDICT: " << predict << " TRUTH : " << labels[i] << endl;
//        if( predict == 1 && labels[i] == 3 || predict == 7 && labels[i] == 6)
        if(predict == labels [i])
            count++;
    }

    cout << "RESULT: " << count << "/" << num_of_data << " Percent: " << 100 * count / num_of_data << endl;
    waitKey(0);
}

int FacialExpression::Test(cv::Mat_<double> keypoint){
    int num_of_landmark = keypoint.rows;
    Mat testDataMat(1, num_of_landmark * 2, CV_32FC1);

    for(int j = 0 ; j < num_of_landmark; j ++){
        testDataMat.at<float>(0, j) = (float) keypoint.at<double>(j, 0);
    }
    for(int j = 0 ; j < num_of_landmark; j ++){
        testDataMat.at<float>(0, j + num_of_landmark) = (float) keypoint.at<double>(j, 1);
    }

    int predict = svm.predict(testDataMat);

    return predict;
}