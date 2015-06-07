#include <iostream>
#include <fstream>
#include <dirent.h>

using namespace std;

void listFile(){
    ifstream inn;
    string   str;
    DIR *pDIR;
    struct dirent *entry;
    if( pDIR=opendir("/Users/quanhua92/Downloads") ){
        while(entry = readdir(pDIR)){
            if (entry->d_type == DT_DIR){				// if entry is a directory
                std::string fname = entry->d_name;
                cout << "FOLDER: " << fname << endl;
            }
            else if (entry->d_type == DT_REG){		// if entry is a regular file
                std::string fname = entry->d_name;	// filename
                cout << "FILE: " << fname << endl;
            }
        }
    }
}
void processJAFFE(string input, string output);
void processKAGGLE(string input, string output);
int main(int argc, char* argv[]) {
    cout << "============= PREPARE DATASET =============" << endl;

    if(argc != 7){
        cout << "Usage: " << endl;
        cout << argv[0] << " -d <dataset_name> -i <input> -o <output_folder>" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string dataset_name;
    string input;
    string output_folder;
    int i;
    for(i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-d") == 0 ){
            if(i + 1 >= argc) return -1;
            dataset_name = argv[i + 1];
        }

        if( strcmp(argv[i], "-i") == 0 ){
            if(i + 1 >= argc) return -1;
            input = argv[i + 1];
        }

        if( strcmp(argv[i], "-o") == 0 ){
            if(i + 1 >= argc) return -1;
            output_folder = argv[i + 1];
        }
    }

    // ********************
    // JAFFE Dataset
    // ********************
    if(dataset_name == "jaffe"){
        processJAFFE(input, output_folder);
    }

    // ********************
    // KAGGLE Dataset
    // ********************
    if(dataset_name == "kaggle"){
        processKAGGLE(input, output_folder);
    }

    return 0;
}

void processJAFFE(string input, string output){
    cout << "Process JAFFE: " << input << endl;


    cout << "Done. Output: " << output << endl;
}

void processKAGGLE(string input, string output){
    cout << "Hello KAGGLE" << endl;
    cout << "Done. Output : " << output << endl;
}