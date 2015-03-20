#include "ShapeAlignment.h"
#include "NormalRegressor.h"

ShapeAlignment::ShapeAlignment(int first_level_regressor, int second_level_regressor, int num_of_feature_per_fern) {
    ShapeAlignment::first_level_regressor = first_level_regressor;
    ShapeAlignment::second_level_regressor = second_level_regressor;
    ShapeAlignment::num_of_feature_per_fern = num_of_feature_per_fern;

    setup();
}

void ShapeAlignment::setup(){

    for(int i = 0 ; i < first_level_regressor ; i ++){
        NormalRegressor curRegressor( second_level_regressor , num_of_feature_per_fern );
        regressors.push_back(curRegressor);
    }
}
void ShapeAlignment::addImages(vector<Mat_<unsigned char>> imgVector) {
    ShapeAlignment::images = imgVector;
}

void ShapeAlignment::addKeyPoints(vector<Mat_<double>> keypointsVector) {
    keypoints = keypointsVector;
}

void ShapeAlignment::addBoundingBoxes(vector<Rect_<int>> boundingboxVector) {
    boundingBoxes = boundingboxVector;
}

void ShapeAlignment::Save(string destination) {
    cout << "SAVE MODEL: " << destination << endl;
    ofstream out(destination, ofstream::out);
    out << "HEHE" << endl;
    out.close();
}

void ShapeAlignment::Train(){
    cout << "TRAIN SHAPE MODEL" << endl;

    vector<Mat_<double>> curShape;
    for(int i = 0 ; i < first_level_regressor ; i ++){
        curShape = regressors[i].Train(images, keypoints, boundingBoxes, curShape);
    }
}