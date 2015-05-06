#ifndef _FACIALPOINTDETECTION_UTILITY_H_
#define _FACIALPOINTDETECTION_UTILITY_H_

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
using namespace std;
using namespace cv;

void visualizeImage(Mat img, Mat_<double> keypoints, int delay = 0, bool debug=false, string win_name="Images", bool isColor=false);
void visualizeImageCompare(Mat img, Mat_<double> keypoints, Mat_<double> keypoints2, int delay = 0, bool debug=false);
Mat_<double> GetMeanShape(vector<Mat_<double>> keypoints, vector<Rect_<int>> boxes);
Point GetMeanPoint(Mat_<double> keypoints);
Mat_<double> ProjectToBoxCoordinate( Mat_<double> points, Rect_<int> box );
Mat_<double> ProjectToImageCoordinate( Mat_<double> points, Rect_<int> box , bool translationToBox = true );
double calculate_covariance(const Mat_<double> x, const Mat_<double> y);
void similarity_transform(const Mat_<double>& destination, const Mat_<double>& alignInput, Mat_<double>& rotation,double& scale);
void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<double>> &keypoints, string &train_path);
void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path);
void readFERDataset();

cv::Mat_<double> normalizeKeypoint(cv::Mat_<double> keypoint);
#endif //_FACIALPOINTDETECTION_UTILITY_H_
