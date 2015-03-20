//
// Created by robotbase on 20/03/2015.
//

#include "NormalRegressor.h"

NormalRegressor::NormalRegressor(int child_level, int feature_per_fern) {
    for(int i = 0 ; i < child_level ; i ++ ){
        FernRegressor curRegressor ( feature_per_fern ) ;
        childRegressor.push_back(curRegressor);
    }
}

vector<Mat_<double>> NormalRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, vector<Rect_<int>> boundingBoxes, vector<Mat_<double>> curShape){
    cout << "NormalRegressor: Train" << endl;

    return curShape;
}