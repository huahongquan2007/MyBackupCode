#include "Configuration.h"

#include <fstream>
#include <iomanip>

Configuration::Configuration(const char *path) :
        numOfTraining(0),
        numOfTesting(0),
        numOfLandmark(0)
{
    parse(path);
}

void Configuration::parse(const char *path) {
    cout << "Parse Configuration" << endl;

    ifstream input(path, ifstream::in);

    string id, eq, value;
    while( input >> id >> eq >> value ){
        add(id, value);
    }
    cout << endl;
}

void Configuration::add(string id, string val) {

    if(id == "ROOT"){
        setRootPath(val);
        cout << "ROOT:" << endl; cout << "\t" << rootPath << endl;
    }

    if(id == "DATASET"){
        setDatasetPath( getRootPath() + val );
        cout << "DATASET: " << endl; cout << "\t" << datasetPath << endl;
    }

    if(id == "DATASET_TRAIN"){
        setDatasetTrainPath ( datasetPath + val );
        cout << "DATASET_TRAIN: " << endl; cout << "\t" << datasetTrainPath << endl;
    }

    if(id == "TRAIN_BOUNDINGBOX"){
        setTrainBoundingBoxPath ( datasetPath + val );
        cout << "TRAIN_BOUNDINGBOX: " << endl; cout << "\t" << trainBoundingBoxPath << endl;
    }

    if(id == "TRAIN_KEYPOINTS"){
        setTrainKeypointsPath ( datasetPath + val );
        cout << "TRAIN_KEYPOINTS: " << endl; cout << "\t" << trainKeypointsPath << endl;
    }

    if(id == "DATASET_TEST"){
        setDatasetTestPath ( datasetPath + val );
        cout << "DATASET_TEST: " << endl; cout << "\t" << datasetTestPath << endl;
    }

    if(id == "TEST_BOUNDINGBOX"){
        setTestBoundingBoxPath ( datasetPath + val );
        cout << "TEST_BOUNDINGBOX: " << endl; cout << "\t" << testBoundingBoxPath << endl;
    }

    if(id == "TEST_KEYPOINTS"){
        setTestKeypointsPath ( datasetPath + val );
        cout << "TEST_KEYPOINTS: " << endl; cout << "\t" << testKeypointsPath << endl;
    }

    if(id == "TRAIN_NUM_OF_IMAGE"){
        setNumOfTraining( stoi(val) );
        cout << "TRAIN_NUM_OF_IMAGE: " << endl; cout << "\t" << getNumOfTraining() << endl;
    }

    if(id == "TEST_NUM_OF_IMAGE"){
        setNumOfTesting( stoi(val) );
        cout << "TEST_NUM_OF_IMAGE: " << endl; cout << "\t" << getNumOfTesting() << endl;
    }

    if(id == "NUM_OF_LANDMARK"){
        setNumOfLandmark( stoi(val) );
        cout << "NUM_OF_LANDMARK: " << endl; cout << "\t" << getNumOfLandmark() << endl;
    }
}