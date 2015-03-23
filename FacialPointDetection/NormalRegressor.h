//
// Created by robotbase on 20/03/2015.
//

#ifndef _FACIALPOINTDETECTION_NORMALREGRESSOR_H_
#define _FACIALPOINTDETECTION_NORMALREGRESSOR_H_

#include "FernRegressor.h"
#include "opencv2/opencv.hpp"
#include <vector>
using namespace std;
using namespace cv;

class NormalRegressor{
private:
    vector<FernRegressor> childRegressor;
    int num_of_random_pixels;
public:
    NormalRegressor(int, int, int );
    vector<Mat_<double>> Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, Mat_<double> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<double>> inputShape);
};


#endif //_FACIALPOINTDETECTION_NORMALREGRESSOR_H_
