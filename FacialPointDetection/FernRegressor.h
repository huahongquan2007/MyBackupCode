//
// Created by robotbase on 20/03/2015.
//

#ifndef _FACIALPOINTDETECTION_FERNREGRESSOR_H_
#define _FACIALPOINTDETECTION_FERNREGRESSOR_H_

#include "opencv2/opencv.hpp"
#include <vector>
using namespace std;
using namespace cv;

class FernRegressor{
    int feature_per_fern;

    vector<double> fernThreshold;
    vector<Mat_<double>> fernPairLocation; //[2x2] x1 y1;
                                           //      x2 y2
    vector<Mat_<int>> fernPairNearestLandmark; // [2x1] nearest 1 , nearest 2
    vector<Mat_<double>> regression_output;
public:
    FernRegressor(int feature_per_fern);
    vector<Mat_<double>> Train(vector<Mat_<double>> regression_target, Mat_<double> covariance_matrix, Mat_<double> pixels, Mat_<double> pixelLocation, Mat_<int> nearestLandmarkOfPixel, bool isDebug = false);
    Mat_<double> Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape);
};


#endif //_FACIALPOINTDETECTION_FERNREGRESSOR_H_
