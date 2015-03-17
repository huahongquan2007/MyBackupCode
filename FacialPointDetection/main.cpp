#include <iostream>
#include <opencv2/opencv.hpp>

#include <map>

using namespace cv;
using namespace std;

#include "utility.h"
#include "Configuration.h"

int main() {
    cout << "Facial Point Detection Projects" << endl;

    // =========================================
    // Read Configuration
    Configuration options("/home/robotbase/github/MyBackupCode/FacialPointDetection/config.txt");

    // =========================================
    // Training
    const int num_of_training = options.getNumOfTraining();


    const string train_data = options.getDatasetTrainPath();
    // -------------- READ IMAGE ---------------
    string img_name = "";

    for(int i = 0; i < num_of_training; i++){
        img_name = train_data + to_string(i+1) + ".jpg";
        cout << "train_img: " << img_name << endl;
    }

    cout << "Start Training" << endl;

    // =========================================
    // Testing
    cout << "Start Testing" << endl;

    return 0;
}