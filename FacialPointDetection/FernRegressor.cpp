//
// Created by robotbase on 20/03/2015.
//

#include "FernRegressor.h"

FernRegressor::FernRegressor(int feature_per_fern) {

}
vector<Mat_<double>> FernRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, vector<Mat_<double>> inputShape, Mat_<double> shapeIndexFeatures ){

    cout << "-FernRegressor: Train" << endl;

    vector<Mat_<double>> curShape = inputShape;

    return curShape;
}