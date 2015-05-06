#include <iostream>
#include <opencv2/opencv.hpp>


#include "ShapeAlignment.h"
#include "Configuration.h"
#include "utility.h"

#include "FacialExpression.h"
using namespace cv;
using namespace std;

int main() {
    cout << "Face Detection - Facial Landmarking - Face Expression!" << endl;


    // ------------------------- LOAD MODEL ------------------------------

    // DETECTION
    // --- Load detection ---
    String face_cascade_name = "/home/robotbase/github/MyBackupCode/FacialPointDetection/haarcascade_frontalface_alt.xml";
    CascadeClassifier face_cascade;
    if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n"); return -1; };

    // FACIAL LANDMARK
    // -------------- Read Configuration --------------
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config_ibug.txt");
    const int num_of_training = options.getNumOfTraining();
    const int num_of_landmark = options.getNumOfLandmark();
    const int first_level = options.getNumOfFirstLevel();
    const int second_level = options.getNumOfSecondLevel();
    const int feature_per_fern = options.getNumOfFeaturePerFern();
    vector<Mat_<double>> keypoints;
    vector<Rect_<int>> bounding_boxes;
    // -------------- READ BOUNDING BOX ----------
    string bounding_box_train_path = options.getTrainBoundingBoxPath();
    readBoundingBoxes(num_of_training, bounding_boxes, bounding_box_train_path);
    // -------------- READ KEYPOINTS -------------
    string train_path = options.getTrainKeypointsPath();
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    ShapeAlignment shapeAlignmentTest(first_level, second_level, feature_per_fern);
    shapeAlignmentTest.addKeyPoints(keypoints);
    shapeAlignmentTest.addBoundingBoxes(bounding_boxes);
    shapeAlignmentTest.Load(options.getModelPath());

    cout << "Load shapeModel successfully" << endl;

    // EXPRESSION
    // -------------- Load Model ----------------
    FacialExpression facialExpression;
    facialExpression.Load("/home/robotbase/github/MyBackupCode/FacialExpression/expressionModel.txt");


    cout << "START PROCESS IMAGE: " << endl;
    VideoCapture cap(0);
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get Image
//    Mat img = imread("/home/robotbase/github/MyBackupCode/FacePointExpressionCombine/test.jpg", CV_LOAD_IMAGE_COLOR);
    Mat img;
    for(int i = 0 ; i < 10; i++){


        bool bSuccess = cap.read(img); // read a new frame from video
        if (!bSuccess) //if not success, break loop
        {
            cout << "Cannot read a frame from video file" << endl;
            return -1;
        }

        // Detection

        if (!cap.isOpened())  // if not success, exit program
        {
            cout  << "Cannot open the video file" << endl;
            return -1;
        }


        // --- Detect faces ---
        int64 t0 = cv::getTickCount();
        std::vector<Rect_<int>> faces;
        face_cascade.detectMultiScale( img, faces, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT, Size(60, 60) );

        if(faces.size() < 0){
            cout << "No face detected";
            return 0;
        }

        int64 t1 = cv::getTickCount();
        double secs = (t1-t0)/cv::getTickFrequency();
        cout << "Detect Done: " << secs << endl;
        // Landmarking

        t0 = cv::getTickCount();

        Mat_<unsigned char> img_gray;
        cvtColor(img, img_gray, COLOR_RGB2GRAY);
        Mat_<double> prediction_keypoint = shapeAlignmentTest.Test(img_gray, faces[0]);

        t1 = cv::getTickCount();
        secs = (t1-t0)/cv::getTickFrequency();
        cout << "Landmark Done: " << secs << endl;
        // Expression

        t0 = cv::getTickCount();

        Mat_<double> keypoint_norm = normalizeKeypoint(prediction_keypoint);
        int predict = facialExpression.Test(keypoint_norm);

        if(predict == 0){
            predict = 3;
        } else {
            predict = 6;
        }

        t1 = cv::getTickCount();
        secs = (t1-t0)/cv::getTickFrequency();
        cout << "Expression Done: " << secs << endl;

        // plot face bounding box
        rectangle(img, faces[0], (255,255,255), 2);
        putText(img, EXPRESSION_NAME[predict], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        visualizeImage(img, prediction_keypoint, 10 , false, "result", true);
    }
    return 0;
}