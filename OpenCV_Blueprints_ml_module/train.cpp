#include <iostream>
#include <fstream>
#include <dirent.h>
#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/nonfree/features2d.hpp>
using namespace cv;
using namespace std;

int main(int argc, char* argv[]) {
    cout << "============= TRAIN =============" << endl;

    if(argc != 17){
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

    Mat feature;
    FileStorage in( input_file, FileStorage::READ);
    int num_of_image = 0;
    in["num_of_image"] >> num_of_image;
    in["image_feature_0"] >> feature;
    string path;
    in["image_path_0"] >> path;
    Mat img = imread( path, CV_LOAD_IMAGE_ANYCOLOR);
    imshow("test", img);
    imshow("test_feature", feature);

    waitKey(0);
    return 0;
}