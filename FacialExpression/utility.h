#ifndef FACIALEXPRESSION_UTILITY_H
#define FACIALEXPRESSION_UTILITY_H

#include <opencv2/opencv.hpp>

void readKeypoints(int const num_of_training, int const num_of_landmark, std::vector<cv::Mat_<double>> &keypoints, std::string &train_path);
void visualizeImage(cv::Mat img, cv::Mat_<double> keypoints, int delay = 0, bool debug=false, std::string win_name="Images", bool isColor=false);

#endif //FACIALEXPRESSION_UTILITY_H
