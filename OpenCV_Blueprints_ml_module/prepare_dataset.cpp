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
    RNG rng;
    int num_of_test = 0;
    vector<string> imgPath = listFile(input);

    fs << "num_of_image" << (int) imgPath.size();
    fs << "num_of_label" << 7;
    fs << "label_0" << "Angry";
    fs << "label_1" << "Disgusted";
    fs << "label_2" << "Fear";
    fs << "label_3" << "Happy";
    fs << "label_4" << "Neural";
    fs << "label_5" << "Sad";
    fs << "label_6" << "Surprised";
    for(int i = 0 ; i < imgPath.size(); i++){
        // load image
        img = imread(imgPath[i], CV_LOAD_IMAGE_ANYCOLOR);

        // extract feature
        feature = extractFeature(img, "SIFT");

        // extract label
        string fileName = imgPath[i].substr(input.length() + 1, imgPath[i].length());
        string ex_code = fileName.substr(3, 2);

        int label = -1;
        if(ex_code == "AN"){
            label = 0;
        } else if(ex_code == "DI"){
            label = 1;
        } else if(ex_code == "FE"){
            label = 2;
        } else if(ex_code == "HA"){
            label = 3;
        } else if(ex_code == "NE"){
            label = 4;
        } else if(ex_code == "SA"){
            label = 5;
        } else if(ex_code == "SU"){
            label = 6;
        }

        // save feature & label
        fs << "image_feature_" + to_string(i) << feature;
        fs << "image_label_" + to_string(i) << label;
        fs << "image_path_" + to_string(i) << imgPath[i];

        // decide train or test
        double c = rng.uniform(0., 1.);
        bool isTrain = true;
        if(c > 0.8){
            isTrain = false;
            num_of_test += 1;
        }
        fs << "image_is_train_" + to_string(i) << isTrain;

        cout << i << "/" << imgPath.size() << endl;
    }
    int feature_size = feature.cols * feature.rows;
    fs << "num_of_train" << (int) imgPath.size() - num_of_test;
    fs << "num_of_test" << num_of_test;
    fs << "feature_size" << feature_size;
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
    cv::DenseFeatureDetector detector;
    std::vector<cv::KeyPoint> keypoints;
    detector.detect(image, keypoints);

    Ptr<DescriptorExtractor> featureExtractor = DescriptorExtractor::create("SIFT");
    Mat descriptors;
    featureExtractor->compute(image, keypoints, descriptors);

    return descriptors.reshape(0, 1);
}