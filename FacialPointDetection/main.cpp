#include <iostream>
using namespace std;

int main() {
    cout << "Facial Point Detection Projects" << endl;

    // Read Dataset

    // =========================================
    // Training
    const int num_of_training = 10;
    const string train_data = "Datasets/COFW/trainingImage/";
    // -------------- READ IMAGE ---------------
    string img_name = "";
    for(int i = 0; i < num_of_training; i++){
        img_name = train_data + to_string(i+1) + ".jpg";
        cout << "train_img: " << img_name << endl;
    }

    cout << "Start Training" << endl;

    // =========================================
    // Testing
    cout << "Start Testing" << endl;

    return 0;
}