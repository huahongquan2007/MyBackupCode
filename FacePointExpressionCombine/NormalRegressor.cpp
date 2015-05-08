#include "NormalRegressor.h"
#include "utility.h"
#include <fstream>

NormalRegressor::NormalRegressor(int child_level, int feature_per_fern, int num_of_random_pixels) {

    NormalRegressor::num_of_random_pixels = num_of_random_pixels;

    for(int i = 0 ; i < child_level ; i ++ ){
        FernRegressor curRegressor ( feature_per_fern ) ;
        childRegressor.push_back(curRegressor);
    }
}

vector<Mat_<float>> NormalRegressor::Train(vector<Mat_<unsigned char>> images, vector<Mat_<float>> keypoints, Mat_<float> meanShape, vector<Rect_<int>> boundingBoxes, vector<Mat_<float>> inputShape, bool isDebug ){
    if(isDebug) cout << "NormalRegressor: Train" << endl;

    // Variable
    int num_of_images = images.size();

    Mat_<float> shapeIndexLocation (num_of_random_pixels, 2);
    Mat_<int> shapeIndexNearestLandmark (num_of_random_pixels, 1);
    vector<Mat_<float>> deltaShape;

    Mat_<float> rotation;
    float scale = 0;

    // Random P pixels

    if(isDebug) cout << meanShape.t() << endl;
    RNG rng;
    float rand_x , rand_y;
    for(int i = 0 ; i < num_of_random_pixels ; i ++){
        rand_x = rng.uniform(-1.0, 1.0);
        rand_y = rng.uniform(-1.0, 1.0);

        if (rand_x * rand_x + rand_y * rand_y > 1.0){
            i--;
            continue;
        }
        // Find nearest landmark
        float min_dist = 1e10;
        int min_idx = -1;
        for(int j = 0 ; j < meanShape.rows; j++){
            // float cur_dist = sqrt( pow( ( meanShape.at<float>(j, 0) - rand_x ), 2 ) + pow( ( meanShape.at<float>(j, 1) - rand_y ), 2 ) );
            float cur_dist = pow( ( meanShape.at<float>(j, 0) - rand_x ), 2 ) + pow( ( meanShape.at<float>(j, 1) - rand_y ), 2 );
            if( cur_dist < min_dist ){
                min_dist = cur_dist;
                min_idx = j;
            }
        }

        shapeIndexLocation.at<float>(i, 0) = rand_x - meanShape.at<float>(min_idx, 0);
        shapeIndexLocation.at<float>(i, 1) = rand_y - meanShape.at<float>(min_idx, 1);
        shapeIndexNearestLandmark.at<int>(i, 0) = min_idx;

    }

    // Get pixel intensity of each P pixels over training set
    if(isDebug) cout << "Get Pixel Intensity of " << num_of_images << " images" << endl;
    Mat_<int> shapeIndexPixels (num_of_random_pixels, num_of_images);

    Mat_<float> curLocation (num_of_random_pixels, 2);

    for(int i = 0 ; i < num_of_images ; i++){
        // project keypoints to box
        Mat_<float> curKeyPoints = ProjectToBoxCoordinate(inputShape[i], boundingBoxes[i]);

        // align with meanshape
        similarity_transform(curKeyPoints, meanShape, rotation, scale);
        transpose(rotation, rotation);

        int idx_landmark = 0;
        Mat_<float> curShapeIndexLoc( 1, 2);
        // calculate shapeIndexLocation

        for(int j = 0 ; j < num_of_random_pixels; j++){
            idx_landmark = shapeIndexNearestLandmark.at<int>(j, 0);
            curShapeIndexLoc = shapeIndexLocation.row(j).clone();
            curShapeIndexLoc = scale * curShapeIndexLoc * rotation;

            curLocation.at<float>(j , 0 ) = curShapeIndexLoc.at<float>(0 , 0) + curKeyPoints.at<float>(idx_landmark, 0);
            curLocation.at<float>(j , 1 ) = curShapeIndexLoc.at<float>(0 , 1) + curKeyPoints.at<float>(idx_landmark, 1);
        }

        Mat_<float> curLocationImageCoor = ProjectToImageCoordinate(curLocation, boundingBoxes[i]);
        int x , y ;
        // get pixels

        for(int j = 0 ; j < num_of_random_pixels; j++){
            x = max(0, min( (int)curLocationImageCoor.at<float>(j, 0) , images[i].cols-1));
            y = max(0, min(  (int)curLocationImageCoor.at<float>(j, 1) , images[i].rows-1));

            shapeIndexPixels.at<int>(j, i) = (int) images[i].at<unsigned char>(y, x);
        }
    }

    // Calculate covariance between i, j

    Mat_<float> covariance_matrix(num_of_random_pixels, num_of_random_pixels);
    for(int i = 0 ; i < num_of_random_pixels; i++){
        for(int j = i ; j < num_of_random_pixels; j++){
            float cov_value = calculate_covariance( shapeIndexPixels.row(i), shapeIndexPixels.row(j) );
            covariance_matrix.at<float>(i, j) = cov_value;
            covariance_matrix.at<float>(j, i) = cov_value;
        }
    }

    // calculate regression target

    vector< Mat_<float> > regression_target;
    vector< Mat_<float> > regression_output;
    for(int i = 0 ; i < num_of_images; i ++){

        Mat_<float> box = ProjectToBoxCoordinate( inputShape[i], boundingBoxes[i] );
        Mat_<float> keybox = ProjectToBoxCoordinate( keypoints[i], boundingBoxes[i] );

        similarity_transform(meanShape, box, rotation, scale);
        transpose(rotation, rotation);

        Mat_<float> target = scale * (keybox - box) * rotation;

        regression_target.push_back( target );
        regression_output.push_back( Mat::zeros(inputShape[i].size(), CV_32F ) );
    }

    int visualIdx = 0;
    Mat_<float> initialShape = inputShape[visualIdx].clone();

    for(int i = 0 ; i < childRegressor.size(); i++){

        cout << "------------ BEGIN [" << i << "]-------------" << endl;
        if(isDebug) cout << "REGRESSION TARGET: " << endl;
        if(isDebug) cout << regression_target[visualIdx].t() << endl;

        // Train each child-level regressor

        deltaShape = childRegressor[i].Train(regression_target, covariance_matrix, shapeIndexPixels, shapeIndexLocation, shapeIndexNearestLandmark);

        for(int j = 0 ; j < num_of_images ; j++){
            regression_target[j] -= deltaShape[j];
            regression_output[j] += deltaShape[j];
        }
        if(isDebug){
            cout << "------------------------------" << endl;

            Mat_<float> rotation;
            float scale = 0;
            similarity_transform(ProjectToBoxCoordinate(inputShape[visualIdx], boundingBoxes[visualIdx]), meanShape, rotation, scale);
            transpose(rotation, rotation);

            Mat_<float> resultShape = ProjectToImageCoordinate( ProjectToBoxCoordinate(initialShape, boundingBoxes[visualIdx]) + scale * regression_output[visualIdx] * rotation, boundingBoxes[visualIdx]);
            visualizeImageCompare(images[visualIdx], resultShape, initialShape, 10);
        }
    }

    for(int j = 0 ; j < num_of_images ; j++){
        similarity_transform(ProjectToBoxCoordinate(inputShape[j], boundingBoxes[j]), meanShape, rotation, scale);
        transpose(rotation, rotation);
        regression_output[j] = scale * regression_output[j] * rotation;
    }
    return regression_output;
}

