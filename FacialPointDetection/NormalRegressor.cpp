//
// Created by robotbase on 20/03/2015.
//

#include "NormalRegressor.h"
#include "utility.h"

NormalRegressor::NormalRegressor(int child_level, int feature_per_fern, int num_of_random_pixels) {

    NormalRegressor::num_of_random_pixels = num_of_random_pixels;

    for(int i = 0 ; i < child_level ; i ++ ){
        FernRegressor curRegressor ( feature_per_fern ) ;
        childRegressor.push_back(curRegressor);
    }
}

vector<Mat_<double>> NormalRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, Mat_<double> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<double>> inputShape){
    cout << "NormalRegressor: Train" << endl;

    // Variable
    int num_of_images = images.size();
    vector<Mat_<double>> curShape = inputShape;

    Mat_<double> shapeIndexLocation (num_of_random_pixels, 2);
    Mat_<int> shapeIndexNearestLandmark (num_of_random_pixels, 1);
    vector<Mat_<double>> deltaShape;

    // Random P pixels

    RNG rng;
    double rand_x , rand_y;
    for(int i = 0 ; i < num_of_random_pixels ; i ++){
        rand_x = rng.uniform(-1.0, 1.0);
        rand_y = rng.uniform(-1.0, 1.0);

        // Find nearest landmark
        double min_dist = 1e10;
        int min_idx = -1;
        for(int j = 0 ; j < meanShape.rows; j++){
            double cur_dist = sqrt( pow( ( meanShape(j,0) - rand_x ), 2 ) + pow( ( meanShape(j,1) - rand_y ), 2 ) );
            if( cur_dist < min_dist ){
                min_dist = cur_dist;
                min_idx = j;
            }
        }

        shapeIndexLocation(i, 0) = rand_x - meanShape(min_idx, 0);
        shapeIndexLocation(i, 1) = rand_y - meanShape(min_idx, 1);
        shapeIndexNearestLandmark(i, 0) = min_idx;
    }

    // Get pixel intensity of each P pixels over training set
    Mat_<unsigned char> shapeIndexPixels (num_of_random_pixels, num_of_images);

    for(int i = 0 ; i < num_of_images ; i++){
        // project keypoints to box
        Mat_<double> curKeyPoints = ProjectToBoxCoordinate(keypoints[i], boundingBoxes[i]);
        Mat_<double> curLocation (num_of_random_pixels, 2);

        // calculate shapeIndexLocation
        for(int j = 0 ; j < num_of_random_pixels; j++){
            int idx_landmark = shapeIndexNearestLandmark(j, 0);
            curLocation(j , 0 ) = shapeIndexLocation(j , 0) + curKeyPoints(idx_landmark, 0);
            curLocation(j , 1 ) = shapeIndexLocation(j , 1) + curKeyPoints(idx_landmark, 1);
        }
        // project location to image coordinates
        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, boundingBoxes[i]);

        // get pixels
        for(int j = 0 ; j < num_of_random_pixels; j++){
            shapeIndexPixels(j, i) = images[i].at<unsigned char>(curLocationImageCoor(j,0), curLocationImageCoor(j, 1));
        }
    }
    cout << "Shape Index Pixels: " << shapeIndexPixels << endl;
    // Calculate covariance between features: X

    for(FernRegressor &child : childRegressor){

        // Train each child-level regressor

        deltaShape = child.Train(images, keypoints, curShape, shapeIndexLocation);

        for(int j = 0 ; j < curShape.size() ; j++){
            curShape[j] += deltaShape[j];
        }
    }

    return curShape;
}