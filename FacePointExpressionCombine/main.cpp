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


    // Get Image
    Mat img = imread("/home/robotbase/github/MyBackupCode/FacePointExpressionCombine/test.jpg", CV_LOAD_IMAGE_COLOR);
    Mat result = img.clone();
    // Detection
    // --- Load detection ---
    String face_cascade_name = "/home/robotbase/github/MyBackupCode/FacialPointDetection/haarcascade_frontalface_alt.xml";
    CascadeClassifier face_cascade;
    if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n"); return -1; };

    // --- Detect faces ---
    std::vector<Rect_<int>> faces;
    face_cascade.detectMultiScale( img, faces, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT, Size(60, 60) );

    if(faces.size() < 0){
        cout << "No face detected";
        return 0;
    }
    // plot face bounding box
    rectangle(result, faces[0], (255,255,255), 2);

    // Landmarking
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

    Mat_<unsigned char> img_gray;
    cvtColor(img, img_gray, COLOR_RGB2GRAY);
    Mat_<double> prediction_keypoint = shapeAlignmentTest.Test(img_gray, faces[0]);


    // Expression
    // --- Load Model ---
    FacialExpression facialExpression;
    facialExpression.Load("/home/robotbase/github/MyBackupCode/FacialExpression/expressionModel.txt");

    Mat_<double> keypoint_norm = normalizeKeypoint(prediction_keypoint);
    int predict = facialExpression.Test(keypoint_norm);

    putText(result, EXPRESSION_NAME[predict], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
    visualizeImage(result, prediction_keypoint, 0 , false, "result", true);
    waitKey(0);
    return 0;
}