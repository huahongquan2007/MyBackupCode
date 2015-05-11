#ifndef FACIALEXPRESSION_UTILITY_H
#define FACIALEXPRESSION_UTILITY_H

#include <opencv2/opencv.hpp>

void readKeypoints(int const num_of_training, int const num_of_landmark, std::vector<cv::Mat_<float>> &keypoints, std::string &train_path);
void visualizeImage(cv::Mat img, cv::Mat_<float> keypoints, int delay = 0, bool debug=false, std::string win_name="Images", bool isColor=false);
cv::Mat_<float> GetMeanShape(std::vector<cv::Mat_<float>> keypoints, std::vector<cv::Rect_<int>> boxes);
void similarity_transform(const cv::Mat_<float>& destination, const cv::Mat_<float>& alignInput, cv::Mat_<float>& rotation,float& scale);

cv::Mat_<float> ProjectToBoxCoordinate( const cv::Mat_<float>& points, const cv::Rect_<int>& box );
cv::Mat_<float> ProjectToImageCoordinate( const cv::Mat_<float>& points, const cv::Rect_<int>& box , bool translationToBox = true );

cv::Mat_<float> normalizeKeypoint(cv::Mat_<float> keypoint);
#endif //FACIALEXPRESSION_UTILITY_H
