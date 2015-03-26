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
    Mat_<double> shapeIndexPixels (num_of_random_pixels, num_of_images);

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
            shapeIndexPixels(j, i) = (double) images[i].at<unsigned char>(curLocationImageCoor(j,0), curLocationImageCoor(j, 1));
        }
    }
//    cout << "Shape Index Pixels: " << shapeIndexPixels << endl;

    // Calculate covariance between i, j
    Mat_<double> covariance_matrix(num_of_random_pixels, num_of_random_pixels);
    for(int i = 0 ; i < num_of_random_pixels; i++){
        for(int j = i ; j < num_of_random_pixels; j++){
            double cov_value = calculate_covariance( shapeIndexPixels.row(i), shapeIndexPixels.row(j) );
            covariance_matrix(i, j) = cov_value;
            covariance_matrix(j, i) = cov_value;
        }
    }

    vector< Mat_<double> > regression_target;
    vector< Mat_<double> > regression_output;
    for(int i = 0 ; i < num_of_images; i ++){
        regression_target.push_back( ProjectToBoxCoordinate( keypoints[i], boundingBoxes[i] ) - ProjectToBoxCoordinate( inputShape[i], boundingBoxes[i] ) );

        regression_output.push_back(Mat::zeros(inputShape[i].size(), CV_32F ));
    }

    int visualIdx = 0;
    Mat_<double> initialShape = inputShape[visualIdx].clone();
    Mat_<double> resultShape = inputShape[visualIdx].clone();

    for(int i = 0 ; i < childRegressor.size(); i++){

        cout << "------------ BEGIN [" << i << "]-------------" << endl;
        cout << "REGRESSION TARGET: " << endl;
        cout << regression_target[visualIdx].t() << endl;

        // Train each child-level regressor

        deltaShape = childRegressor[i].Train(regression_target, covariance_matrix, shapeIndexPixels, shapeIndexLocation, shapeIndexNearestLandmark);

        for(int j = 0 ; j < regression_output.size() ; j++){
            regression_target[j] -= deltaShape[j];
            //regression_output[j] += deltaShape[j];
            regression_output[j] = ProjectToImageCoordinate(
                    ProjectToBoxCoordinate( inputShape[visualIdx] , boundingBoxes[visualIdx] ) - regression_target[j],
                    boundingBoxes[visualIdx]) - inputShape[visualIdx];
        }
//        cout << "------------------------------" << endl;
        cout << "Initial SHAPE: " << endl;
        cout << initialShape.t() << endl;
//        cout << "Initial SHAPE (PROJECT): " << endl;
//        cout << ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) << endl;
//        cout << "DELTA SHAPE: " << endl;
//        cout << deltaShape[visualIdx] << endl;
//        cout << "BOUNDINGBOXES: " << boundingBoxes[visualIdx] << endl;
//        cout << "INITIAL + DELTA SHAPE: " << endl;
//        cout << ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + deltaShape[visualIdx] << endl;
//        cout << "INITIAL + DELTA SHAPE (PROJECT): " << endl;
//        cout << ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_output[visualIdx], boundingBoxes[visualIdx]) << endl;
//        cout << "INITIAL + DELTA SHAPE (ORIGINAL): " << endl;
//        cout << ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_output[visualIdx], boundingBoxes[visualIdx]) - initialShape << endl;
        cout << "REGRESSION TARGET: " << endl;
        cout << regression_target[visualIdx].t() << endl;
        cout << "DELTA SHAPE: " << endl;
        cout << deltaShape[visualIdx].t() << endl;
        cout << "REGRESSION OUTPUT: " << endl;
        cout << regression_output[visualIdx].t() << endl;
        resultShape = initialShape - regression_output[visualIdx];
        cout << "RESULT SHAPE: " << endl;
        cout << resultShape.t() << endl;
//        resultShape = ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_output[visualIdx], boundingBoxes[visualIdx]);
        visualizeImageCompare(images[visualIdx], resultShape, initialShape, 5);
    }

    cout << "REGRESSION OUTPUT - BEFORE[0] : " << endl;
    cout << regression_output[visualIdx].t() << endl;

    return regression_output;
}

Mat_<double> NormalRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape){
    cout << "NormalRegressor: Test" << endl;

    Mat_<double> regression_output;
    Mat_<double> deltaShape;
    Mat_<double> inputShape = curShape.clone();

    for(int i = 0 ; i < childRegressor.size(); i++){
        deltaShape = childRegressor[i].Test(image, bounding_box, inputShape);
        regression_output = ProjectToImageCoordinate(
                ProjectToBoxCoordinate( inputShape , bounding_box ) - deltaShape,
                bounding_box) - inputShape;

        inputShape -= regression_output;

        cout << "FINISH CHILD " << i << endl;
        cout << "DeltaShape" << endl;
        cout << deltaShape.t() << endl;
        cout << "INPUTSHAPE " << endl;
        cout << inputShape.t() << endl;
        cout << "REGRESSION OUTPUT: " << endl;
        cout << regression_output.t() << endl;

        visualizeImage(image, inputShape, 10);
    }

    return regression_output;
}