#include <iostream>
#include <fstream>
#include <dirent.h>
#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
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

    int num_of_image = imgPath.size();
//    num_of_image = 10;

    vector<Mat> features_vector;

    for(int i = 0 ; i < num_of_image; i++){
        // load image
        img = imread(imgPath[i], CV_LOAD_IMAGE_COLOR);

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
        features_vector.push_back(feature);
//        fs << "image_feature_" + to_string(i) << feature;
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

    // compute feature distribution over k bins
    int num_of_feature = 0;
    for(int i = 0 ; i < features_vector.size(); i++){
        cout << features_vector[i].cols << " " << features_vector[i].rows << endl;
        num_of_feature += features_vector[i].rows;
    }
    cout << "num_of_feature: " << num_of_feature << endl;

    Mat featureData = Mat::zeros(num_of_feature, features_vector[0].cols, CV_32FC1);
    int cur_idx = 0;
    for(int i = 0 ; i < features_vector.size(); i++){
        features_vector[i].copyTo(featureData.rowRange(cur_idx, cur_idx + features_vector[i].rows));
        cur_idx += features_vector[i].rows;

        cout << features_vector[i].at<float>(0,0) << " " << featureData.at<float>(cur_idx - features_vector[i].rows, 0) << endl;
    }

    // --- compute kmeans
    Mat labels, centers;
    kmeans(featureData, 1000, labels, TermCriteria( TermCriteria::EPS+TermCriteria::COUNT, 100, 1.0),
           3, KMEANS_PP_CENTERS, centers);

    // --- computer feature
    cur_idx = 0;
    for(int i = 0 ; i < features_vector.size(); i++){ // for each image
        features_vector[i].copyTo(featureData.rowRange(cur_idx, cur_idx + features_vector[i].rows));

        Mat feature = Mat::zeros(1, 1000, CV_32FC1);

        for(int j = 0; j < features_vector[i].rows; j++){ // for each feature in cur image
            int bin = labels.at<int>(cur_idx + j);
            feature.at<float>(0, bin) += 1;
        }
        cout << "histogram feature: " << endl << feature << endl;
        normalize(feature, feature, 0, 1, NORM_MINMAX, -1, Mat() );
//        cout << "Feature_norm: " << endl;
//        cout << feature << endl;
        fs << "image_feature_" + to_string(i) << feature;

        // Draw the histograms for B, G and R
//        int hist_w = 512; int hist_h = 512;
//        int bin_w = cvRound( (double) hist_w/1000);
//
//        Mat histImage( hist_h, hist_w, CV_8UC3, Scalar( 255, 255, 255) );

        /// Draw for each channel
//        normalize(feature, feature, 0, histImage.rows, NORM_MINMAX, -1, Mat() );
//        cout << "Feature: " << endl;
//        cout << feature << endl;
//        for( int m = 1; m < feature.cols; m++ )
//        {
//            line( histImage, Point( bin_w*(m-1), hist_h - cvRound( feature.at<float>(m-1)) ) ,
//                  Point( bin_w*(m), hist_h - cvRound( feature.at<float>(m)) ),
//                  Scalar( 255, 0, 0), 2, 8, 0  );
//        }
//
//        /// Display
//        namedWindow("calcHist Demo", CV_WINDOW_AUTOSIZE );
//        imshow("calcHist Demo", histImage );
//
//        waitKey(0);

        cur_idx += features_vector[i].rows;
    }

    cout << "labels" << endl;
    cout << labels.t() << endl;

    // save result
    int feature_size = 1000;
    fs << "num_of_image" << num_of_image;
    fs << "num_of_label" << 7;
    fs << "label_0" << "Angry";
    fs << "label_1" << "Disgusted";
    fs << "label_2" << "Fear";
    fs << "label_3" << "Happy";
    fs << "label_4" << "Neural";
    fs << "label_5" << "Sad";
    fs << "label_6" << "Surprised";
    fs << "num_of_train" << num_of_image - num_of_test;
    fs << "num_of_test" << num_of_test;
    fs << "feature_size" << feature_size;
    fs << "centers" << centers;
    fs.release();

    cout << "Features saved: " << output_path << endl;

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

//    Ptr<DescriptorExtractor> featureExtractor = DescriptorExtractor::create("SIFT");
    Ptr<cv::DescriptorExtractor> oppDescExtractor= DescriptorExtractor::create("SIFT");
    Ptr<OpponentColorDescriptorExtractor> featureExtractor(new OpponentColorDescriptorExtractor(oppDescExtractor));
    Mat descriptors;
    cvtColor(image, image, CV_RGB2BGR);
    featureExtractor->compute(image, keypoints, descriptors);

    return descriptors;
}