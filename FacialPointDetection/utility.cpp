#include "utility.h"
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
        result(i, 0) = ( points(i, 0) - center_x )/ half_w;
        result(i, 1) = ( points(i, 1) - center_y )/ half_h;
    }

    return result;
}

Mat_<double> ProjectToImageCoordinate( Mat_<double> points, Rect_<int> box ){
    Mat_<double> result = Mat::zeros(points.size(), CV_32F);

    double half_w = box.width / 2.0;    double half_h = box.height / 2.0;
    double center_x = box.x + half_w;   double center_y = box.y + half_h;

    for(int i = 0 ; i < points.size().height ; i ++){
        result(i, 0) = ( points(i, 0) * half_w ) + center_x;
        result(i, 1) = ( points(i, 1) * half_h ) + center_y;
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
