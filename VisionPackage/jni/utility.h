#include <iostream>
#include <sstream>
#include <cstdio>
#include <ctime>
#include <string>
#include <vector>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/video/tracking.hpp>
#include <opencv2/video/background_segm.hpp>

using namespace cv;
using namespace std;

#ifndef _my_native_utility_
#define _my_native_utility_

struct FaceInfo{
    string name;
    float x, y, w, h;
    time_t time;
};
struct PointInfo{
    Point point;
    time_t time;
};
static const int MIN_WIDTH = 10;
static const int MIN_HEIGHT = 10;

class track_motion_class
{
private:
    Mat prev;
    vector<Point2f> points[2];
    std::vector < std::vector < cv::Point > >contours;
    Mat back, fore;
    BackgroundSubtractorMOG2 bg;
    Size frameSize;
    int frameType;
public:
    track_motion_class();
    void initialize();
    void run2(Mat img, PointInfo& motion, Mat &foreMat);
//    void run(Mat img, int &motionX, int &motionY);
};

#endif
