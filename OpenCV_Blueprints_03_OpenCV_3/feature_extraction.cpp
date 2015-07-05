#include <iostream>
#include "opencv2/xfeatures2d.hpp"

using namespace std;
using namespace cv;

int main(int argc, char* argv[]) {
    cout << "============= FEATURE EXTRACTION =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -feature <feature_name> -src <input_folder> -dest <output_folder>" << endl;
        return 1;
    }

    Ptr<Feature2D> f2d = xfeatures2d::SIFT::create();

    return 0;
}