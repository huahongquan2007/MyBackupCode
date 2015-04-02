#include "utility.h"
#include <opencv2/opencv.hpp>
#include "iomanip"

void visualizeImage(Mat img, Mat_<double> keypoints, int delay, bool debug){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow("Images", WINDOW_NORMAL);

    Mat curImg;

    cvtColor( img, curImg, CV_GRAY2BGR );

    Mat_<double> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < curKey.rows ; j++){
        int x = (int) curKey.at<double>(j, 0);
        int y = (int) curKey.at<double>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
    }

    imshow("Images", curImg);

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

Mat_<double> ProjectToImageCoordinate( Mat_<double> points, Rect_<int> box ){
    Mat_<double> result = Mat::zeros(points.size(), CV_32F);

    double half_w = box.width / 2.0;    double half_h = box.height / 2.0;
    double center_x = box.x + half_w;   double center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){
        result.at<double>(i, 0) = ( points.at<double>(i, 0) * half_w ) + center_x;
        result.at<double>(i, 1) = ( points.at<double>(i, 1) * half_h ) + center_y;
    }

    return result;
}

double calculate_covariance(Mat_<double> x, Mat_<double> y) {
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