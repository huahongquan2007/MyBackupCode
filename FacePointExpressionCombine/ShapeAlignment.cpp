#include <opencv2/opencv.hpp>
#include "ShapeAlignment.h"
#include "utility.h"
#include <iomanip>

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

    cout << first_level_regressor << endl;
    cout << second_level_regressor << endl;
    cout << num_of_feature_per_fern << endl;
    cout << num_of_random_pixel << endl;
    cout << meanShape.rows << endl;
    cout << meanShape.t() << endl;


    FileStorage out(destination, FileStorage::WRITE);

//    ofstream out(destination, ofstream::out);
    out << "first_level_regressor" << first_level_regressor;
    out << "second_level_regressor" << second_level_regressor;
    out << "num_of_feature_per_fern" << num_of_feature_per_fern;
    out << "num_of_random_pixel" << num_of_random_pixel;

    out << "meanShape" << meanShape;

    out << "regressors_size" << (int)regressors.size();

    for(int i = 0 ; i < regressors.size(); i++){
        string name = "regressors_";
        name += to_string(i);
        out << name << "{";
        regressors[i].Save(out);
        out << "}";
    }

    out.release();
}
void ShapeAlignment::Load(string destination) {
    cout << "LOAD MODEL: " << destination << endl;

    FileStorage in(destination, FileStorage::READ);

    in["first_level_regressor"] >> first_level_regressor;
    in["second_level_regressor"] >> second_level_regressor;
    in["num_of_feature_per_fern"] >> num_of_feature_per_fern;
    in["num_of_random_pixel"] >> num_of_random_pixel;

    in["meanShape"] >> meanShape;


    cout << first_level_regressor << endl;
    cout << second_level_regressor << endl;
    cout << num_of_feature_per_fern << endl;
//    cout << num_of_random_pixel << endl;
//    cout << meanShape.rows << endl;
//    cout << meanShape.t() << endl;

    int regressors_size = 0;
    in["regressors_size"] >> regressors_size;

    for(int i = 0 ; i < regressors_size; i++){
        string name = "regressors_";
        name += to_string(i);

        regressors[i].Load(in[name]);
    }

    in.release();
}
void ShapeAlignment::Train(){
    cout << "TRAIN SHAPE MODEL" << endl;

    vector<Mat_<double>> curShape;
    vector<Mat_<double>> deltaShape;

    // generate more image, keypoints, curShape & inputShape
    // Use boundingBox to generate initialized locations for training data
    RNG rng;

    int total_image_original = images.size();

    for(int i = 0 ; i < images.size(); i++){
        // method 1: random
        int index = i;
        while(index == i){
            index = rng.uniform(0, images.size() - 1);
        }

        curShape.push_back( ProjectToImageCoordinate(ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]), boundingBoxes[i] ) );
    }
    cout << "before augment images" << endl;
    for(int i = 0 ; i < total_image_original; i++){
        cout << i << "/" << total_image_original << " " << images[i].size()<< endl;
        for(int j = 0 ; j < 13 ; j ++){

            // method 1: random
            int index = i;
            while(index == i){
                index = rng.uniform(0, total_image_original - 1);
            }

            curShape.push_back( ProjectToImageCoordinate(ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]), boundingBoxes[i] ) );

            images.push_back(images[i].clone());
            keypoints.push_back(keypoints[i].clone());
            boundingBoxes.push_back(boundingBoxes[i]);
        }
    }

    meanShape = GetMeanShape(keypoints, boundingBoxes);

    for(int i = 0 ; i < first_level_regressor ; i ++){
        cout << "=================================" << endl;
        cout << "FIRST LEVEL " << i << endl;

        deltaShape = regressors[i].Train(images, keypoints, meanShape, boundingBoxes, curShape);

        for(int j = 0 ; j < curShape.size() ; j++){
            curShape[j] = ProjectToBoxCoordinate(curShape[j], boundingBoxes[j]) + deltaShape[j];
            curShape[j] = ProjectToImageCoordinate(curShape[j], boundingBoxes[j]);
        }
        visualizeImage(images[0], curShape[0], 1, false, "first_level");
    }
}

Mat_<double> ShapeAlignment::Test(Mat_<unsigned char> &image, Rect_<int> &bounding_box) {

    Mat_<double> deltaShape;
    vector<Mat_<double>> result;

    // initialize curShape
    RNG rng;
    int total_test = 20;

    Mat_<double> resultShape = Mat::zeros(keypoints[0].size(), keypoints[0].type());
    for(int resultID = 0 ; resultID < total_test ; resultID++){
        Mat_<double> curShape;

        // method 1: random
        int index = rng.uniform(0, keypoints.size() - 1);
        Mat_<double> rand_shape = ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]);
        curShape = ProjectToImageCoordinate(rand_shape, bounding_box );

//        cout << "ShapeAlignment: Test" << endl;
//        Mat_<double> initial = curShape.clone();
//        visualizeImage(image, curShape, 1, false, "initialize");

        for(int i = 0 ; i < first_level_regressor ; i ++){
//            cout << "ShapeAlignment: Test first level " << i << endl;

            deltaShape = regressors[i].Test(image, bounding_box, curShape, meanShape);
            curShape += deltaShape;
        }

        resultShape += curShape;
//        Mat tImg = image.clone();
//        for(int i = 0 ; i < initial.rows ; i++){
//            int x = (int) initial.at<double>(i, 0);
//            int y = (int) initial.at<double>(i, 1);
//            circle(tImg, Point(x, y), 1, Scalar(255, 0, 255), -1);
//        }
//        visualizeImage(tImg, curShape, 1, false, "test_result");
//
//        result[resultID] = curShape;
//        cout << "RESULT ID: " << resultID << endl;
//        cout << curShape.t() << endl;
    }


    resultShape = 1.0 / total_test * resultShape;

//    cout << resultShape.t() << endl;
//    visualizeImage(image, resultShape, 10, false, "final mean 10");

    return resultShape;
}