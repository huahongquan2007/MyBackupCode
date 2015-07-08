#include <iostream>

#include "opencv2/opencv.hpp"
#include "opencv2/highgui.hpp"
#include <opencv2/features2d.hpp>
#include "opencv2/xfeatures2d.hpp"
#include <opencv2/imgproc.hpp>

using namespace std;
using namespace cv;

Mat extractFeature(Mat eyeLeft, Mat eyeRight, Mat mouth, string feature_type);
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

    string feature;
    string input_folder;
    string output_folder;

    for(int i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-feature") == 0 ){
            if(i + 1 >= argc) return -1;
            feature = argv[i + 1];
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

    cout << "feature: " << feature << endl;
    cout << "src: " << input_folder << endl;
    cout << "dest: " << output_folder << endl;

    // READ YML FILES
    string input_file = input_folder + "/list.yml";
    FileStorage in( input_file, FileStorage::READ);
    int num_of_image = 0;
    in["num_of_image"] >> num_of_image;
    num_of_image = 5;

    cout << "num of image : " << num_of_image << endl;
    for(int i = 0 ; i < num_of_image; i++){
        string eyeLeftPath, eyeRightPath, mouthPath;
        in["img_" + to_string(i) + "_eyeLeft"] >> eyeLeftPath;
        in["img_" + to_string(i) + "_eyeRight"] >> eyeRightPath;
        in["img_" + to_string(i) + "_mouth"] >> mouthPath;
        cout << "---------" << endl;
        cout << eyeLeftPath << endl;
        cout << eyeRightPath << endl;
        cout << mouthPath << endl;

        Mat eyeLeft = imread(eyeLeftPath, CV_LOAD_IMAGE_GRAYSCALE);
        Mat eyeRight = imread(eyeLeftPath, CV_LOAD_IMAGE_GRAYSCALE);
        Mat mouth = imread(mouthPath, CV_LOAD_IMAGE_GRAYSCALE);

        Mat result = extractFeature(eyeLeft, eyeRight, mouth, feature);

        cout << "Feature size: " << result.rows << endl;
    }
    return 0;
}

Mat extractFeature(Mat eyeLeft, Mat eyeRight, Mat mouth, string feature_type){
    // extract image features from each region
    Mat eyeLeftFeature = extractImageFeature(eyeLeft, feature_type);
    Mat eyeRightFeature = extractImageFeature(eyeRight, feature_type);
    Mat mouthFeature = extractImageFeature(mouth , feature_type);

    // create a result Mat to contain all features
    int num_of_col = eyeLeftFeature.cols;
    int num_of_row = eyeLeftFeature.rows + eyeRightFeature.rows + mouthFeature.rows;
    Mat result = Mat::zeros(num_of_row, num_of_col, eyeLeftFeature.type());

    // copy features into result Mat
    if(eyeLeftFeature.rows > 0) eyeLeftFeature.copyTo(result.rowRange(0, eyeLeftFeature.rows));
    if(eyeRightFeature.rows > 0) eyeRightFeature.copyTo(result.rowRange(eyeLeftFeature.rows, eyeLeftFeature.rows + eyeRightFeature.rows));
    if(mouthFeature.rows > 0) mouthFeature.copyTo(result.rowRange(eyeLeftFeature.rows + eyeRightFeature.rows, result.rows));

    return result;
}
Mat extractImageFeature(Mat img, string feature_type){

    vector<KeyPoint> keypoints;
    Mat descriptors;

    Ptr<Feature2D> detector;

    // if / switch here
    if(feature_type.compare("orb") == 0){
        detector = ORB::create();
        detector->detect(img, keypoints, Mat());
        detector->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("dense-orb") == 0){
        detector = ORB::create();
        createDenseFeature(keypoints, img);

        cout << "num of keypoints in elseif: " << keypoints.size() << endl;
        detector->detectAndCompute(img, Mat(), keypoints, descriptors, true);
//        detector->compute(img, keypoints, descriptors);
    }else if(feature_type.compare("sift") == 0){
        detector = xfeatures2d::SIFT::create();
        detector->detect(img, keypoints, Mat());
        detector->compute(img, keypoints, descriptors);
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