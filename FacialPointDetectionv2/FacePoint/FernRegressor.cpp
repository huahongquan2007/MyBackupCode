#include <strings.h>
#include "FernRegressor.h"
#include "utility.h"
#include <fstream>

FernRegressor::FernRegressor(int feature_per_fern) {
    FernRegressor::feature_per_fern = feature_per_fern;
}

vector<Mat_<double>> FernRegressor::Train(vector<Mat_<double>> regression_target, Mat_<double> covariance_matrix, Mat_<int> pixels, Mat_<double> pixelLocation, Mat_<int> nearestLandmarkOfPixel, bool isDebug) {

    Mat_<int> max_corr_index ( feature_per_fern, 2 );



    cout << endl;


    regression_output.resize(bins_index.size()); // 2^F output

    if(isDebug) cout << endl;

    return deltaShape;
}


Mat_<double> FernRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape, Mat_<double> meanShape){
    // project keypoints to box
    Mat_<double> curKeyPoints = ProjectToBoxCoordinate(curShape, bounding_box);

    // align with meanshape
    Mat_<double> rotationMatrix;
    double scale = 0.0;

    similarity_transform(ProjectToBoxCoordinate(curShape, bounding_box), meanShape, rotationMatrix, scale);
    transpose(rotationMatrix, rotationMatrix);

    // find bin
    int index = 0, x, y;
    Mat_<double> curLocation ( 1, 2 );
    Mat_<double> curFernLocation ( 1, 2);
    Mat_<double> curLocationImageCoor;
    for (int i = 0; i < feature_per_fern; i++) {
        // Get Pixel 1
        int idx_landmark_1 = fernPairNearestLandmark[i].at<int>(0, 0);

        curFernLocation = fernPairLocation[i].row(0).clone();
        curFernLocation = scale * curFernLocation * rotationMatrix;
        curLocation.at<double>(0, 0) = curFernLocation.at<double>(0, 0) + curKeyPoints.at<double>( idx_landmark_1, 0);
        curLocation.at<double>(0, 1) = curFernLocation.at<double>(0, 1) + curKeyPoints.at<double>( idx_landmark_1, 1);

        curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        x = max(0, min( (int)curLocationImageCoor.at<double>(0, 0) , image.cols-1));
        y = max(0, min(  (int)curLocationImageCoor.at<double>(0, 1) , image.rows-1));
        int pixel_1 = (int) image.at<unsigned char>(y, x);

        // Get Pixel 2
        int idx_landmark_2 = fernPairNearestLandmark[i].at<int>(0, 1);

        curFernLocation = fernPairLocation[i].row(1).clone();
        curFernLocation = scale * curFernLocation * rotationMatrix;
        curLocation.at<double>(0, 0) = curFernLocation.at<double>(0, 0) + curKeyPoints.at<double>( idx_landmark_2, 0);
        curLocation.at<double>(0, 1) = curFernLocation.at<double>(0, 1) + curKeyPoints.at<double>( idx_landmark_2, 1);

        curLocationImageCoor = ProjectToImageCoordinate(curLocation, bounding_box);
        x = max(0, min( (int)curLocationImageCoor.at<double>(0, 0) , image.cols-1));
        y = max(0, min( (int)curLocationImageCoor.at<double>(0, 1) , image.rows-1));
        int pixel_2 = (int) image.at<unsigned char>(y, x);

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