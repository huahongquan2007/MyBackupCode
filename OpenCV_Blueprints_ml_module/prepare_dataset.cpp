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
void processJAFFE(string input, string output, string feature_name);
void processKAGGLE(string input, string output, string feature_name);
int main(int argc, char* argv[]) {
    cout << "============= PREPARE DATASET =============" << endl;

    if(argc != 9){
        cout << "Usage: " << endl;
        cout << argv[0] << " -dataset <dataset_name> -feature <feature_name> -src <input> -dest <output_folder>" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string dataset_name;
    string feature_name;
    string input;
    string output_folder;
    int i;
    for(i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-dataset") == 0 ){
            if(i + 1 >= argc) return -1;
            dataset_name = argv[i + 1];
        }
        if( strcmp(argv[i], "-feature") == 0 ){
            if(i + 1 >= argc) return -1;
            feature_name = argv[i + 1];
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
        processJAFFE(input, output_folder, feature_name);
    }

    // ********************
    // KAGGLE Dataset
    // ********************
    if(dataset_name == "kaggle"){
        processKAGGLE(input, output_folder, feature_name);
    }

    return 0;
}

void processJAFFE(string input, string output, string feature_name){
    string output_path = output + "/" + feature_name +"_features.yml";
    FileStorage fs( output_path , FileStorage::WRITE);

    cout << "Process JAFFE: " << input << endl;
    Mat img, feature;
    vector<string> imgPath = listFile(input);

    fs << "num_of_image" << (int) imgPath.size();
    for(int i = 0 ; i < imgPath.size(); i++){
        // load image
        img = imread(imgPath[i], CV_LOAD_IMAGE_ANYCOLOR);

        // extract feature
        feature = extractFeature(img, "SIFT");

        // extract label
        // TODO: trich label tu trong filename

        // save feature & label
        fs << "image_feature_" + to_string(i) << feature;
        fs << "image_path_" + to_string(i) << imgPath[i];

        cout << i << "/" << imgPath.size() << endl;
    }
    fs.release();

    cout << "Features saved: " << output_path << endl;

    waitKey(0);
}

void processKAGGLE(string input, string output, string feature_name){
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
                    imgPath.push_back(folder + "/" + fname);
                }
            }
        }
    }

    return imgPath;
}
Mat extractFeature(Mat image, string feature_type){
    cv::SiftFeatureDetector detector;
    std::vector<cv::KeyPoint> keypoints;
    detector.detect(image, keypoints);

    Ptr<DescriptorExtractor> featureExtractor = DescriptorExtractor::create("SIFT");
    Mat descriptors;
    featureExtractor->compute(image, keypoints, descriptors);

    return descriptors;
}