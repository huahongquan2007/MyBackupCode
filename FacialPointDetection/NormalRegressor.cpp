#include "NormalRegressor.h"
#include "utility.h"
#include <opencv2/opencv.hpp>

NormalRegressor::NormalRegressor(int child_level, int feature_per_fern, int num_of_random_pixels) {

    NormalRegressor::num_of_random_pixels = num_of_random_pixels;

    for(int i = 0 ; i < child_level ; i ++ ){
        FernRegressor curRegressor ( feature_per_fern ) ;
        childRegressor.push_back(curRegressor);
    }
}

vector<Mat_<double>> NormalRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<double>> keypoints, Mat_<double> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<double>> inputShape, bool isDebug ){
    if(isDebug) cout << "NormalRegressor: Train" << endl;

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
            double cur_dist = sqrt( pow( ( meanShape.at<double>(j, 0) - rand_x ), 2 ) + pow( ( meanShape.at<double>(j, 1) - rand_y ), 2 ) );
            if( cur_dist < min_dist ){
                min_dist = cur_dist;
                min_idx = j;
            }
        }

        shapeIndexLocation.at<double>(i, 0) = rand_x - meanShape.at<double>(min_idx, 0);
        shapeIndexLocation.at<double>(i, 1) = rand_y - meanShape.at<double>(min_idx, 1);
        shapeIndexNearestLandmark.at<double>(i, 0) = min_idx;
    }

    // Get pixel intensity of each P pixels over training set
    if(isDebug) cout << "Get Pixel Intensity" << endl;
    Mat_<double> shapeIndexPixels (num_of_random_pixels, num_of_images);
    vector<Mat_<double>> rotationMatrixArray;
    vector<double> scaleArray;
    rotationMatrixArray.resize(num_of_images);
    scaleArray.resize(num_of_images);

    for(int i = 0 ; i < num_of_images ; i++){
        // project keypoints to box
        Mat_<double> curKeyPoints = ProjectToBoxCoordinate(keypoints[i], boundingBoxes[i]);

        // align with meanshape
        similarity_transform(meanShape, curKeyPoints, rotationMatrixArray[i], scaleArray[i]);

        curKeyPoints = (rotationMatrixArray[i] * curKeyPoints.t() * scaleArray[i]).t();

        Mat_<double> curLocation (num_of_random_pixels, 2);

        // calculate shapeIndexLocation
        for(int j = 0 ; j < num_of_random_pixels; j++){
            int idx_landmark = shapeIndexNearestLandmark.at<double>(j, 0);
            curLocation.at<double>(j , 0 ) = shapeIndexLocation.at<double>(j , 0) + curKeyPoints.at<double>(idx_landmark, 0);
            curLocation.at<double>(j , 1 ) = shapeIndexLocation.at<double>(j , 1) + curKeyPoints.at<double>(idx_landmark, 1);
        }
        // project location to image coordinates
//        Mat_<double> curLocationImageCoor = ( rotationMatrixArray[i].t() * ProjectToImageCoordinate(curLocation, boundingBoxes[i]).t() / scaleArray[i] ).t();

        curLocation = (rotationMatrixArray[i].t() * curLocation.t() / scaleArray[i] ).t();
        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, boundingBoxes[i]);

        // get pixels
        for(int j = 0 ; j < num_of_random_pixels; j++){
            shapeIndexPixels.at<double>(j, i) = (double) images[i].at<unsigned char>(curLocationImageCoor.at<double>(j,0), curLocationImageCoor.at<double>(j, 1));
        }
    }