Mat_<float> NormalRegressor::Test(const Mat_<unsigned char> &image, const Rect_<int> &bounding_box, const Mat_<float> &curShape, const Mat_<float> &meanShape){
    Mat_<float> regression_output = Mat::zeros(curShape.size(), curShape.type());
    Mat_<float> deltaShape;
    Mat_<float> inputShape = curShape.clone();

    for(int i = 0 ; i < childRegressor.size(); i++){
        deltaShape = childRegressor[i].Test(image, bounding_box, inputShape, meanShape);
        regression_output += deltaShape;
        inputShape = curShape + regression_output;
//        visualizeImage(image, inputShape, 1);
    }

    return regression_output;
}

void NormalRegressor::Save(FileStorage &out){
    out << "num_of_random_pixels" << num_of_random_pixels;

    out << "child_regressor_size" << (int)childRegressor.size();

    cout << num_of_random_pixels << endl;
    cout << childRegressor.size() << endl;

    for(int i = 0 ; i < childRegressor.size(); i++){
        string name = "child_regressors_";
        name += to_string(i);
        out << name << "{";
        childRegressor[i].Save(out);
        out << "}";
    }
}

void NormalRegressor::Load(FileNode in){
    in["num_of_random_pixels"] >> num_of_random_pixels;

    int childSize = 0;
    in["child_regressor_size"] >> childSize;

    cout << num_of_random_pixels << endl;
    cout << childSize << endl;

    childRegressor.resize(childSize);
    for(int i = 0 ; i < childSize; i++) {
        string name = "child_regressors_";
        name += to_string(i);
        childRegressor[i].Load(in[name]);
    }
}