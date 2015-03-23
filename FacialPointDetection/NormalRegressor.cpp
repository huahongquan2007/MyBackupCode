//
// Created by robotbase on 20/03/2015.
//

#include "NormalRegressor.h"

NormalRegressor::NormalRegressor(int child_level, int feature_per_fern, int num_of_random_pixels) {

    NormalRegressor::num_of_random_pixels = num_of_random_pixels;

    for(int i = 0 ; i < child_level ; i ++ ){
        FernRegressor curRegressor ( feature_per_fern ) ;
        childRegressor.push_back(curRegressor);
    }
}

vector<Mat_<double>> NormalRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, Mat_<double> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<double>> inputShape){
    cout << "NormalRegressor: Train" << endl;

    vector<Mat_<double>> curShape = inputShape;

    Mat_<double> shapeIndexFeatures (num_of_random_pixels, 3);

    vector<Mat_<double>> deltaShape;

    // Random P pixels
    int P = num_of_random_pixels; // 400

    for(int i = 0 ; i < P ; i ++){

    }
    // Create P^2 shape-index features

    // Calculate covariance between features: X

    for(FernRegressor &child : childRegressor){

        // Train each child-level regressor

        deltaShape = child.Train(images, keypoints, curShape, shapeIndexFeatures);

        // TO DO: curShape = curShape + deltaShape;
    }

    return curShape;
}