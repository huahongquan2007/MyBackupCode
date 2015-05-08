#ifndef _FACIALPOINTDETECTION_UTILITY_H_
#define _FACIALPOINTDETECTION_UTILITY_H_

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
using namespace std;
using namespace cv;

void visualizeImage(Mat img, Mat_<float> keypoints, int delay = 0, bool debug=false, string win_name="Images", bool isColor=false);
void visualizeImageCompare(Mat img, Mat_<float> keypoints, Mat_<float> keypoints2, int delay = 0, bool debug=false);
Mat_<float> GetMeanShape(vector<Mat_<float>> keypoints, vector<Rect_<int>> boxes);
Point GetMeanPoint(Mat_<float> keypoints);
Mat_<float> ProjectToBoxCoordinate( const Mat_<float>& points, const Rect_<int>& box );
Mat_<float> ProjectToImageCoordinate( const Mat_<float>& points, const Rect_<int>& box , bool translationToBox = true );

float calculate_covariance(const Mat_<float> x, const Mat_<float> y);
void similarity_transform(const Mat_<float>& destination, const Mat_<float>& alignInput, Mat_<float>& rotation,float& scale);
void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<float>> &keypoints, string &train_path);
void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path);
void readFERDataset();



float heuristicMult(float a, float b);
cv::Mat_<double> normalizeKeypoint(cv::Mat_<double> keypoint);

#endif //_FACIALPOINTDETECTION_UTILITY_H_
