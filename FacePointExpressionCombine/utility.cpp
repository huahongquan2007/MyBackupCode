#include "utility.h"
#include <opencv2/opencv.hpp>
#include "iomanip"

#include <vector>
#include <fstream>
using namespace std;

void readBoundingBoxes(int const num_of_training, vector<Rect_<int>> &bounding_boxes, string &bounding_box_train_path) {
    ifstream finBox( bounding_box_train_path , ios_base::in );
    Rect_<int> rect;
    for(int i = 0;i < num_of_training; i++){
        finBox >> rect.x >> rect.y >> rect.width >> rect.height;
        cout << rect << endl;
        bounding_boxes.push_back(rect);
    }
    finBox.close();
}

void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<float>> &keypoints, string &train_path) {
    ifstream finKey( train_path , ios_base::in );
    for(int i = 0;i < num_of_training; i++){
        Mat_<float> temp(num_of_landmark,2);
        float val;
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<float>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<float>(j, 1) = val;
        }
        keypoints.push_back(temp);
    }
    finKey.close();
}

void visualizeImage(Mat img, Mat_<float> keypoints, int delay, bool debug, string win_name, bool isColor){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow(win_name, WINDOW_NORMAL);

    Mat curImg;

    if(isColor){
        curImg = img.clone();
    }
    else{
        cvtColor( img, curImg, CV_GRAY2BGR );
    }


    Mat_<float> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<float>(j, 0);
        int y = (int) curKey.at<float>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
    }

    imshow(win_name, curImg);

    waitKey(delay);
}

void visualizeImageCompare(Mat img, Mat_<float> keypoints, Mat_<float> keypoints2, int delay, bool debug){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow("ImageCompare", WINDOW_NORMAL);

    Mat curImg;

    cvtColor( img, curImg, CV_GRAY2BGR );

    Mat_<float> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<float>(j, 0);
        int y = (int) curKey.at<float>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
    }

    curKey = keypoints2;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<float>(j, 0);
        int y = (int) curKey.at<float>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(0, 255, 0), -1);
    }

    imshow("ImageCompare", curImg);

    waitKey(delay);
}

Mat_<float> GetMeanShape(vector<Mat_<float>> keypoints, vector<Rect_<int>> boxes) {
    int numOfImages = keypoints.size();
    Mat_<float> result = Mat::zeros(keypoints[0].size(), CV_32F);

    for(int i = 0 ; i < numOfImages; i++){
        result += ProjectToBoxCoordinate( keypoints[i] , boxes[i] );
    }

    result = result / numOfImages;

    cout << "Keypoints size: " << keypoints[0].size() << endl;
    cout << "Meanshape: " << result << endl;
    return result;
}

Point GetMeanPoint(Mat_<float> keypoints){
    Point meanIndex;
    for(int j = 0 ; j < keypoints.rows ; j++){
        meanIndex.x += keypoints.at<float>(j, 0);
        meanIndex.y += keypoints.at<float>(j, 1);
    }
    meanIndex.x = meanIndex.x / keypoints.rows;
    meanIndex.y = meanIndex.y / keypoints.rows;

    return meanIndex;
}

Mat_<float> ProjectToBoxCoordinate( Mat_<float> points, Rect_<int> box ){
    Mat_<float> result = Mat::zeros(points.size(), CV_32F);

    float half_w = box.width / 2.0;    float half_h = box.height / 2.0;
    float center_x = box.x + half_w;   float center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){
        result.at<float>(i, 0) = ( points.at<float>(i, 0) - center_x )/ half_w;
        result.at<float>(i, 1) = ( points.at<float>(i, 1) - center_y )/ half_h;
    }

    return result;
}

Mat_<float> ProjectToImageCoordinate( Mat_<float> points, Rect_<int> box , bool translationToBox){
    Mat_<float> result = Mat::zeros(points.size(), CV_32F);

    float half_w = box.width / 2.0;    float half_h = box.height / 2.0;
    float center_x = box.x + half_w;   float center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){

        result.at<float>(i, 0) = (translationToBox) ? ( points.at<float>(i, 0) * half_w ) + center_x : ( points.at<float>(i, 0) * half_w );

        result.at<float>(i, 1) = (translationToBox) ? ( points.at<float>(i, 1) * half_h ) + center_y : ( points.at<float>(i, 1) * half_h );
    }

    return result;
}

float calculate_covariance(const Mat_<float> x, const Mat_<float> y) {
//    cout << "------------------------" << endl;
//    cout << "X: " << x << endl;
//    cout << "Y: " << y << endl;
//    cout << "MeanX: " << mean(x)[0] << endl;
//    cout << "MeanY: " << mean(y)[0] << endl;
    Mat_<float> x1 = x - mean(x)[0];
    Mat_<float> y1 = y - mean(y)[0];
    return mean( x1.mul(y1) )[0];
}

