#ifndef FACIALEXPRESSION_FACIALEXPRESSION_H
#define FACIALEXPRESSION_FACIALEXPRESSION_H

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>

enum EXPRESSION_CODE { Angry = 0, Disgusted = 1, Fear = 2, Happy = 3, Neural = 4, Sad = 5, Surprised = 6};
const std::string EXPRESSION_NAME[] = {"Angry", "Disgusted", "Fear", "Happy", "Neural", "Sad", "Surprised"};

class FacialExpression {
private:
    cv::SVM svm;
public:
    void Train(std::vector<EXPRESSION_CODE> labels, std::vector<cv::Mat_<double>> keypoints);
    void Save(std::string destination);
    void Load(std::string destination);
    int Test(cv::Mat_<double> keypoint);
};

#endif //FACIALEXPRESSION_FACIALEXPRESSION_H
