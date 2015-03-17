#include "utility.h"
#include "iomanip"

void visualizeImage(Mat img, Mat_<double> keypoints, int num_of_landmark, bool debug){
    // --------------- DRAW A FACE + KEYPOINT --------
    namedWindow("Images", WINDOW_NORMAL);

    Mat curImg;

    cvtColor( img, curImg, CV_GRAY2BGR );

    Mat_<double> curKey = keypoints;

    if(debug) cout << endl;

    for(int j = 0 ; j < num_of_landmark ; j++){
        double x = curKey.at<double>(j, 0);
        double y = curKey.at<double>(j, 1);
        if(debug) cout << "Point["<< j << "]( " << setw(7) << x << " , " << setw(7) << y << " )" << endl;
        circle(curImg, Point(x, y), 1, Scalar(255, 0, 0), -1);
    }

    imshow("Images", curImg);

    waitKey(0);
}