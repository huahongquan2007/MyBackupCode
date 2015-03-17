#ifndef _FACIALPOINTDETECTION_UTILITY_H_
#define _FACIALPOINTDETECTION_UTILITY_H_

#include <iostream>
#include <vector>
#include <opencv2/opencv.hpp>
using namespace std;
using namespace cv;

void visualizeImage(Mat img, Mat_<double> keypoints, int num_of_landmark, bool debug=false);
#endif //_FACIALPOINTDETECTION_UTILITY_H_
