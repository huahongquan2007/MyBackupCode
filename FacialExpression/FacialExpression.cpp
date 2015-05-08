#include "FacialExpression.h"
using namespace std;
using namespace cv;

void FacialExpression::Train(std::vector<int> labels, std::vector<cv::Mat_<double>> keypoints){
    cout << "Train Facial Expression" << endl;

    int num_of_data = labels.size();
    int num_of_landmark = keypoints[0].rows;
    num_of_class = 2;
    Mat labelMat = Mat::zeros(num_of_data, num_of_class , CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        labelMat.at<float>(i, labels[i]) = (float) 1.0;
    }
    cout << labelMat.size() << endl;

    Mat trainingDataMat(num_of_data, num_of_landmark * 2, CV_32FC1);

    for(int i = 0 ; i < num_of_data; i++){
        for(int j = 0 ; j < num_of_landmark; j ++){
            trainingDataMat.at<float>(i, j) = (float) keypoints[i].at<double>(j, 0);
        }
        for(int j = 0 ; j < num_of_landmark; j ++){
            trainingDataMat.at<float>(i, j + num_of_landmark) = (float) keypoints[i].at<double>(j, 1);
        }
    }
    cout << trainingDataMat.size() << endl;
    cout << trainingDataMat.row(0) << endl;

    // ------------------------------ SVM ----------------------------
//    SVMParams params;
//    params.svm_type    = SVM::C_SVC;
//    params.kernel_type = SVM::RBF;
//    params.term_crit   = cvTermCriteria(CV_TERMCRIT_ITER, 100, 1e-6);
//
//    svm.train_auto(trainingDataMat, labelMat, Mat(), Mat(), params);
//
//    cout << "SV COUNT: " << svm.get_support_vector_count() << endl;
//
//
//    int count = 0;
//    for(int i = 0 ; i < num_of_data ; i++){
//        int predict = svm.predict(trainingDataMat.row(i));
//        cout << "PREDICT: " << predict << " TRUTH : " << labels[i] << endl;
//        if( predict == labels[i])
//            count++;
//    }

    // ------------------------------- ANN ---------------------------
    cv::Mat layers = cv::Mat(3, 1, CV_32SC1);

    layers.row(0) = cv::Scalar(136);
    layers.row(1) = cv::Scalar(100);
    layers.row(2) = cv::Scalar(num_of_class);

    CvANN_MLP_TrainParams params;
    CvTermCriteria criteria;
    criteria.max_iter = 1000;
    criteria.epsilon = 0.00001f;
    criteria.type = CV_TERMCRIT_ITER | CV_TERMCRIT_EPS;
    params.train_method = CvANN_MLP_TrainParams::BACKPROP;
    params.bp_dw_scale = 0.05f;
    params.bp_moment_scale = 0.05f;
    params.term_crit = criteria;

    cout << "Before create" << endl;

    mlp.create(layers);

    cout << "Before train" << endl;
    // train
    mlp.train(trainingDataMat, labelMat, cv::Mat(), cv::Mat(), params);

    cout << "After train" << endl;

    int count = 0;
    for(int i = 0 ; i < num_of_data ; i++){
        cv::Mat response(1, 2, CV_32FC1);
        mlp.predict(trainingDataMat.row(i), response);

        float maxPredict = response.at<float>(0, 0);
        int predict = 0;
        for(int index = 1 ; index < num_of_class; index ++){
            if( response.at<float>(0, index) > maxPredict){
                predict = index;
                maxPredict = response.at<float>(0, index);
            }
        }

        cout << "PREDICT: " << predict << " TRUTH : " << labels[i] << endl;

        if(predict == labels [i])
            count++;
    }

    cout << "RESULT: " << count << "/" << num_of_data << " Percent: " << 100 * count / num_of_data << endl;
    waitKey(0);
}

int FacialExpression::Test(cv::Mat_<double> keypoint){
    int num_of_landmark = keypoint.rows;
    Mat testDataMat(1, num_of_landmark * 2, CV_32FC1);

    for(int j = 0 ; j < num_of_landmark; j ++){
        testDataMat.at<float>(0, j) = (float) keypoint.at<double>(j, 0);
    }
    for(int j = 0 ; j < num_of_landmark; j ++){
        testDataMat.at<float>(0, j + num_of_landmark) = (float) keypoint.at<double>(j, 1);
    }

//    int predict = svm.predict(testDataMat);
    cv::Mat response(1, 2, CV_32FC1);
    mlp.predict(testDataMat, response);

    float maxPredict = response.at<float>(0, 0);
    int predict = 0;
    for(int index = 1 ; index < num_of_class; index ++){
        if( response.at<float>(0, index) > maxPredict){
            predict = index;
            maxPredict = response.at<float>(0, index);
        }
    }

//    cout << "PREDICT: " << predict << endl;

    return predict;
}

void FacialExpression::Save(std::string destination){
    cout << "Start Save" << endl;
    mlp.save(destination.c_str(), "expressionModel");
    cout << "Save successful" << endl;
}
void FacialExpression::Load(std::string destination){
    mlp.load(destination.c_str(), "expressionModel");
}