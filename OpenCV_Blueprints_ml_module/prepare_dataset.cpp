#include <iostream>
#include <fstream>
#include <dirent.h>
#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/nonfree/features2d.hpp>
using namespace cv;
using namespace std;

vector<string> listFile(string folder);
Mat extractFeature(Mat image, string feature_type);
void processJAFFE(string input, string output);
void processKAGGLE(string input, string output);
int main(int argc, char* argv[]) {
    cout << "============= PREPARE DATASET =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -dataset <dataset_name> -src <input> -dest <output_folder>" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string dataset_name;
    string input;
    string output_folder;
    int i;
    for(i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-dataset") == 0 ){
            if(i + 1 >= argc) return -1;
            dataset_name = argv[i + 1];
        }

        if( strcmp(argv[i], "-src") == 0 ){
            if(i + 1 >= argc) return -1;
            input = argv[i + 1];
        }

        if( strcmp(argv[i], "-dest") == 0 ){
            if(i + 1 >= argc) return -1;
            output_folder = argv[i + 1];
        }
    }

    // ********************
    // JAFFE Dataset
    // ********************
    if(dataset_name == "jaffe"){
        processJAFFE(input, output_folder);
    }

    // ********************
    // KAGGLE Dataset
    // ********************
    if(dataset_name == "kaggle"){
        processKAGGLE(input, output_folder);
    }

    return 0;
}

void processJAFFE(string input, string output){
    cout << "Process JAFFE: " << input << endl;

    vector<string> imgPath = listFile(input);
    for(int i = 0 ; i < imgPath.size(); i++){
        cout << imgPath[i] << " " << endl;
    }

    Mat img;
    img = imread(imgPath[0], CV_LOAD_IMAGE_ANYCOLOR);
    imshow("Test", img);

    extractFeature(img, "SIFT");
    waitKey(0);

    cout << "Output: " << output << endl;
}

void processKAGGLE(string input, string output){
    cout << "Hello KAGGLE" << endl;
    cout << "Output: " << output << endl;
}

vector<string> listFile(string folder){
    vector<string> imgPath;
    DIR *pDIR;
    struct dirent *entry;
    if( pDIR=opendir(folder.c_str()) ){
        while(entry = readdir(pDIR)){
            if (entry->d_type == DT_REG){		// if entry is a regular file
                std::string fname = entry->d_name;	// filename
                std::string::size_type size = fname.find(".tiff");
                if(size != std::string::npos){
//                    cout << " FILE: " << folder + "/" + fname << endl;
                    imgPath.push_back(folder + "/" + fname);
                }
            }
        }
    }

    return imgPath;
}
Mat extractFeature(Mat image, string feature_type){
    Mat feature;
    cv::SiftFeatureDetector detector;
    std::vector<cv::KeyPoint> keypoints;
    detector.detect(image, keypoints);

    Ptr<DescriptorExtractor> featureExtractor = DescriptorExtractor::create("SIFT");
    Mat descriptors;
    featureExtractor->compute(image, keypoints, descriptors);

    Mat outputImage;
    Scalar keypointColor = Scalar(255, 0, 0);     // Blue keypoints.
    drawKeypoints(image, keypoints, outputImage, keypointColor, DrawMatchesFlags::DEFAULT);

    imshow("Output", outputImage);

    imshow("descriptor", descriptors);

    waitKey(0);

    return feature;
}