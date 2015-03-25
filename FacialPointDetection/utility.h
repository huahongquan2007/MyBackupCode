#ifndef _FACIALPOINTDETECTION_UTILITY_H_
#define _FACIALPOINTDETECTION_UTILITY_H_

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
using namespace std;
using namespace cv;

void visualizeImage(Mat img, Mat_<double> keypoints, int delay = 0, bool debug=false);
void visualizeImageCompare(Mat img, Mat_<double> keypoints, Mat_<double> keypoints2, int delay = 0, bool debug=false);
Mat_<double> GetMeanShape(vector<Mat_<double>> keypoints, vector<Rect_<int>> boxes);
Mat_<double> ProjectToBoxCoordinate( Mat_<double> points, Rect_<int> box );
Mat_<double> ProjectToImageCoordinate( Mat_<double> points, Rect_<int> box );
double calculate_covariance(Mat_<double> x, Mat_<double> y);

#endif //_FACIALPOINTDETECTION_UTILITY_H_
