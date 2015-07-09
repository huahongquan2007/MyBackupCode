#include <iostream>

#include "opencv2/opencv.hpp"
#include "opencv2/highgui.hpp"
#include <opencv2/features2d.hpp>
#include "opencv2/xfeatures2d.hpp"
#include <opencv2/imgproc.hpp>
#include <vector>

using namespace std;
using namespace cv;

Mat extractFeature(Mat face, string feature_name);
Mat extractImageFeature(Mat img, string feature_type);
void createDenseFeature(vector<KeyPoint> &keypoints, Mat image, float initFeatureScale=1.f, int featureScaleLevels=1,
                                    float featureScaleMul=0.1f,
                                    int initXyStep=6, int initImgBound=0,
                                    bool varyXyStepWithScale=true,
                                    bool varyImgBoundWithScale=false);
int main(int argc, char* argv[]) {
    cout << "============= FEATURE EXTRACTION =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -feature <feature_name> -src <input_folder> -dest <output_folder>" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string feature_name;
    string input_folder;
    string output_folder;

    for(int i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-feature") == 0 ){
            if(i + 1 >= argc) return -1;
            feature_name = argv[i + 1];
        }

        if( strcmp(argv[i], "-src") == 0 ){
            if(i + 1 >= argc) return -1;
            input_folder = argv[i + 1];
        }

        if( strcmp(argv[i], "-dest") == 0 ){
            if(i + 1 >= argc) return -1;
            output_folder = argv[i + 1];
        }
    }

    // *********************
    // extract feature
    // *********************

    cout << "feature: " << feature_name << endl;
    cout << "src: " << input_folder << endl;
    cout << "dest: " << output_folder << endl;

    // READ YML FILES
    string input_file = input_folder + "/list.yml";
    FileStorage in( input_file, FileStorage::READ);

    string output_path = output_folder + "/" + feature_name +"_features.yml";
    FileStorage fs( output_path , FileStorage::WRITE);

    int num_of_image = 0;
    in["num_of_image"] >> num_of_image;
//    num_of_image = 50;

    cout << "num of image : " << num_of_image << endl;
    vector<Mat> features_vector;

    // extract image features from facial components
    for(int i = 0 ; i < num_of_image; i++){
        string eyeLeftPath, eyeRightPath, mouthPath;
        in["img_" + to_string(i) + "_eyeLeft"] >> eyeLeftPath;
        in["img_" + to_string(i) + "_eyeRight"] >> eyeRightPath;
        in["img_" + to_string(i) + "_mouth"] >> mouthPath;

        string facePath;
        in["img_" + to_string(i) + "_face"] >> facePath;

        Mat face = imread(facePath, CV_LOAD_IMAGE_GRAYSCALE);
        resize(face, face, Size(80, 80));
//        Mat result = extractImageFeature(face, feature_name);

//        Mat eyeLeft = imread(eyeLeftPath, CV_LOAD_IMAGE_GRAYSCALE);
//        Mat eyeRight = imread(eyeRightPath, CV_LOAD_IMAGE_GRAYSCALE);
//        Mat mouth = imread(mouthPath, CV_LOAD_IMAGE_GRAYSCALE);
//
        Mat result = extractFeature(face, feature_name);

        cout << "Feature size: " << result.rows << endl;

        features_vector.push_back(result);
    }

    // --------------- clustering -----------------
    // compute feature distribution over k bins
    // create 1 featureData Mat with each feature as a row
    int num_of_feature = 0;
    for(int i = 0 ; i < features_vector.size(); i++){
        num_of_feature += features_vector[i].rows;
    }
    cout << "num_of_feature: " << num_of_feature << endl;

    Mat rawFeatureData = Mat::zeros(num_of_feature, features_vector[0].cols, CV_32FC1);
    int cur_idx = 0;

    for(int i = 0 ; i < features_vector.size(); i++){
        features_vector[i].copyTo(rawFeatureData.rowRange(cur_idx, cur_idx + features_vector[i].rows));
        cur_idx += features_vector[i].rows;
    }

    // --- compute kmeans
    Mat labels, centers;
    int bin_size = 1000;
    kmeans(rawFeatureData, bin_size, labels, TermCriteria( TermCriteria::COUNT+TermCriteria::EPS, 100, 1.0),
           3, KMEANS_PP_CENTERS, centers);

    // compute final feature
    cur_idx = 0;
    int num_of_test = 0;
    RNG rng;
    Mat featureDataOverBins = Mat::zeros(num_of_image, bin_size, CV_32FC1); //[num_of_image * bin_size]
    for(int i = 0 ; i < num_of_image; i++){ // for each image
        Mat feature = Mat::zeros(1, bin_size, CV_32FC1);
        int num_of_image_features = features_vector[i].rows;
        for(int j = 0; j < num_of_image_features; j++){ // for each feature in cur image
            int bin = labels.at<int>(cur_idx + j);
            feature.at<float>(0, bin) += 1;
        }

        normalize( feature, feature );

        string path;
        in["img_" + to_string(i) + "_eyeLeft"] >> path;

        // extract label
        string fileName = path.substr(input_folder.length() + 1, path.length());
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

        cout << "histogram feature: " << endl << feature << " label: " << label << " " <<  path << endl;

//        fs << "image_feature_" + to_string(i) << feature;


        fs << "image_label_" + to_string(i) << label;

        // decide train or test
        double c = rng.uniform(0., 1.);
        bool isTrain = true;
        if(c > 0.8){
            isTrain = false;
            num_of_test += 1;
        }
        fs << "image_is_train_" + to_string(i) << isTrain;

        feature.copyTo(featureDataOverBins.row(i));

        cur_idx += features_vector[i].rows;
    }

