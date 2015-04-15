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
            projected_target.at<double>(0, j) = sum(regression_target[j].mul(random_direction))[0];
        }

        // calculate cov(i, y)
        Mat_<double> covariance_i_y(num_of_random_pixels, 1);

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
//                double corr = (covariance_i_y.at<double>(fi, 0) - covariance_i_y.at<double>(fj, 0)) / (sqrt(var_y * (covariance_matrix.at<double>(fi, fi) + covariance_matrix.at<double>(fj, fj) - 2 * covariance_matrix.at<double>(fi, fj))));
//                corr = abs(corr);
//
//                if(corr > max_correlation){
//                    max_correlation = corr;
//                    index_i = fi;
//                    index_j = fj;
//                }
                double temp1 = covariance_matrix.at<double>(fi, fi) + covariance_matrix.at<double>(fj, fj) - 2 * covariance_matrix.at<double>(fi, fj);
                if(abs(temp1) < 1e-10){
                    continue;
                }
                bool isSelected = false;
                for(int p = 0;p < fi;p++){
                    if(fi == max_corr_index.at<int>(p,0) && fj == max_corr_index.at<int>(p,1)){
                        isSelected = true;
                        break;
                    }else if(fi == max_corr_index.at<int>(p,1) && fj == max_corr_index.at<int>(p,0)){
                        isSelected = true;
                        break;
                    }
                }
                if(isSelected){
                    continue;
                }
                double temp = (covariance_i_y.at<double>(fi, 0) - covariance_i_y.at<double>(fj, 0))
                              / sqrt(temp1);
                if(abs(temp) > max_correlation){
                    max_correlation = temp;
                    index_i = fi;
                    index_j = fj;
                }
            }
        }
        max_corr_index.at<int>(i, 0) = index_i;
        max_corr_index.at<int>(i, 1) = index_j;

        double min, max;
        Mat_<double> pixelDiff = pixels.row(index_i) - pixels.row(index_j);
        pixelDiff = abs(pixelDiff);
        minMaxLoc(pixelDiff, &min, &max);

        cout << "max: " << max << endl;
        double threshold = rng.uniform( max * -0.2, max * 0.2 );

        Mat_<double> location(2, 2, CV_32F); // x1 y1 ; x2 y2
        location.at<double>(0, 0) = pixelLocation.at<double>(index_i, 0);
        location.at<double>(0, 1) = pixelLocation.at<double>(index_i, 1);
        location.at<double>(1, 0) = pixelLocation.at<double>(index_j, 0);
        location.at<double>(1, 1) = pixelLocation.at<double>(index_j, 1);

        Mat_<int> nearestLandmark(2, 1, CV_32F);
        nearestLandmark.at<int>(0, 0) = nearestLandmarkOfPixel.at<int>(index_i, 0);
        nearestLandmark.at<int>(1, 0) = nearestLandmarkOfPixel.at<int>(index_j, 0);

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
            for(int j = 0 ; j < bin_size ; j++){
                int shape_idx = bins_index[i][j];
                result += regression_target[shape_idx];

            }

            result = 1.0/((1.0 + 1000.0/bin_size) * bin_size) * result;
        }

        regression_output[i] = result;
    }
    if(isDebug) cout << endl;

    // compute output for each shape in training
    vector<Mat_<double>> deltaShape;
    deltaShape.resize(num_of_images);

    for(int i = 0 ; i < bins_index.size() ; i++) {
        for (int j = 0; j < bins_index[i].size(); j++) {
            int shape_idx = bins_index[i][j];
            deltaShape[shape_idx] = regression_output[i];

        }
    }

    return deltaShape;
}


Mat_<double> FernRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape, Mat_<double> meanShape){
    // project keypoints to box
    Mat_<double> curKeyPoints = ProjectToBoxCoordinate(curShape, bounding_box);

    // align with meanshape
    Mat_<double> rotationMatrix;
    double scale = 0.0;
    similarity_transform(meanShape, curKeyPoints, rotationMatrix, scale);
    transpose(rotationMatrix, rotationMatrix);

    curKeyPoints = scale * curKeyPoints * rotationMatrix;

    similarity_transform(ProjectToBoxCoordinate(curShape, bounding_box), meanShape, rotationMatrix, scale);
    transpose(rotationMatrix, rotationMatrix);

    // find bin
    int index = 0;
    for (int i = 0; i < feature_per_fern; i++) {

        Mat_<double> curLocation ( 1, 2 );
        // Get Pixel 1
        int idx_landmark_1 = fernPairNearestLandmark[i].at<int>(0, 0);
        curLocation.at<double>(0, 0) = fernPairLocation[i].at<double>(0, 0) + curKeyPoints.at<double>( idx_landmark_1, 0);
        curLocation.at<double>(0, 1) = fernPairLocation[i].at<double>(0, 1) + curKeyPoints.at<double>( idx_landmark_1, 1);

        curLocation = scale * curLocation * rotationMatrix;

        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_1 = (double) image.at<unsigned char>((int)curLocationImageCoor.at<double>(0,1),(int) curLocationImageCoor.at<double>(0, 0));

        // Get Pixel 2
        int idx_landmark_2 = fernPairNearestLandmark[i].at<int>(0, 1);
        curLocation.at<double>(0, 0) = fernPairLocation[i].at<double>(1, 0) + curKeyPoints.at<double>( idx_landmark_2, 0);
        curLocation.at<double>(0, 1) = fernPairLocation[i].at<double>(1, 1) + curKeyPoints.at<double>( idx_landmark_2, 1);

        curLocation = scale * curLocation * rotationMatrix;

        curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        double pixel_2 = (double) image.at<unsigned char>((int)curLocationImageCoor.at<double>(0,1), (int)curLocationImageCoor.at<double>(0, 0));

        if(pixel_1 - pixel_2 >= fernThreshold[i]){
            index += pow(2.0, i);
        }
    }

    // return regression_output in box-coordinate
    Mat_<double> output = scale * regression_output[index] * rotationMatrix;

    output = ProjectToImageCoordinate(output, bounding_box, false);

    return output;
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