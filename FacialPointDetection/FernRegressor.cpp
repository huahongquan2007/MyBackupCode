#include <strings.h>
#include "FernRegressor.h"
#include "utility.h"
#include <fstream>

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
        if(isDebug) cout << "FERN: " << i << endl;
        // create a random direction to project target -> scalar

        Mat_<double> random_direction(num_of_landmark, 2);
        rng.fill(random_direction, RNG::UNIFORM, -1.1, 1.1);
        normalize(random_direction, random_direction);

        // project target -> scalar
        Mat_<double> projected_target(1, num_of_images);
        for (int j = 0; j < num_of_images; j++) {
            projected_target.at<double>(0, j) = (double) sum(regression_target[j].mul(random_direction))[0];
        }

        // calculate cov(i, y)
        Mat_<double> covariance_i_y(num_of_random_pixels, 1);

//        if(isDebug) cout << "PROJECTED: " << projected_target << endl;

        for (int j = 0; j < num_of_random_pixels; j++) {
            covariance_i_y.at<double>(j, 0) = calculate_covariance(pixels.row(j), projected_target);
        }
        // calculate var(y)
        double var_y = calculate_covariance(projected_target, projected_target);

        // among P2 features, select a feature with highest correlation to the scalar
        double max_correlation = 0;
        int index_i = 0;
        int index_j = 0;
        for (int fi = 0; fi < num_of_random_pixels; fi++) {
            for (int fj = fi; fj < num_of_random_pixels; fj++) {
                double corr = (covariance_i_y.at<double>(fi, 0) - covariance_i_y.at<double>(fj, 0)) / (sqrt(var_y * (covariance_matrix.at<double>(fi, fi) + covariance_matrix.at<double>(fj, fj) - 2 * covariance_matrix.at<double>(fi, fj))));

                corr = abs(corr);

                if(corr > max_correlation){
                    max_correlation = corr;
                    index_i = fi;
                    index_j = fj;
                }
            }
        }

        max_corr_index.at<int>(i, 0) = index_i;
        max_corr_index.at<int>(i, 1) = index_j;

        Mat_<double> pixelDiff = pixels.row(index_i) - pixels.row(index_j);
        pixelDiff = abs(pixelDiff);

        double min, max;
        minMaxLoc(pixelDiff, &min, &max);

//        double threshold = mean(pixelDiff)[0];
        double threshold = rng.uniform( max * -0.2, max * 0.2 );

        Mat_<double> location(2, 2, CV_32F); // x1 y1 ; x2 y2
        location.at<double>(0, 0) = pixelLocation.at<double>(index_i, 0);
        location.at<double>(0, 1) = pixelLocation.at<double>(index_i, 1);
        location.at<double>(1, 0) = pixelLocation.at<double>(index_j, 0);
        location.at<double>(1, 1) = pixelLocation.at<double>(index_j, 1);

        Mat_<int> nearestLandmark(2, 1, CV_32F);
        nearestLandmark.at<int>(0, 0) = nearestLandmarkOfPixel.at<double>(index_i, 0);
        nearestLandmark.at<int>(1, 0) = nearestLandmarkOfPixel.at<double>(index_j, 0);

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
            double pixel1 = pixels.at<double>(max_corr_index.at<int>(j, 0), i);
            double pixel2 = pixels.at<double>(max_corr_index.at<int>(j, 1), i);

            if(pixel1 - pixel2 >= fernThreshold[j]){
                index += pow(2.0, j);
            }
        }
        bins_index[index].push_back(i);
    }

    regression_output.resize(bins_index.size()); // 2^F output

    // compute regression output
    if(isDebug) cout << "BIN: ";
    for(int i = 0 ; i < bins_index.size() ; i++){
        int bin_size = bins_index[i].size();

        Mat_<double> result = Mat::zeros (num_of_landmark, 2, CV_32F);
        if(bin_size > 0){
            if(isDebug) cout << "[" << i << "_"<< bin_size << "] ";
//            if(isDebug) cout << "BIN : " << i << endl;

            for(int j = 0 ; j < bin_size ; j++){
//                if(isDebug) cout << bins_index[i][j] << " " ;
                int shape_idx = bins_index[i][j];
                result += regression_target[shape_idx];
            }
            double ratio = (1 + 100.0/ bin_size) * bin_size;
//            double ratio = bin_size;
            result /= ratio;
//            if(isDebug) cout << endl;
        }

        regression_output[i] = result;
    }
    if(isDebug) cout << endl;

    int visualIdx = 0;

    // compute output for each shape in training
    vector<Mat_<double>> deltaShape;
    deltaShape.resize(num_of_images);

    for(int i = 0 ; i < bins_index.size() ; i++) {
        for (int j = 0; j < bins_index[i].size(); j++) {
            int shape_idx = bins_index[i][j];
            deltaShape[shape_idx] = regression_output[i];

            if(shape_idx == visualIdx){
                cout << "BIN: " << i << "/" << bins_index[i].size() << endl;
            }

        }
    }



