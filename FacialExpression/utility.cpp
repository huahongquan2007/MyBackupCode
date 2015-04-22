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
