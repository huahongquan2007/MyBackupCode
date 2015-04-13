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

        if (rand_x * rand_x + rand_y * rand_y > 1.0){
            i--;
            continue;
        }
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
        shapeIndexNearestLandmark.at<int>(i, 0) = min_idx;
    }

    // Get pixel intensity of each P pixels over training set
    if(isDebug) cout << "Get Pixel Intensity" << endl;
    Mat_<double> shapeIndexPixels (num_of_random_pixels, num_of_images);


    for(int i = 0 ; i < num_of_images ; i++){
        // project keypoints to box
        Mat_<double> rotation;
        double scale = 0;
//        cout << i << "/" << num_of_images << endl;

        Mat_<double> curKeyPoints = ProjectToBoxCoordinate(inputShape[i], boundingBoxes[i]);

        // align with meanshape
        similarity_transform(meanShape, curKeyPoints, rotation, scale);

        transpose(rotation, rotation);
        curKeyPoints = scale * curKeyPoints * rotation;

//        cout << "HQHHQ" << endl;

        Mat_<double> curLocation (num_of_random_pixels, 2);

//        cout << "HQHHQ" << endl;
        // calculate shapeIndexLocation
        for(int j = 0 ; j < num_of_random_pixels; j++){
            int idx_landmark = shapeIndexNearestLandmark.at<int>(j, 0);
            curLocation.at<double>(j , 0 ) = shapeIndexLocation.at<double>(j , 0) + curKeyPoints.at<double>(idx_landmark, 0);
            curLocation.at<double>(j , 1 ) = shapeIndexLocation.at<double>(j , 1) + curKeyPoints.at<double>(idx_landmark, 1);
        }

        // project location to image coordinates
        similarity_transform(curKeyPoints, meanShape, rotation, scale);
        transpose(rotation, rotation);
        curLocation = scale * curLocation * rotation;

        Mat_<double> curLocationImageCoor = ProjectToImageCoordinate(curLocation, boundingBoxes[i]);

        if ( i == 2020){
        // --------------- DRAW A FACE + KEYPOINT --------
//        namedWindow("shapeIndex", WINDOW_NORMAL);
//
//        Mat curImg;
//
//        cvtColor( images[i], curImg, CV_GRAY2BGR );
//
//        Mat_<double> curKey = curLocationImageCoor;
//
//        for(int j = 0 ; j < curKey.rows ; j++){
//            int x = (int) curKey.at<double>(j, 0);
//            int y = (int) curKey.at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
//        }
//
//        for(int j = 0 ; j < inputShape[i].rows ; j++){
//            int x = (int) inputShape[i].at<double>(j, 0);
//            int y = (int) inputShape[i].at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(0, 255, 0), -1);
//        }
//
//        imshow("shapeIndex", curImg);
//
//        waitKey(0);
        }


        // get pixels
        for(int j = 0 ; j < num_of_random_pixels; j++){
//            cout << curLocationImageCoor.at<double>(j,0) << " " <<  curLocationImageCoor.at<double>(j, 1) << " " << images[i].size() <<  " " << shapeIndexPixels.size() <<endl;

            int x = (int)curLocationImageCoor.at<double>(j,0);
//            cout << "X: " << x << endl;
            int y = (int)curLocationImageCoor.at<double>(j, 1);
//            cout << "Y: " << y << endl;
            try{
//                cout << "images[i] " << images[i].at<unsigned char>(x, y) << endl;
                unsigned char uc = images[i].at<unsigned char>(x, y);
//                cout << "uc " << uc << endl;
                double tt = (double) uc;
//                cout << "tt " << tt << endl;
                shapeIndexPixels.at<double>(j, i) = tt;
            } catch (exception& e)
            {
                cout << "Standard exception: " << e.what() << endl;
            }


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


    // calculate regression target

    vector< Mat_<double> > regression_target;
    vector< Mat_<double> > regression_output;
    for(int i = 0 ; i < num_of_images; i ++){

        Mat_<double> rotation;
        double scale = 0;


        Mat_<double> box = ProjectToBoxCoordinate( inputShape[i], boundingBoxes[i] );
        Mat_<double> keybox = ProjectToBoxCoordinate( keypoints[i], boundingBoxes[i] );

        similarity_transform(meanShape, box, rotation, scale);
        transpose(rotation, rotation);

        Mat_<double> target = scale * (keybox - box) * rotation;

        regression_target.push_back( target );
        regression_output.push_back( Mat::zeros(inputShape[i].size(), CV_32F ) );

//        Mat_<double> test = ProjectToImageCoordinate((rotation * box.t() * scale).t(), boundingBoxes[i]);
//
//        cout << "test 1" << endl;
//        Mat curImg;
//        Mat img = images[i].clone();
//        cvtColor( img, curImg, CV_GRAY2BGR );
//        cout << "test 2" << endl;
//        for(int j = 0 ; j < inputShape[i].rows ; j++){
//            int x = (int) inputShape[i].at<double>(j, 0);
//            int y = (int) inputShape[i].at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(0, 0, 255), -1);
//        }
//        for(int j = 0 ; j < test.rows ; j++){
//            int x = (int) test.at<double>(j, 0);
//            int y = (int) test.at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
//        }
//        namedWindow("target", WINDOW_NORMAL);
//        imshow("target", curImg);
//        waitKey(0);
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
            regression_target[j] -= deltaShape[j];
//            regression_output[j] -= deltaShape[j];
//            Mat_<double> output = ProjectToBoxCoordinate( inputShape[j] , boundingBoxes[j] ) + (rotationMatrixArray[j].t() * deltaShape[j].t() / scaleArray[j]).t();

            regression_output[j] += deltaShape[j];
//            regression_output[j] += ProjectToImageCoordinate(output, boundingBoxes[j]) - inputShape[j];
// /            regression_output[j] = ProjectToImageCoordinate(
//                    ProjectToBoxCoordinate( inputShape[j] , boundingBoxes[j] ) + (rotationMatrixArray[j].t() * deltaShape[j].t() / scaleArray[j]).t(),
//                    boundingBoxes[j])  - inputShape[j];
        }
        cout << "------------------------------" << endl;
        if(isDebug){

//            cout << "Initial SHAPE: " << endl;
//            cout << initialShape.t() << endl;
//            cout << "DELTA SHAPE: " << endl;
//            cout << deltaShape[visualIdx].t() << endl;
//            cout << "BOUNDINGBOXES: " << boundingBoxes[visualIdx] << endl;
//            cout << "INITIAL + DELTA SHAPE: " << endl;
//            cout << (ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + deltaShape[visualIdx]).t() << endl;
//            cout << "INITIAL + DELTA SHAPE (PROJECT): " << endl;
//            cout << (ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_target[visualIdx], boundingBoxes[visualIdx])).t() << endl;
//            cout << "INITIAL + DELTA SHAPE (ORIGINAL): " << endl;
//            cout << (ProjectToImageCoordinate(ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ) + regression_target[visualIdx], boundingBoxes[visualIdx]) - initialShape).t() << endl;
            Mat_<double> rotation;
            double scale = 0;
            similarity_transform(ProjectToBoxCoordinate(inputShape[visualIdx], boundingBoxes[visualIdx]), meanShape, rotation, scale);
            transpose(rotation, rotation);

            Mat_<double> output = ProjectToBoxCoordinate(inputShape[visualIdx], boundingBoxes[visualIdx]) + scale * regression_output[visualIdx] * rotation;

            cout << "OUTPUT" << endl;
            cout << output.t() << endl;
            resultShape = ProjectToImageCoordinate(output, boundingBoxes[visualIdx]);

            cout << "Initial SHAPE: " << endl;
            cout << initialShape.t() << endl;
            cout << "Initial SHAPE (PROJECT): " << endl;
            cout << ProjectToBoxCoordinate( initialShape , boundingBoxes[visualIdx] ).t() << endl;
            cout << "RESULT SHAPE: " << endl;
            cout << resultShape.t() << endl;
            cout << "GROUND TRUTH SHAPE: " << endl;
            cout << keypoints[visualIdx].t() << endl;
            cout << "REGRESSION TARGET: " << endl;
            cout << regression_target[visualIdx].t() << endl;
            cout << "DELTA SHAPE: " << endl;
            cout << deltaShape[visualIdx].t() << endl;
            cout << "REGRESSION OUTPUT: " << endl;
            cout << regression_output[visualIdx].t() << endl;

            visualizeImageCompare(images[visualIdx], resultShape, initialShape, 10);
        }
    }

//    if(isDebug) cout << "REGRESSION OUTPUT - BEFORE[0] : " << endl;
//    if(isDebug) cout << regression_output[visualIdx].t() << endl;

    for(int j = 0 ; j < num_of_images ; j++){
        Mat_<double> rotation;
        double scale = 0;
        //similarity_transform(ProjectToBoxCoordinate(inputShape[j], boundingBoxes[j]), meanShape, rotation, scale);
        similarity_transform(ProjectToBoxCoordinate(inputShape[j], boundingBoxes[j]), meanShape, rotation, scale);

        transpose(rotation, rotation);
        Mat_<double> output = ProjectToBoxCoordinate(inputShape[j], boundingBoxes[j]) + scale *  regression_output[j] * rotation;
        regression_output[j] = ProjectToImageCoordinate(output, boundingBoxes[j]) - inputShape[j];

//        Mat_<double> output = (rotation *  regression_output[j].t() * scale).t();
//        regression_output[j] = ProjectToImageCoordinate(output, boundingBoxes[j]);

//        Mat_<double> test = ProjectToImageCoordinate((rotation * box.t() * scale).t(), boundingBoxes[i]);
//
//        cout << "test 1" << endl;
//        Mat curImg;
//        Mat img = images[i].clone();
//        cvtColor( img, curImg, CV_GRAY2BGR );
//        cout << "test 2" << endl;
//        for(int j = 0 ; j < inputShape[i].rows ; j++){
//            int x = (int) inputShape[i].at<double>(j, 0);
//            int y = (int) inputShape[i].at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(0, 0, 255), -1);
//        }
//        for(int j = 0 ; j < test.rows ; j++){
//            int x = (int) test.at<double>(j, 0);
//            int y = (int) test.at<double>(j, 1);
//            circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
//        }
//        namedWindow("target", WINDOW_NORMAL);
//        imshow("target", curImg);
//        waitKey(0);

    }
    return regression_output;
}

Mat_<double> NormalRegressor::Test(Mat_<unsigned char> image, Rect_<int> bounding_box, Mat_<double> curShape, Mat_<double> meanShape){
    Mat_<double> regression_output = Mat::zeros(curShape.size(), curShape.type());
    Mat_<double> deltaShape;
    Mat_<double> inputShape = curShape.clone();

    for(int i = 0 ; i < childRegressor.size(); i++){
        deltaShape = childRegressor[i].Test(image, bounding_box, inputShape, meanShape);
//
//        regression_output += ProjectToImageCoordinate(
//                ProjectToBoxCoordinate( inputShape , bounding_box ) + deltaShape, bounding_box) - inputShape;

        regression_output += deltaShape;
        inputShape = curShape + regression_output;

        visualizeImage(image, inputShape, 1);
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