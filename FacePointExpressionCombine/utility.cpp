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
//        cout << rect << endl;
        bounding_boxes.push_back(rect);
    }
    finBox.close();
}

void readKeypoints(int const num_of_training, int const num_of_landmark, vector<Mat_<double>> &keypoints, string &train_path) {
    ifstream finKey( train_path , ios_base::in );
    for(int i = 0;i < num_of_training; i++){
        Mat_<double> temp(num_of_landmark,2);
        double val;
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<double>(j, 0) = val;
        }
        for(int j = 0; j < num_of_landmark; j++){
            finKey >> val;
            temp.at<double>(j, 1) = val;
        }
        keypoints.push_back(temp);
    }
    finKey.close();
}

void visualizeImage(Mat img, Mat_<double> keypoints, int delay, bool debug, string win_name, bool isColor){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow(win_name, WINDOW_NORMAL);

    Mat curImg;

    if(isColor){
        curImg = img.clone();
    }
    else{
        cvtColor( img, curImg, CV_GRAY2BGR );
    }


    Mat_<double> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<double>(j, 0);
        int y = (int) curKey.at<double>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 3, Scalar(255, 0, 0), -1);
    }

    imshow(win_name, curImg);

    waitKey(delay);
}

void visualizeImageCompare(Mat img, Mat_<double> keypoints, Mat_<double> keypoints2, int delay, bool debug){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow("ImageCompare", WINDOW_NORMAL);

    Mat curImg;

    cvtColor( img, curImg, CV_GRAY2BGR );

    Mat_<double> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<double>(j, 0);
        int y = (int) curKey.at<double>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 3, Scalar(255, 0, 0), -1);
    }

    curKey = keypoints2;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<double>(j, 0);
        int y = (int) curKey.at<double>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(0, 255, 0), -1);
    }

    imshow("ImageCompare", curImg);

    waitKey(delay);
}

Mat_<double> GetMeanShape(vector<Mat_<double>> keypoints, vector<Rect_<int>> boxes) {
    int numOfImages = keypoints.size();
    Mat_<double> result = Mat::zeros(keypoints[0].size(), CV_32F);

    for(int i = 0 ; i < numOfImages; i++){
        result += ProjectToBoxCoordinate( keypoints[i] , boxes[i] );
    }

    result = result / numOfImages;

    cout << "Keypoints size: " << keypoints[0].size() << endl;
    cout << "Meanshape: " << result << endl;
    return result;
}

Point GetMeanPoint(Mat_<double> keypoints){
    Point meanIndex;
    for(int j = 0 ; j < keypoints.rows ; j++){
        meanIndex.x += keypoints.at<double>(j, 0);
        meanIndex.y += keypoints.at<double>(j, 1);
    }
    meanIndex.x = meanIndex.x / keypoints.rows;
    meanIndex.y = meanIndex.y / keypoints.rows;

    return meanIndex;
}

Mat_<double> ProjectToBoxCoordinate( Mat_<double> points, Rect_<int> box ){
    Mat_<double> result = Mat::zeros(points.size(), CV_32F);

    double half_w = box.width / 2.0;    double half_h = box.height / 2.0;
    double center_x = box.x + half_w;   double center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){
        result.at<double>(i, 0) = ( points.at<double>(i, 0) - center_x )/ half_w;
        result.at<double>(i, 1) = ( points.at<double>(i, 1) - center_y )/ half_h;
    }

    return result;
}

Mat_<double> ProjectToImageCoordinate( Mat_<double> points, Rect_<int> box , bool translationToBox){
    Mat_<double> result = Mat::zeros(points.size(), CV_32F);

    double half_w = box.width / 2.0;    double half_h = box.height / 2.0;
    double center_x = box.x + half_w;   double center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){

        result.at<double>(i, 0) = (translationToBox) ? ( points.at<double>(i, 0) * half_w ) + center_x : ( points.at<double>(i, 0) * half_w );

        result.at<double>(i, 1) = (translationToBox) ? ( points.at<double>(i, 1) * half_h ) + center_y : ( points.at<double>(i, 1) * half_h );
    }

    return result;
}

double calculate_covariance(const Mat_<double> x, const Mat_<double> y) {
//    cout << "------------------------" << endl;
//    cout << "X: " << x << endl;
//    cout << "Y: " << y << endl;
//    cout << "MeanX: " << mean(x)[0] << endl;
//    cout << "MeanY: " << mean(y)[0] << endl;

    Mat_<double> x1 = x - mean(x)[0];
    Mat_<double> y1 = y - mean(y)[0];

    return mean( x1.mul(y1) )[0];
}

void similarity_transform(const Mat_<double>& shape1, const Mat_<double>& shape2, Mat_<double>& rotation, double& scale){
    rotation = Mat::zeros(2,2,CV_64FC1);
    scale = 0;

    // center the data
    double center_x_1 = 0;
    double center_y_1 = 0;
    double center_x_2 = 0;
    double center_y_2 = 0;
    for(int i = 0;i < shape1.rows;i++){
        center_x_1 += shape1.at<double>(i,0);
        center_y_1 += shape1.at<double>(i,1);
        center_x_2 += shape2.at<double>(i,0);
        center_y_2 += shape2.at<double>(i,1);
    }
    center_x_1 /= shape1.rows;
    center_y_1 /= shape1.rows;
    center_x_2 /= shape2.rows;
    center_y_2 /= shape2.rows;

    Mat_<double> temp1 = shape1.clone();
    Mat_<double> temp2 = shape2.clone();
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

    double s1 = sqrt(norm(covariance1));
    double s2 = sqrt(norm(covariance2));
    scale = s1 / s2;
    temp1 = 1.0 / s1 * temp1;
    temp2 = 1.0 / s2 * temp2;

    double num = 0;
    double den = 0;
    for(int i = 0;i < shape1.rows;i++){
        num = num + temp1.at<double>(i,1) * temp2.at<double>(i,0) - temp1.at<double>(i,0) * temp2.at<double>(i,1);
        den = den + temp1.at<double>(i,0) * temp2.at<double>(i,0) + temp1.at<double>(i,1) * temp2.at<double>(i,1);
    }

    double norm = sqrt(num*num + den*den);
    double sin_theta = num / norm;
    double cos_theta = den / norm;
    rotation.at<double>(0,0) = cos_theta;
    rotation.at<double>(0,1) = -sin_theta;
    rotation.at<double>(1,0) = sin_theta;
    rotation.at<double>(1,1) = cos_theta;
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