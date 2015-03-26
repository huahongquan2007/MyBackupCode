#include <strings.h>
#include "FernRegressor.h"
#include "utility.h"

FernRegressor::FernRegressor(int feature_per_fern) {
    FernRegressor::feature_per_fern = feature_per_fern;
}

vector<Mat_<double>> FernRegressor::Train(vector<Mat_<double>> regression_target, Mat_<double> covariance_matrix, Mat_<double> pixels, Mat_<double> pixelLocation, Mat_<int> nearestLandmarkOfPixel, bool isDebug) {

    if(isDebug) cout << "-FernRegressor: Train" << endl;

    int num_of_images = regression_target.size();
    int num_of_landmark = regression_target[0].rows;
    int num_of_random_pixels = covariance_matrix.rows;

    Mat_<int> max_corr_index ( feature_per_fern, 2 );

    RNG rng;
    for (int i = 0; i < feature_per_fern; i++) {
//        if(isDebug) cout << "FERN: " << i << endl;
        // create a random direction to project target -> scalar

        Mat_<double> random_direction(num_of_landmark, 2);
        rng.fill(random_direction, RNG::UNIFORM, -1.1, 1.1);
        normalize(random_direction, random_direction);

        // project target -> scalar
        Mat_<double> projected_target(1, num_of_images);
        for (int j = 0; j < num_of_images; j++) {
            projected_target(0, j) = (double) sum(regression_target[j].mul(random_direction))[0];
        }

        // calculate cov(i, y)
        Mat_<double> covariance_i_y(num_of_random_pixels, 1);

//        if(isDebug) cout << "PROJECTED: " << projected_target << endl;

        for (int j = 0; j < num_of_random_pixels; j++) {
            covariance_i_y(j, 0) = calculate_covariance(pixels.row(j), projected_target);
        }
        // calculate var(y)
        double var_y = calculate_covariance(projected_target, projected_target);

        // among P2 features, select a feature with highest correlation to the scalar
        double max_correlation = 0;
        int index_i = 0;
        int index_j = 0;
        for (int fi = 0; fi < num_of_random_pixels; fi++) {
            for (int fj = fi; fj < num_of_random_pixels; fj++) {
                double corr = (covariance_i_y(fi, 0) - covariance_i_y(fj, 0)) / (sqrt(var_y * (covariance_matrix(fi, fi) + covariance_matrix(fj, fj) - 2 * covariance_matrix(fi, fj))));

                corr = abs(corr);

                if(corr > max_correlation){
                    max_correlation = corr;
                    index_i = fi;
                    index_j = fj;
                }
            }
        }

        max_corr_index(i, 0) = index_i;
        max_corr_index(i, 1) = index_j;

        Mat_<double> pixelDiff = pixels.row(index_i) - pixels.row(index_j);
        pixelDiff = abs(pixelDiff);

        double min, max;
        minMaxLoc(pixelDiff, &min, &max);

//        double threshold = mean(pixelDiff)[0];
        double threshold = rng.uniform( max * -0.2, max * 0.2 );

        Mat_<double> location(2, 2, CV_32F); // x1 y1 ; x2 y2
        location(0, 0) = pixelLocation(index_i, 0);
        location(0, 1) = pixelLocation(index_i, 1);
        location(1, 0) = pixelLocation(index_j, 0);
        location(1, 1) = pixelLocation(index_j, 1);

        Mat_<int> nearestLandmark(2, 1, CV_32F);
        nearestLandmark(0, 0) = nearestLandmarkOfPixel(index_i, 0);
        nearestLandmark(1, 0) = nearestLandmarkOfPixel(index_j, 0);

//        if(isDebug) cout << "PIXEL DIFF: " << pixelDiff << endl;
//        if(isDebug) cout << "THRESHOLD " << threshold << endl;
//        if(isDebug) cout << "LOCATION: " << location << endl;
//        if(isDebug) cout << "NEAREST: " << nearestLandmark << endl;

        fernThreshold.push_back(threshold);
        fernPairLocation.push_back(location);
        fernPairNearestLandmark.push_back(nearestLandmark);
    }

    // cluster shape into bins
    vector< vector<int> > bins_index;
    bins_index.resize( pow(2.0, feature_per_fern) );

    for(int i = 0 ; i < num_of_images; i++){
        int index = 0;

        for(int j = 0 ; j < feature_per_fern; j++){
            double pixel1 = pixels(max_corr_index(j, 0), i);
            double pixel2 = pixels(max_corr_index(j, 1), i);

            if(pixel1 - pixel2 >= fernThreshold[j]){
                index += pow(2.0, j);
            }
        }
        bins_index[index].push_back(i);
    }

    regression_output.resize(bins_index.size()); // 2^F output

    // compute regression output
    cout << "BIN: ";
    for(int i = 0 ; i < bins_index.size() ; i++){
        int bin_size = bins_index[i].size();

        Mat_<double> result = Mat::zeros (num_of_landmark, 2, CV_32F);
        if(bin_size > 0){
            cout << "[" << i << "_"<< bin_size << "] ";
//            if(isDebug) cout << "BIN : " << i << endl;

            for(int j = 0 ; j < bin_size ; j++){
//                if(isDebug) cout << bins_index[i][j] << " " ;
                int shape_idx = bins_index[i][j];
                result += regression_target[shape_idx];
            }
            double ratio = (1 + 1000.0/ bin_size) * bin_size;
            result /= ratio;
//            if(isDebug) cout << endl;
        }

        regression_output[i] = result;
    }
    cout << endl;

    // compute output for each shape in training
    vector<Mat_<double>> deltaShape;
    deltaShape.resize(num_of_images);

    for(int i = 0 ; i < bins_index.size() ; i++) {
        for (int j = 0; j < bins_index[i].size(); j++) {
            int shape_idx = bins_index[i][j];
            deltaShape[shape_idx] = regression_output[i];
        }
    }

//    cout << "---------- DELTA SHAPE" << endl << deltaShape[0].t() << endl;
//    for(Mat_<double> r : deltaShape){
//        cout << r << endl;
//    }
    // to do:
    // 1. similarity transform
    // 2. how to deal with threshold & bins


    if( abs(deltaShape[0](0,0)) > 1){
        cout << "ERROR???" << endl;
        waitKey(0);
    }

    return deltaShape;
}


