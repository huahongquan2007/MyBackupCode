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
    vector<Mat_<float>> Train(vector<Mat_<unsigned char>> images, vector<Mat_<float>> keypoints, Mat_<float> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<float>> inputShape, bool isDebug = true);
    Mat_<float> Test(const Mat_<unsigned char> &image, const Rect_<int> &bounding_box, const Mat_<float> &curShape, const Mat_<float> &meanShape);

    void Save(FileStorage &out);
    void Load(FileNode in);
};


#endif //_FACIALPOINTDETECTION_NORMALREGRESSOR_H_
