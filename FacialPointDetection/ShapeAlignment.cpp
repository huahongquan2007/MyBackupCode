#include <strings.h>
#include "ShapeAlignment.h"
#include "utility.h"

ShapeAlignment::ShapeAlignment(int first_level_regressor, int second_level_regressor, int num_of_feature_per_fern, int num_of_random_pixel) {
    ShapeAlignment::first_level_regressor = first_level_regressor;
    ShapeAlignment::second_level_regressor = second_level_regressor;
    ShapeAlignment::num_of_feature_per_fern = num_of_feature_per_fern;
    ShapeAlignment::num_of_random_pixel = num_of_random_pixel;

    setup();
}

void ShapeAlignment::setup(){

    for(int i = 0 ; i < first_level_regressor ; i ++){
        NormalRegressor curRegressor( second_level_regressor , num_of_feature_per_fern , num_of_random_pixel);
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
    vector<Mat_<double>> deltaShape;
    Mat_<double> meanShape = GetMeanShape(keypoints, boundingBoxes);

    // Use boundingBox to generate initialized locations for training data
    RNG rng;
    for(int i = 0 ; i < images.size(); i++){
        int index = i;
        while(index == i){
            index = rng.uniform(0, images.size() - 1);
        }

        Mat_<double> initial = ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]);
        curShape.push_back(ProjectToImageCoordinate(initial, boundingBoxes[i] ));
    }


    cout << "KEYPOINTS: " << keypoints[0] << endl;
    cout << "CURSHAPE: " << curShape[0] << endl;

    for(int i = 0 ; i < first_level_regressor ; i ++){
        deltaShape = regressors[i].Train(images, keypoints, meanShape, boundingBoxes, curShape);

        for(int j = 0 ; j < curShape.size() ; j++){
            curShape[j] += deltaShape[j];
        }
    }
}
