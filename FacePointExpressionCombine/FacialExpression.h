#ifndef FACIALEXPRESSION_FACIALEXPRESSION_H
#define FACIALEXPRESSION_FACIALEXPRESSION_H

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
#include <opencv2/ml/ml.hpp>

//enum EXPRESSION_CODE { Angry = 0, Disgusted = 1, Fear = 2, Happy = 3, Sad = 4, Surprised = 5, Neural = 6};
const std::string EXPRESSION_NAME[] = {"Angry", "Disgusted", "Fear", "Happy", "Sad", "Surprised", "Neural"};

class FacialExpression {
private:
    cv::SVM svm;
    cv::CvANN_MLP mlp;
public:
    void Train(std::vector<int> labels, std::vector<cv::Mat_<double>> keypoints);
    void Save(std::string destination);
    void Load(std::string destination);
    int Test(cv::Mat_<double> keypoint);
};

#endif //FACIALEXPRESSION_FACIALEXPRESSION_H
