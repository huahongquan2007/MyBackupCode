#include "utility.h"
#include <fstream>
#include "iomanip"
using namespace std;
using namespace cv;

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
        circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
    }

    imshow(win_name, curImg);

    waitKey(delay);
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