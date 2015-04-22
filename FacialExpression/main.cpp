#include <iostream>
#include <fstream>
#include <opencv2/opencv.hpp>
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

using namespace cv;
using namespace std;

#include "utility.h"

int main() {
    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    int num_of_training = 213;
    int num_of_landmark = 68;

    vector<Mat_<double>> keypoints;

    // -------------- READ KEYPOINTS -------------
    string train_path = "/home/robotbase/github/MyBackupCode/FacialExpression/result_keypoints.txt";
    readKeypoints(num_of_training, num_of_landmark, keypoints, train_path);

    vector<string> expression_labels;

    // READ DATASET
    cout << "Read Dataset" << endl;
    ifstream input("/home/robotbase/github/MyBackupCode/FacialExpression/Datasets/JAFFE/listJAFFE.txt", ifstream::in);

    string str;
    vector<string> listImagePath;
    while( getline(input, str) ){
        listImagePath.push_back(str);

        string ex_code = str.substr(3, 2);

        if(ex_code == "AN"){
            ex_code = "Angry";
        } else if(ex_code == "DI"){
            ex_code = "Disgusted";
        } else if(ex_code == "FE"){
            ex_code = "Fear";
        } else if(ex_code == "HA"){
            ex_code = "Happy";
        } else if(ex_code == "NE"){
            ex_code = "Neural";
        } else if(ex_code == "SA"){
            ex_code = "Sad";
        } else if(ex_code == "SU"){
            ex_code = "Surprised";
        }

        expression_labels.push_back( ex_code );
    }

    cout << "Read Images " << listImagePath.size() << endl;

    for(int i = 0 ; i < listImagePath.size(); i++){
        Mat_<unsigned char> img = imread("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/JAFFE/" + listImagePath[i] , CV_LOAD_IMAGE_GRAYSCALE);
        equalizeHist( img, img );

        putText(img, expression_labels[i], Point(0, 50), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        putText(img, listImagePath[i], Point(0, 100), FONT_HERSHEY_COMPLEX, 1.0, (255, 255, 255));
        visualizeImage(img, keypoints[i], 0 , false, "result");
    }

    waitKey(0);
    return 0;
}