//    cout << "Shape Index Pixels: " << shapeIndexPixels << endl;

    // Calculate covariance between i, j
    Mat_<double> covariance_matrix(num_of_random_pixels, num_of_random_pixels);
    for(int i = 0 ; i < num_of_random_pixels; i++){
        for(int j = i ; j < num_of_random_pixels; j++){
            double cov_value = calculate_covariance( shapeIndexPixels.row(i), shapeIndexPixels.row(j) );
            covariance_matrix.at<double>(i, j) = cov_value;
            covariance_matrix.at<double>(j, i) = cov_value;
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

        if(isDebug) cout << "------------ BEGIN [" << i << "]-------------" << endl;
        if(isDebug) cout << "REGRESSION TARGET: " << endl;
        if(isDebug) cout << regression_target[visualIdx].t() << endl;

        // Train each child-level regressor

        deltaShape = childRegressor[i].Train(regression_target, covariance_matrix, shapeIndexPixels, shapeIndexLocation, shapeIndexNearestLandmark);

        for(int j = 0 ; j < num_of_images ; j++){
            regression_target[j] -= (rotationMatrixArray[j].t() * deltaShape[j].t() / scaleArray[j]).t();
//            regression_output[j] -= deltaShape[j];
            regression_output[j] += ProjectToImageCoordinate(ProjectToBoxCoordinate( inputShape[j] , boundingBoxes[j] ) + (rotationMatrixArray[j].t() * deltaShape[j].t() / scaleArray[j]).t(), boundingBoxes[j]) - inputShape[j];
// /            regression_output[j] = ProjectToImageCoordinate(
//                    ProjectToBoxCoordinate( inputShape[j] , boundingBoxes[j] ) + (rotationMatrixArray[j].t() * deltaShape[j].t() / scaleArray[j]).t(),
//                    boundingBoxes[j])  - inputShape[j];
        }
        cout << "------------------------------" << endl;
        if(isDebug){

            cout << "Initial SHAPE: " << endl;
            cout << initialShape.t() << endl;
            cout << "Initial SHAPE (PROJECT): " << endl;
            cout << ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ).t() << endl;
            cout << "DELTA SHAPE: " << endl;
            cout << deltaShape[visualIdx].t() << endl;
            cout << "BOUNDINGBOXES: " << boundingBoxes[visualIdx] << endl;
            cout << "INITIAL + DELTA SHAPE: " << endl;
            cout << (ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + deltaShape[visualIdx]).t() << endl;
            cout << "INITIAL + DELTA SHAPE (PROJECT): " << endl;
            cout << (ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_target[visualIdx], boundingBoxes[visualIdx])).t() << endl;
            cout << "INITIAL + DELTA SHAPE (ORIGINAL): " << endl;
            cout << (ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_target[visualIdx], boundingBoxes[visualIdx]) - initialShape).t() << endl;
            cout << "REGRESSION TARGET: " << endl;
            cout << regression_target[visualIdx].t() << endl;
            cout << "DELTA SHAPE: " << endl;
            cout << deltaShape[visualIdx].t() << endl;
            cout << "REGRESSION OUTPUT: " << endl;
            cout << regression_output[visualIdx].t() << endl;
            resultShape = initialShape + regression_output[visualIdx];
            cout << "Initial SHAPE: " << endl;
            cout << initialShape.t() << endl;
            cout << "RESULT SHAPE: " << endl;
            cout << resultShape.t() << endl;
            cout << "GROUND TRUTH SHAPE: " << endl;
            cout << keypoints[visualIdx].t() << endl;
        }

        resultShape = inputShape[visualIdx] + regression_output[visualIdx];
        if(isDebug) visualizeImageCompare(images[visualIdx], resultShape, initialShape, 10);
    }

    if(isDebug) cout << "REGRESSION OUTPUT - BEFORE[0] : " << endl;
    if(isDebug) cout << regression_output[visualIdx].t() << endl;

    return regression_output;
}

Mat_<double> NormalRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape, Mat_<double> meanShape){
    Mat_<double> regression_output = Mat::zeros(curShape.size(), curShape.type());
    Mat_<double> deltaShape;
    Mat_<double> inputShape = curShape.clone();

    for(int i = 0 ; i < childRegressor.size(); i++){
        deltaShape = childRegressor[i].Test(image, bounding_box, inputShape, meanShape);
        regression_output = ProjectToImageCoordinate(
                ProjectToBoxCoordinate( inputShape , bounding_box ) - deltaShape,
                bounding_box) - inputShape;

        inputShape -= regression_output;

        visualizeImage(image, inputShape, 10);
    }

    return curShape - inputShape;
}