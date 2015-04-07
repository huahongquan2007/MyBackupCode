#include <opencv2/opencv.hpp>
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

    meanShape = GetMeanShape(keypoints, boundingBoxes);

    // generate more image, keypoints, curShape & inputShape
    int total_image_original = images.size();
    for(int j = 0 ; j < 0 ; j ++){
        for(int i = 0 ; i < total_image_original; i++){
            cout << "GENERATE " << i << endl;
            images.push_back(images[i].clone());
            keypoints.push_back(keypoints[i].clone());
            boundingBoxes.push_back(boundingBoxes[i]);
        }
    }

    // Use boundingBox to generate initialized locations for training data
    RNG rng;
    for(int i = 0 ; i < images.size(); i++){

        // method 1: random
        int index = i;
        while(index == i){
            index = rng.uniform(0, images.size() - 1);
        }

        Mat_<double> initial = ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]);

        Mat_<double> new_points = ProjectToImageCoordinate(initial, boundingBoxes[i] );
        Point mean_new_points = GetMeanPoint(new_points);
        Point mean_old_points = GetMeanPoint(keypoints[i]);

        Point translation = mean_new_points - mean_old_points;

        for(int j = 0 ; j < new_points.rows ; j++){
            new_points.at<double>(j, 0) = new_points.at<double>(j, 0) - translation.x;
            new_points.at<double>(j, 1) = new_points.at<double>(j, 1) - translation.y;
        }

        curShape.push_back( new_points );
        // method 2: use mean
//        curShape.push_back(ProjectToImageCoordinate(meanShape, boundingBoxes[i] ));

//        visualizeImage(images[i], ProjectToImageCoordinate(initial, boundingBoxes[i] ) , 0);
    }

    int visualIdx = 0;
    Mat_<double> initialShape = curShape[visualIdx].clone();

    for(int i = 0 ; i < first_level_regressor ; i ++){
        cout << "=================================" << endl;
        cout << "FIRST LEVEL " << i << endl;
        deltaShape = regressors[i].Train(images, keypoints, meanShape, boundingBoxes, curShape);

        for(int j = 0 ; j < curShape.size() ; j++){
            curShape[j] += deltaShape[j];
        }

        cout << "---------FIRST LEVEL ------------" << endl;
        cout << "INITIALSHAPE" << endl;
        cout << initialShape.t() << endl;
        cout << "CURSHAPE" << endl;
        cout << curShape[visualIdx].t() << endl;
        cout << "DELTASHAPE" << endl;
        cout << deltaShape[visualIdx].t() << endl;
        visualizeImage(images[visualIdx], curShape[visualIdx], 10);

        waitKey(10);
    }
}

Mat_<double> ShapeAlignment::Test(Mat_<unsigned char> &image, Rect_<int> &bounding_box) {
    Mat_<double> curShape;
    Mat_<double> deltaShape;

    // initialize curShape

    curShape = ProjectToImageCoordinate(meanShape, bounding_box );

    for(int i = 0 ; i < first_level_regressor ; i ++){
        deltaShape = regressors[i].Test(image, bounding_box, curShape, meanShape);

        curShape += deltaShape;
    }
    return curShape;
}