void similarity_transform(const Mat_<float>& shape1, const Mat_<float>& shape2, Mat_<float>& rotation, float& scale){
    rotation = Mat::zeros(2,2,CV_64FC1);
    scale = 0;

    // center the data
    float center_x_1 = 0;
    float center_y_1 = 0;
    float center_x_2 = 0;
    float center_y_2 = 0;
    for(int i = 0;i < shape1.rows;i++){
        center_x_1 += shape1.at<float>(i,0);
        center_y_1 += shape1.at<float>(i,1);
        center_x_2 += shape2.at<float>(i,0);
        center_y_2 += shape2.at<float>(i,1);
    }

    center_x_1 /= shape1.rows;
    center_y_1 /= shape1.rows;
    center_x_2 /= shape2.rows;
    center_y_2 /= shape2.rows;

    Mat_<double> temp1 = Mat::zeros(shape1.size(), shape1.type());
    Mat_<double> temp2 = Mat::zeros(shape2.size(), shape1.type());
    for(int i = 0;i < shape1.rows;i++){
        temp1.at<double>(i, 0) = (double) shape1.at<float>(i, 0);
        temp1.at<double>(i, 1) = (double) shape1.at<float>(i, 1);
        temp2.at<double>(i, 0) = (double) shape2.at<float>(i, 0);
        temp2.at<double>(i, 1) = (double) shape2.at<float>(i, 1);
    }


    for(int i = 0;i < shape1.rows;i++){
        temp1.at<double>(i,0) -= center_x_1;
        temp1.at<double>(i,1) -= center_y_1;
        temp2.at<double>(i,0) -= center_x_2;
        temp2.at<double>(i,1) -= center_y_2;
    }

    Mat_<double> covariance1, covariance2;
    Mat_<double> mean1,mean2;
    // calculate covariance matrix
    calcCovarMatrix(temp1,covariance1,mean1,CV_COVAR_COLS);
    calcCovarMatrix(temp2,covariance2,mean2,CV_COVAR_COLS);


    float s1 = sqrt(norm(covariance1));
    float s2 = sqrt(norm(covariance2));
    scale = s1 / s2;
    temp1 = 1.0 / s1 * temp1;
    temp2 = 1.0 / s2 * temp2;

    float num = 0;
    float den = 0;
    for(int i = 0;i < shape1.rows;i++){
        num = num + temp1.at<double>(i,1) * temp2.at<double>(i,0) - temp1.at<double>(i,0) * temp2.at<double>(i,1);
        den = den + temp1.at<double>(i,0) * temp2.at<double>(i,0) + temp1.at<double>(i,1) * temp2.at<double>(i,1);
    }

    float norm = sqrt(num*num + den*den);
    float sin_theta = num / norm;
    float cos_theta = den / norm;
    rotation.at<float>(0,0) = cos_theta;
    rotation.at<float>(0,1) = -sin_theta;
    rotation.at<float>(1,0) = sin_theta;
    rotation.at<float>(1,1) = cos_theta;

    if( std::isnan(rotation.at<float>(0, 0)) ){
        waitKey(0);
    }


}

void readFERDataset(){
    // ===========================================
    //              READ FER DATASET
    // ===========================================
    char line[4096];
    ifstream input("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/FER/fer2013.csv", ifstream::in);

//    input.getline(line, 4096);
//    cout << line << endl;
    string str, parsed;
    getline(input, str);
    while( getline(input, str) ){

        stringstream input_stringstream(str);
        cout << "New line: " << endl;

        int count = 0;
        while( getline( input_stringstream, parsed, ',' )){
            if(count == 0){
                cout << "CLASS: " << parsed << endl;
            } else if(count == 1){
                cout << "PIXEL: " << endl;
                stringstream pixel_stream(parsed);
                unsigned char pixel_data[2304];
                for (int i = 0 ; i < 2304 ; i++){
                    int pixel;
                    pixel_stream >> pixel;
                    pixel_data[i] = (unsigned char) pixel;
                }
                Mat image (48, 48, CV_8UC1, &pixel_data);
                cout << image << endl;
                namedWindow("Test", CV_WINDOW_NORMAL);
                imshow("Test", image);
                waitKey(0);

            }
            count ++;
        }
        cout << endl;

    }
    cout << endl;
}

float heuristicMult(float a, float b){
    // Test float vs int
    int64 max = 100000;
//    int64 t0 = getTickCount();
//    float c = a * b;
//    cout << "Result float: " << c << endl;
//    int64 t1 = getTickCount();
//    cout << "Time compute float: " << (t1 - t0) / getTickFrequency() << endl;

//    int64 t2 = getTickCount();

    int64 a1 = static_cast<int64>(a * max);
    int64 b1 = static_cast<int64>(b * max);
    int64 d = a1 * b1;
    float c1 = 1.0 * d / max / max;
//    cout << "Result int: " << c1 << endl;
//    int64 t3 = getTickCount();
//    cout << "Time compute int:: " << (t3 - t2) / getTickFrequency()  << endl;
    return c1;
}
cv::Mat_<double> normalizeKeypoint(cv::Mat_<double> keypoint){

    Mat_<double> normalize = keypoint.clone();

    double mean_x = mean(keypoint.row(0))[0];
    double mean_y = mean(keypoint.row(1))[0];

    for(int i = 0 ; i < keypoint.rows; i++){
        normalize.at<double>(i, 0) -= mean_x;
        normalize.at<double>(i, 1) -= mean_y;
    }

    double min, max;
    minMaxLoc(normalize.row(0), &min, &max);

    max = (abs(min) > max) ? abs(min) : max;

    for(int i = 0 ; i < keypoint.rows; i++){

        bool chosen = true;
//        if( i < 17 ) {
//            chosen = false;
//        }
//        if( i > 27 && i < 37)
//            chosen = false;

        if(chosen){
            normalize.at<double>(i, 0) /= max;
            normalize.at<double>(i, 1) /= max;
        }else{
            normalize.at<double>(i, 0) = 0;
            normalize.at<double>(i, 1) = 0;
        }


    }

    return normalize;
}