//
// Created by robotbase on 17/03/2015.
//

#ifndef _FACIALPOINTDETECTION_CONFIGURATION_H_
#define _FACIALPOINTDETECTION_CONFIGURATION_H_

#include <iostream>
using namespace std;

class Configuration {

public:
    Configuration(const char* path);

    void parse(const char* path);
    string getDatasetPath() const {
        return datasetPath;
    }

    void setDatasetPath(string datasetPath) {
        Configuration::datasetPath = datasetPath;
    }

    string getDatasetTrainPath() const {
        return datasetTrainPath;
    }

    void setDatasetTrainPath(string datasetTrainPath) {
        Configuration::datasetTrainPath = datasetTrainPath;
    }

    string getDatasetTestPath() const {
        return datasetTestPath;
    }

    void setDatasetTestPath(string datasetTestPath) {
        Configuration::datasetTestPath = datasetTestPath;
    }

    string getRootPath() const {
        return rootPath;
    }

    void setRootPath(string rootPath) {
        Configuration::rootPath = rootPath;
    }

    string getTrainBoundingBoxPath() const {
        return trainBoundingBoxPath;
    }

    void setTrainBoundingBoxPath(string trainBoundingBoxPath) {
        Configuration::trainBoundingBoxPath = trainBoundingBoxPath;
    }

    string getTestBoundingBoxPath() const {
        return testBoundingBoxPath;
    }

    void setTestBoundingBoxPath(string testBoundingBoxPath) {
        Configuration::testBoundingBoxPath = testBoundingBoxPath;
    }

    string getTrainKeypointsPath() const {
        return trainKeypointsPath;
    }

    void setTrainKeypointsPath(string trainKeypointsPath) {
        Configuration::trainKeypointsPath = trainKeypointsPath;
    }

    string getTestKeypointsPath() const {
        return testKeypointsPath;
    }

    void setTestKeypointsPath(string testKeypointsPath) {
        Configuration::testKeypointsPath = testKeypointsPath;
    }
    int getNumOfTraining() const {
        return numOfTraining;
    }

    void setNumOfTraining(int numOfTraining) {
        Configuration::numOfTraining = numOfTraining;
    }

    int getNumOfTesting() const {
        return numOfTesting;
    }

    void setNumOfTesting(int numOfTesting) {
        Configuration::numOfTesting = numOfTesting;
    }

    int getNumOfLandmark() const {
        return numOfLandmark;
    }

    void setNumOfLandmark(int numOfLandmark) {
        Configuration::numOfLandmark = numOfLandmark;
    }
private:
    string rootPath;
    string datasetPath;

    string datasetTrainPath;
    string datasetTestPath;

    string trainBoundingBoxPath;
    string testBoundingBoxPath;

    string trainKeypointsPath;
    string testKeypointsPath;

    int numOfTraining;

    int numOfTesting;

    int numOfLandmark;

    void add(string, string);
};


#endif //_FACIALPOINTDETECTION_CONFIGURATION_H_
