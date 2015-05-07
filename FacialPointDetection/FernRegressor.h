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

    vector<float> fernThreshold;
    vector<Mat_<float>> fernPairLocation; //[2x2] x1 y1;
                                           //      x2 y2
    vector<Mat_<int>> fernPairNearestLandmark; // [2x1] nearest 1 , nearest 2
    vector<Mat_<float>> regression_output;
public:
    FernRegressor(){

    }
    FernRegressor(int feature_per_fern);
    vector<Mat_<float>> Train(vector<Mat_<float>> regression_target, Mat_<float> covariance_matrix, Mat_<int> pixels, Mat_<float> pixelLocation, Mat_<int> nearestLandmarkOfPixel, bool isDebug = false);
    Mat_<float> Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<float> curShape, Mat_<float> meanShape);

    void Save(FileStorage &out);
    void Load(FileNode in);
};


#endif //_FACIALPOINTDETECTION_FERNREGRESSOR_H_
