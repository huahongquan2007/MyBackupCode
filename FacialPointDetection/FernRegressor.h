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

public:
    FernRegressor(int feature_per_fern);
    vector<Mat_<double>> Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, vector<Mat_<double>> inputShape, Mat_<double> shapeIndexFeatures);
};


#endif //_FACIALPOINTDETECTION_FERNREGRESSOR_H_
