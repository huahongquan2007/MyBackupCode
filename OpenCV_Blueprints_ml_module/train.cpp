#include <iostream>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
using namespace cv;
using namespace std;

void mlp(vector<Mat> features, vector<int> labels, vector<string> paths);
int main(int argc, char* argv[]) {
    cout << "============= TRAIN =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -algo <algorithm_name> -src <input_features> -dest <output_folder>" << endl;
        cout << "Example: " << endl;
        cout << argv[0] << " -algo mlp -src /Volumes/Data/Dataset/JAFFE/output/sift_features.yml -dest /Volumes/Data/Dataset/JAFFE/output" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string algorithm_name;
    string input_file;
    string output_folder;

    int i;
    for(i = 1; i < argc ; i++)
    {
        if( strcmp(argv[i], "-algorithm") == 0 ){
            if(i + 1 >= argc) return -1;
            algorithm_name = argv[i + 1];
        }

        if( strcmp(argv[i], "-src") == 0 ){
            if(i + 1 >= argc) return -1;
            input_file = argv[i + 1];
        }

        if( strcmp(argv[i], "-dest") == 0 ){
            if(i + 1 >= argc) return -1;
            output_folder = argv[i + 1];
        }
    }

    cout << algorithm_name << " : " << input_file << " : " << output_folder <<  endl;


    FileStorage in( input_file, FileStorage::READ);
    int num_of_image = 0;
    in["num_of_image"] >> num_of_image;
    Mat feature;
    string path;
    int label;
    vector<Mat> features;
    vector<int> labels;
    vector<string> paths;

    for(int i = 0 ; i < num_of_image; i ++){
        in["image_feature_" + to_string(i)] >> feature;
        in["image_label_" + to_string(i)] >> label;
        in["image_path_" + to_string(i)] >> path;

        features.push_back(feature);
        paths.push_back(path);
        labels.push_back(label);
    }

    if( algorithm_name == "mlp" ){
        mlp(features, labels, paths);
    }


    waitKey(0);
    return 0;
}

void mlp(vector<Mat> features, vector<int> labels, vector<string> paths){
    cout << "Training MLP" << endl;
    imshow("features", features[100]);
    imshow("img", imread(paths[100], CV_LOAD_IMAGE_ANYCOLOR));
    cout << "Label: " << labels[100];

}