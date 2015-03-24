//
// Created by robotbase on 20/03/2015.
//

#include "FernRegressor.h"

FernRegressor::FernRegressor(int feature_per_fern) {

}
vector<Mat_<double>> FernRegressor::Train(vector< Mat_<double> > regression_target, Mat_<double> covariance_matrix ){

    cout << "-FernRegressor: Train" << endl;

    vector<Mat_<double>> curShape = regression_target;

    return curShape;
}