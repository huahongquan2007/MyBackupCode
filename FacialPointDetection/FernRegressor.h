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
    vector<Mat_<double>> Train(vector< Mat_<double> >, Mat_<double> covariance_matrix);
};


#endif //_FACIALPOINTDETECTION_FERNREGRESSOR_H_
