#include <iostream>

using namespace std;

int main(int argc, char* argv[]) {
    cout << "============= FACIAL COMPONENTS =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -src <input_folder> -dest <output_folder>" << endl;
        return 1;
    }

    return 0;
}