//    cout << "---------- DELTA SHAPE" << endl << deltaShape[0].t() << endl;
//    for(Mat_<double> r : deltaShape){
//        cout << r << endl;
//    }
    // to do:
    // 1. similarity transform
    // 2. how to deal with threshold & bins

    return deltaShape;
}


Mat_<double> FernRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape, Mat_<double> meanShape){
    // project keypoints to box
    Mat_<double> curKeyPoints = ProjectToBoxCoordinate(curShape, bounding_box);

    // align with meanshape
    Mat_<double> rotationMatrix(2,2 , CV_32FC1);
    double scale = 1.0;
    similarity_transform(meanShape, curKeyPoints, rotationMatrix, scale);

    curKeyPoints = ( rotationMatrix * curKeyPoints.t() * scale ).t();

    // find bin
    int index = 0;
    for (int i = 0; i < feature_per_fern; i++) {

        Mat_<double> curLocation ( 2, 1 );
        // Get Pixel 1
        int idx_landmark_1 = fernPairNearestLandmark[i].at<int>(0, 0);
        curLocation.at<double>(0, 0) = fernPairLocation[i].at<double>(0, 0) + curKeyPoints.at<double>( idx_landmark_1, 0);
        curLocation.at<double>(1, 0) = fernPairLocation[i].at<double>(0, 1) + curKeyPoints.at<double>( idx_landmark_1, 1);

        curLocation = rotationMatrix.t() * curLocation / scale;

        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_1 = (double) image.at<unsigned char>(curLocationImageCoor.at<double>(0,0), curLocationImageCoor.at<double>(0, 1));

        // Get Pixel 2
        int idx_landmark_2 = fernPairNearestLandmark[i].at<int>(0, 1);
        curLocation.at<double>(0, 0) = fernPairLocation[i].at<double>(1, 0) + curKeyPoints.at<double>( idx_landmark_2, 0);
        curLocation.at<double>(1, 0) = fernPairLocation[i].at<double>(1, 1) + curKeyPoints.at<double>( idx_landmark_2, 1);

        curLocation = rotationMatrix.t() * curLocation / scale;

        curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_2 = (double) image.at<unsigned char>(curLocationImageCoor.at<double>(0,0), curLocationImageCoor.at<double>(0, 1));

        if(pixel_1 - pixel_2 >= fernThreshold[i]){
            index += pow(2.0, i);
        }
    }
    // return regression_output in box-coordinate
    Mat_<double> output = ( rotationMatrix.t() * regression_output[index].t() / scale ).t();

    return output; // box-coordinate
}

void FernRegressor::Save(FileStorage &out){
    out << "feature_per_fern" << feature_per_fern;

    for(int i = 0 ; i < fernThreshold.size() ; i++){
        string name = "fern_threshold_";
        name += to_string(i);
        out << name << fernThreshold[i];
    }

    out << "fernPairLocationSize" << (int)fernPairLocation.size();

    for(int i = 0 ; i < fernPairLocation.size(); i++){
        string name = "fern_pair_location_";
        name += to_string(i);
        out << name << fernPairLocation[i];
    }

    out << "fernPairNearestLandmarkSize" << (int) fernPairNearestLandmark.size();
    for(int i = 0; i < fernPairNearestLandmark.size(); i++){
        string name = "fern_pair_nearest_landmark_";
        name += to_string(i);
        out << name << fernPairNearestLandmark[i];
    }

    out << "regressionOutputSize" << (int) regression_output.size();
    for(int i = 0; i < regression_output.size(); i++){
        string name = "regression_output_";
        name += to_string(i);
        out << name << regression_output[i];
    }
//    vector<Mat_<double>> regression_output;
}
void FernRegressor::Load(FileNode in){
    in["feature_per_fern"] >> feature_per_fern;

    fernThreshold.resize(feature_per_fern);

    for(int i = 0 ; i < fernThreshold.size() ; i++){
        string name = "fern_threshold_";
        name += to_string(i);
        in[name] >> fernThreshold[i];
    }

    int fernPairLocationSize = 0;
    in["fernPairLocationSize"] >> fernPairLocationSize;

    fernPairLocation.resize(fernPairLocationSize);
    for(int i = 0 ; i < fernPairLocation.size(); i++){
        string name = "fern_pair_location_";
        name += to_string(i);
        in[name] >> fernPairLocation[i];
    }

    int fernPairNearestLandmarkSize = 0;
    in["fernPairNearestLandmarkSize"] >> fernPairNearestLandmarkSize;

    fernPairNearestLandmark.resize(fernPairNearestLandmarkSize);
    for(int i = 0; i < fernPairNearestLandmark.size(); i++){
        string name = "fern_pair_nearest_landmark_";
        name += to_string(i);
        in[name] >> fernPairNearestLandmark[i];
    }

    int regressionOutputSize = 0;
    in["regressionOutputSize"] >> regressionOutputSize;
    regression_output.resize(regressionOutputSize);

    for(int i = 0; i < regression_output.size(); i++){
        string name = "regression_output_";
        name += to_string(i);
        in[name] >> regression_output[i];
    }

    cout << "REGRESISON OUTPUT" << endl;
    cout << regression_output[0].t() << endl;
}