//    // --- PCA
//    // perform PCA
    PCA pca(featureDataOverBins, cv::Mat(), CV_PCA_DATA_AS_ROW, 0.90);
    int feature_size = pca.eigenvectors.rows;
    Mat feature;
    for(int i = 0 ; i < num_of_image; i++){
        feature = pca.project(featureDataOverBins.row(i));
        fs << "image_feature_" + to_string(i) << feature;
    }
    cout << "feature_size: " << feature_size << endl;

    // NO PCA
//    int feature_size = bin_size;
    fs << "feature_size" << feature_size;
    // save result

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
    fs << "pca_mean" << pca.mean;
    fs << "pca_eigenvalues" << pca.eigenvalues;
    fs << "pca_eigenvectors" << pca.eigenvectors;
    fs << "centers" << centers;

    fs.release();
    return 0;
}
Mat extractBrisk(Mat img){
    Mat descriptors;
    vector<KeyPoint> keypoints;

    Ptr<DescriptorExtractor> brisk = BRISK::create();
    brisk->detect(img, keypoints, Mat());
    brisk->compute(img, keypoints, descriptors);

    return descriptors;
}
Mat extractKaze(Mat img){
    Mat descriptors;
    vector<KeyPoint> keypoints;

    Ptr<DescriptorExtractor> kaze = KAZE::create();
    kaze->detect(img, keypoints, Mat());
    kaze->compute(img, keypoints, descriptors);

    return descriptors;
}
Mat extractSift(Mat img){
    Mat descriptors;
    vector<KeyPoint> keypoints;

    Ptr<Feature2D> sift = xfeatures2d::SIFT::create();
    sift->detect(img, keypoints, Mat());
    sift->compute(img, keypoints, descriptors);

    return descriptors;
}
Mat extractDenseSift(Mat img){
    Mat descriptors;
    vector<KeyPoint> keypoints;

    Ptr<Feature2D> sift = xfeatures2d::SIFT::create();
    createDenseFeature(keypoints, img);
    sift->compute(img, keypoints, descriptors);

    return descriptors;
}
Mat extractDaisy(Mat img){
    Mat descriptors;
    vector<KeyPoint> keypoints;

    Ptr<FeatureDetector> surf = xfeatures2d::SURF::create();
    surf->detect(img, keypoints, Mat());
    Ptr<DescriptorExtractor> daisy = xfeatures2d::DAISY::create();
    daisy->compute(img, keypoints, descriptors);

    return descriptors;
}
Mat extractFeature(Mat img, string feature_name){
    Mat descriptors;
    if(feature_name.compare("brisk") == 0){
        descriptors = extractBrisk(img);
    } else if(feature_name.compare("kaze") == 0){
        descriptors = extractKaze(img);
    } else if(feature_name.compare("sift") == 0){
        descriptors = extractSift(img);
    } else if(feature_name.compare("dense-sift") == 0){
        descriptors = extractDenseSift(img);
    } else if(feature_name.compare("daisy") == 0){
        descriptors = extractDaisy(img);
    }
    return descriptors;
}
//Mat extractFeature(Mat eyeLeft, Mat eyeRight, Mat mouth, string feature_type){
//    // extract image features from each region
//    Mat eyeLeftFeature = extractImageFeature(eyeLeft, feature_type);
//    Mat eyeRightFeature = extractImageFeature(eyeRight, feature_type);
//    Mat mouthFeature = extractImageFeature(mouth , feature_type);
//
//    // create a result Mat to contain all features
//    int num_of_col = eyeLeftFeature.cols;
//    int num_of_row = eyeLeftFeature.rows + eyeRightFeature.rows + mouthFeature.rows;
//    Mat result = Mat::zeros(num_of_row, num_of_col, eyeLeftFeature.type());
//
//    // copy features into result Mat
//    if(eyeLeftFeature.rows > 0) eyeLeftFeature.copyTo(result.rowRange(0, eyeLeftFeature.rows));
//    if(eyeRightFeature.rows > 0) eyeRightFeature.copyTo(result.rowRange(eyeLeftFeature.rows, eyeLeftFeature.rows + eyeRightFeature.rows));
//    if(mouthFeature.rows > 0) mouthFeature.copyTo(result.rowRange(eyeLeftFeature.rows + eyeRightFeature.rows, result.rows));
//
//    return result;
//}
Mat extractImageFeature(Mat img, string feature_type){

    vector<KeyPoint> keypoints;
    Mat descriptors;

    Ptr<Feature2D> detector;

    // if / switch here
    if(feature_type.compare("brisk") == 0){
//        detector = FastFeatureDetector::create();
//        detector->detect(img, keypoints, Mat());
        Ptr<DescriptorExtractor> extractor = BRISK::create();
        extractor->detect(img, keypoints, Mat());
        extractor->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("kaze") == 0){
        Ptr<DescriptorExtractor> extractor = KAZE::create();
        extractor->detect(img, keypoints, Mat());
        extractor->compute(img, keypoints, descriptors);
    }
    else if(feature_type.compare("dense-orb") == 0){
        createDenseFeature(keypoints, img);
        cout << "num of keypoints in elseif: " << keypoints.size() << endl;
        detector = xfeatures2d::SIFT::create();
        equalizeHist(img, img);
        detector->compute(img, keypoints, descriptors);
//        detector->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("sift") == 0){
        detector = xfeatures2d::SIFT::create();
        detector->detect(img, keypoints, Mat());
        detector->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("daisy") == 0){
        Ptr<FeatureDetector> brisk = xfeatures2d::SURF::create();
        brisk->detect(img, keypoints, Mat());
        Ptr<DescriptorExtractor> daisy = xfeatures2d::DAISY::create();
        daisy->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("gabor") == 0){
        int kernel_size = 31;
        double sigma = 1, theta = 0, lambd = 51, gamma = 0.03, psi = 0;

        Mat img0 , img45, img90, img135;
        cv::Mat kernel = cv::getGaborKernel(cv::Size(kernel_size,kernel_size), sigma, theta, lambd, gamma, psi);
        cv::filter2D(img, img0, CV_32F, kernel);

        theta = 45;
        kernel = cv::getGaborKernel(cv::Size(kernel_size,kernel_size), sigma, theta, lambd, gamma, psi);
        cv::filter2D(img, img45, CV_32F, kernel);

        theta = 90;
        kernel = cv::getGaborKernel(cv::Size(kernel_size,kernel_size), sigma, theta, lambd, gamma, psi);
        cv::filter2D(img, img90, CV_32F, kernel);

        theta = 135;
        kernel = cv::getGaborKernel(cv::Size(kernel_size,kernel_size), sigma, theta, lambd, gamma, psi);
        cv::filter2D(img, img135, CV_32F, kernel);

//        descriptors = (img0 + img45 + img90 + img135) / 4;
        cout << "before descriptos" << endl;
        int w = 40;
        descriptors = Mat::zeros(4, w * w, img0.type());

        resize(img0, img0, Size(w, w));
        img0 = img0.reshape(0, 1);
        img0.copyTo(descriptors.row(0));

        resize(img45, img45, Size(w, w));
        img45 = img45.reshape(0, 1);
        img45.copyTo(descriptors.row(1));

        resize(img90, img90, Size(w, w));
        img90 = img90.reshape(0, 1);
        img90.copyTo(descriptors.row(2));

        resize(img135, img135, Size(w, w));
        img135 = img135.reshape(0, 1);
        img135.copyTo(descriptors.row(3));
//        descriptors = img45;
//        descriptors = img;

        cout << "feature descriptors: " << descriptors.size() << endl;

//        Mat viz;
//        img0.convertTo(viz,CV_8U,1.0/255.0);     // move to proper[0..255] range to show it
//        equalizeHist(viz, viz);
//        imshow("img0", viz);
//
//        img45.convertTo(viz,CV_8U,1.0/255.0);     // move to proper[0..255] range to show it
//        equalizeHist(viz, viz);
//        imshow("img45", viz);
//
//        img90.convertTo(viz,CV_8U,1.0/255.0);     // move to proper[0..255] range to show it
//        equalizeHist(viz, viz);
//        imshow("img90", viz);
//
//
//        descriptors.convertTo(viz,CV_8U,1.0/255.0);     // move to proper[0..255] range to show it
//        equalizeHist(viz, viz);
//        imshow("img", img);
//        imshow("d",viz);
//        resize(kernel, kernel, Size(200, 200));
//        imshow("k",kernel);
//
//        waitKey(0);

    }

//    cout << "feature_type: " << feature_type << endl;
//    cout << "image size: " << img.size() << endl;
//    cout << "num of keyupoints: " << keypoints.size() << endl;
//    for(int i = 0 ; i < keypoints.size() ; i ++){
//        cout << i << " " << keypoints[i].pt << endl;
//    }
//    cout << "Done" << endl;

//    detector->detectAndCompute(img, Mat(), keypoints, descriptors, false);
    // We can detect keypoint with detect method
//    b->detect(img1, keyImg1, Mat());
//    // and compute their descriptors with method  compute
//    b->compute(img1, keyImg1, descImg1);
//    // or detect and compute descriptors in one step
//    b->detectAndCompute(img2, Mat(),keyImg2, descImg2,false);

    return descriptors;
}

