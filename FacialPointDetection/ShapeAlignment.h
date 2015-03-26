#ifndef _FACIALPOINTDETECTION_SHAPEALIGNMENT_H_
#define _FACIALPOINTDETECTION_SHAPEALIGNMENT_H_

#include <opencv2/opencv.hpp>

#include <vector>
#include <fstream>
#include "NormalRegressor.h"

using namespace cv;
using namespace std;

class ShapeAlignment {
private:
    int first_level_regressor;
    int second_level_regressor;
    int num_of_feature_per_fern;
    int num_of_random_pixel;
    vector<Mat_<unsigned char>> images;
    vector<Mat_<double>> keypoints;
    vector<Rect_<int>> boundingBoxes;
    Mat_<double> meanShape;
    vector<NormalRegressor> regressors;
    void setup();
public:
    ShapeAlignment(int first_level_regressor, int second_level_regressor, int num_of_feature_per_fern=5, int num_of_random_pixel=400);
    void addImages( vector<Mat_<unsigned char>> );
    void addBoundingBoxes( vector<Rect_<int>> );
    void addKeyPoints( vector<Mat_<double>> );
    void Train();
    void Save(string destination);

    Mat_<double> Test(Mat_<unsigned char> &image, Rect_<int> &bounding_box);
};


#endif //_FACIALPOINTDETECTION_SHAPEALIGNMENT_H_
