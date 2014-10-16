#include "utility.h"

#include <android/log.h>
// Utility for logging:
#define LOG_TAG    "CAMERA_RENDERER"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

template < typename T > std::string to_string( const T& n )
{
    std::ostringstream stm ;
    stm << n ;
    return stm.str() ;
}


track_motion_class::track_motion_class(){
	bg = BackgroundSubtractorMOG2(500,16.0, false);
	frameSize = Size(0,0);
	frameType = -1;
}
void track_motion_class::initialize()
{
	if(frameType != -1)
	{
		bg.initialize(frameSize, frameType);
//            cout << "========================== INITIALIZE =====================" << endl;
	}
}
bool myPathWrite = true;
void track_motion_class::run2(Mat img, PointInfo& motion, Mat &foreMat)
{
	Mat frame;
	img.copyTo(frame);
	bg.operator()(frame, fore);
	if(frameType == -1)
	{
		frameSize = img.size();
		frameType = img.type();
	}

//	bg.getBackgroundImage(back);
	cv::erode(fore,fore,Mat::ones(3,3,CV_32F));
	cv::dilate(fore,fore,Mat::ones(10,10,CV_32F));
	cvtColor(fore, foreMat, CV_GRAY2BGRA);
//	foreMat = fore.clone();

	foreMat = fore.clone();
	cv::findContours(fore,contours,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
	LOG("FIND COUTOURS %d", contours.size());
	int largest_area=0;int largest_contour_index=0;  Rect bounding_rect;

	// iterate through each contour.
	for( int i = 0; i< contours.size(); i++ )
	{
		//  Find the area of contour
		double a=contourArea( contours[i],false);
		if(a>largest_area){
			largest_area=a;
//          cout<<i<<" area  "<<a<<endl;
			// Store the index of largest contour
			largest_contour_index=i;
			// Find the bounding rectangle for biggest contour
			bounding_rect=boundingRect(contours[i]);
		}
	}
	cv::drawContours(foreMat,contours,largest_contour_index, cv::Scalar(0,0,255,1),2);
	rectangle(foreMat, bounding_rect,Scalar(255,255,255, 1),2,8,0);

//	cv::imshow("Frame",frame);
//        cv::imshow("Background",back);
//        cv::imshow("Foreground",fore);
//
	if(contours.size() > 0 && bounding_rect.width > MIN_WIDTH && bounding_rect.height > MIN_HEIGHT)
	{
		motion.point.x = bounding_rect.x + bounding_rect.width/2;
		motion.point.y = bounding_rect.y + bounding_rect.height/2;
	}
	else
	{
		motion.point.x = motion.point.y = 0;
	}

	motion.time    = time(0);
}

//void track_motion_class::run(Mat img, int &motionX, int &motionY)
//{
//	cout << "Track Motion" << endl;
//	Mat outImg, gray;
//	Size subPixWinSize(10,10), winSize(31,31);
//	const int MAX_COUNT = 500;
//
//	TermCriteria termcrit(TermCriteria::COUNT|TermCriteria::EPS,20,0.03);
//
//	cvtColor(img, gray, CV_RGB2GRAY);
//	img.copyTo(outImg);
//
//	if(prev.empty())
//	{
//		//Init
//		goodFeaturesToTrack(gray, points[1], MAX_COUNT, 0.01, 10, Mat(), 3, 0, 0.04);
//		cornerSubPix(gray, points[1], subPixWinSize, Size(-1,-1), termcrit);
//	}
//	else if( !points[0].empty() )
//	{
//		vector<uchar> status;
//		vector<float> err;
//		if(prev.empty())
//			gray.copyTo(prev);
//		calcOpticalFlowPyrLK(prev, gray, points[0], points[1], status, err, winSize,
//							 3, termcrit, 0, 0.001);
//		size_t i, k;
//
//		int curSize = min(points[0].size(), points[1].size());
//
//		int countPoint = 0;
//		CvPoint mean;
//		mean.x = mean.y = 0;
//		for( i = 0; i < curSize; i++ )
//		{
//			int line_thickness;				line_thickness = 1;
//
//			CvScalar line_color;			line_color = CV_RGB(255,0,0);
//
//			CvPoint p,q;
//			p.x = (int) points[0][i].x;
//			p.y = (int) points[0][i].y;
//			q.x = (int) points[1][i].x;
//			q.y = (int) points[1][i].y;
//
//
//			circle( outImg, q , 3, Scalar(0,255,0), -1, 8);
//
//
//
//			double angle;		angle = atan2( (double) p.y - q.y, (double) p.x - q.x );
//			double hypotenuse;	hypotenuse = sqrt( (p.y - q.y)*(p.y - q.y) + (p.x - q.x)*(p.x - q.x) );
//
//
//			if(hypotenuse > 10)
//			{
//				q.x = (int) (p.x - 3 * hypotenuse * cos(angle));
//				q.y = (int) (p.y - 3 * hypotenuse * sin(angle));
//
//				mean.x += q.x;
//				mean.y += q.y;
//
//				line( outImg, p, q, line_color, line_thickness, CV_AA, 0 );
//
//				float pi = 3.14;
//				p.x = (int) (q.x + 9 * cos(angle + pi / 4));
//				p.y = (int) (q.y + 9 * sin(angle + pi / 4));
//				line( outImg, p, q, line_color, line_thickness, CV_AA, 0 );
//				p.x = (int) (q.x + 9 * cos(angle - pi / 4));
//				p.y = (int) (q.y + 9 * sin(angle - pi / 4));
//				line( outImg, p, q, line_color, line_thickness, CV_AA, 0 );
//				countPoint ++;
//			}
//		}//end for
//		if(countPoint)
//		{
//			mean.x = mean.x / countPoint; mean.y = mean.y / countPoint;
//			circle( outImg, mean , 5, Scalar(0,255,255), 8, 8);
//		}
//
//		cout << "SIZE: " << countPoint << " / " << curSize << endl;
//		motionX = mean.x;
//		motionY = mean.y;
//	}
//	std::swap(points[1], points[0]);
//	cv::swap(prev, gray);
//
//	imshow("TRACK MOTION", outImg);
//	waitKey(10);
//}