void createDenseFeature(vector<KeyPoint> &keypoints, Mat image, float initFeatureScale, int featureScaleLevels,
                                    float featureScaleMul,
                                    int initXyStep, int initImgBound,
                                    bool varyXyStepWithScale,
                                    bool varyImgBoundWithScale){
//    vector<KeyPoint> keypoints;

    if(image.rows < 100){
        cout << "is mouth" << endl;
    }

    float curScale = static_cast<float>(initFeatureScale);
    int curStep = initXyStep;
    int curBound = initImgBound;
    for( int curLevel = 0; curLevel < featureScaleLevels; curLevel++ )
    {
        for( int x = curBound; x < image.cols - curBound; x += curStep )
        {
            for( int y = curBound; y < image.rows - curBound; y += curStep )
            {
                keypoints.push_back( KeyPoint(static_cast<float>(x), static_cast<float>(y), curScale) );
            }
        }

        curScale = static_cast<float>(curScale * featureScaleMul);
        if( varyXyStepWithScale ) curStep = static_cast<int>( curStep * featureScaleMul + 0.5f );
        if( varyImgBoundWithScale ) curBound = static_cast<int>( curBound * featureScaleMul + 0.5f );
    }
    cout << "Keypoints in function size: " << keypoints.size() << endl;
//    return keypoints;
}