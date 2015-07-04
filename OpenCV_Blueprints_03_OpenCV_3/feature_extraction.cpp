#include <iostream>

using namespace std;

int main(int argc, char* argv[]) {
    cout << "============= FEATURE EXTRACTION =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -feature <feature_name> -src <input_folder> -dest <output_folder>" << endl;
        return 1;
    }

    return 0;
}