Mat_<double> FernRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape){
    cout << "FernRegressor: Test" << endl;


    // project keypoints to box
    Mat_<double> curKeyPoints = ProjectToBoxCoordinate(curShape, bounding_box);

    // find bin
    int index = 0;
    for (int i = 0; i < feature_per_fern; i++) {
        cout << "---Fern: " << i << endl;
        cout << fernThreshold[i] << endl;
        cout << fernPairLocation[i] << endl;
        cout << fernPairNearestLandmark[i] << endl;

        Mat_<double> curLocation ( 2, 1 );
        // Get Pixel 1
        int idx_landmark_1 = fernPairNearestLandmark[i](0, 0);

        cout << "IDX 1 " << idx_landmark_1 << endl;
        curLocation(0, 0) = fernPairLocation[i](0, 0) + curKeyPoints( idx_landmark_1, 0);
        curLocation(1, 0) = fernPairLocation[i](0, 1) + curKeyPoints( idx_landmark_1, 1);

        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_1 = (double) image.at<unsigned char>(curLocationImageCoor(0,0), curLocationImageCoor(0, 1));

        // Get Pixel 2
        int idx_landmark_2 = fernPairNearestLandmark[i](0, 1);
        cout << "IDX 2 " << idx_landmark_2 << endl;
        curLocation(0, 0) = fernPairLocation[i](1, 0) + curKeyPoints( idx_landmark_2, 0);
        curLocation(1, 0) = fernPairLocation[i](1, 1) + curKeyPoints( idx_landmark_2, 1);

        curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_2 = (double) image.at<unsigned char>(curLocationImageCoor(0,0), curLocationImageCoor(0, 1));

        cout << "PIXEL 1: " << pixel_1 << endl;
        cout << "PIXEL 2: " << pixel_2 << endl;

        if(pixel_1 - pixel_2 >= fernThreshold[i]){
            index += pow(2.0, i);
        }
    }

    cout << "BIN : " << index << endl;
    cout << "REGRESSION OUTPUT : " << regression_output[index].t() << endl;

    // return regression_output in box-coordinate
    return regression_output[index]; // box-coordinate
}