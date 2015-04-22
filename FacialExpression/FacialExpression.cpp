#include "FacialExpression.h"
#include <iostream>
#include <opencv2/ml/ml.hpp>

using namespace std;
using namespace cv;

void FacialExpression::Train(std::vector<EXPRESSION_CODE> labels, std::vector<cv::Mat_<double>> keypoints){
    cout << "Train Facial Expression" << endl;

    int num_of_data = labels.size();
    Mat labelMat (num_of_data, 1 , CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        labelMat.at<float>(i, 0) = (float) labels[i];
    }
    cout << labelMat.size() << endl;

    Mat trainingDataMat(num_of_data, keypoints[0].rows * 2, CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        for(int j = 0 ; j < keypoints[i].rows; j ++){
            trainingDataMat.at<float>(i, j) = (float) keypoints[i].at<double>(j, 0);
        }
        for(int j = 0 ; j < keypoints[i].rows; j ++){
            trainingDataMat.at<float>(i, j + keypoints[i].rows) = (float) keypoints[i].at<double>(j, 1);
        }
    }
    cout << trainingDataMat.size() << endl;
    cout << trainingDataMat.row(0) << endl;

    SVMParams params;
    params.svm_type    = SVM::C_SVC;
    params.kernel_type = SVM::RBF;
    params.term_crit   = cvTermCriteria(CV_TERMCRIT_ITER, 100, 1e-6);

    SVM svm;
    svm.train_auto(trainingDataMat, labelMat, Mat(), Mat(), params);

    cout << "SV COUNT: " << svm.get_support_vector_count() << endl;


    int count = 0;
    for(int i = 0 ; i < num_of_data ; i++){
        int predict = svm.predict(trainingDataMat.row(i));
        cout << "PREDICT: " << predict << " TRUTH : " << labels[i] << endl;
        if( predict == labels[i])
            count++;
    }
    cout << "RESULT: " << count << "/" << num_of_data << endl;

    waitKey(0);
}
