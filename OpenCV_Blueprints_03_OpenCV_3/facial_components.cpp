#include <iostream>
#include <string>
#include <dirent.h>

#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/objdetect.hpp>

#include "flandmark_detector.h"

using namespace std;
using namespace cv;

void processJAFFE(string input, string output);
vector<string> listFile(string folder);
CascadeClassifier loadCascade(string cascadePath);

int main(int argc, char* argv[]) {
    cout << "============= FACIAL COMPONENTS =============" << endl;

    if(argc != 5){
        cout << "Usage: " << endl;
        cout << argv[0] << " -src <input_folder> -dest <output_folder>" << endl;
        return 1;
    }

    // ********************
    // Get input parameters
    // ********************

    string input;
    string output_folder;

    for(int i = 0; i < argc ; i++)
    {
        if( strcmp(argv[i], "-src") == 0 ){
            if(i + 1 >= argc) return -1;
            input = argv[i + 1];
        }

        if( strcmp(argv[i], "-dest") == 0 ){
            if(i + 1 >= argc) return -1;
            output_folder = argv[i + 1];
        }
    }

    // ********************
    // JAFFE Dataset
    // ********************
    processJAFFE(input, output_folder);

    return 0;
}

void processJAFFE(string input, string output) {
    cout << "Process JAFFE: " << input << endl;

    vector<string> imgPath = listFile(input);

    int num_of_image = imgPath.size();

    int EYE_IMG_WIDTH = 100;
    int EYE_IMG_HEIGHT = 100;
    int MOUTH_IMG_WIDTH = 60;
    int MOUTH_IMG_HEIGHT = 40;
    int FACE_IMG_WIDTH = 160;
    int FACE_IMG_HEIGHT = 160;

    //  num_of_image = 100;
    FileStorage fs( output + "/list.yml" , FileStorage::WRITE);
    fs << "num_of_image" << num_of_image;

    // ------ load cascade files ----
    CascadeClassifier face_cascade = loadCascade("haarcascade_frontalface_alt.xml");
    if(face_cascade.empty()) return;

    FLANDMARK_Model * model = flandmark_init("flandmark_model.dat");
    int num_of_landmark = model->data.options.M;
    double *points = new double[2 * num_of_landmark];

    for(int img_id = 0 ; img_id < num_of_image; img_id++) {
        Mat img, img_gray;
        // load image
        img = imread(imgPath[img_id], CV_LOAD_IMAGE_COLOR);
        cvtColor(img, img_gray, CV_RGB2GRAY);
        equalizeHist(img_gray, img_gray);

        vector<Rect> faces;
        face_cascade.detectMultiScale( img_gray, faces, 1.1, 3);

        for(int i = 0 ; i < 1 ; i++){
//        for(int i = 0 ; i < faces.size() ; i++){
            int bbox[4] = { faces[i].x, faces[i].y, faces[i].x + faces[i].width, faces[i].y + faces[i].height };
            flandmark_detect(new IplImage(img_gray), bbox, model, points);

//            // draw landmarks
//            for(int j = 0 ; j < num_of_landmark; j++){
//                Point centerLeft = Point((int)points[2 * j], (int)points[2* j + 1]);
//                circle(img, centerLeft, 4, Scalar(255, 255, 255), -1);
//                circle(img, centerLeft, 2, Scalar(0, 0, 255), -1);
//            }

            // left eye
            Point centerLeft = Point( (int) (points[2 * 6] + points[2 * 2]) / 2, (int) (points[2 * 6 + 1] + points[2 * 2 + 1]) / 2 );
            int widthLeft = abs(points[2 * 6] - points[2 * 2]);
            int heightLeft = widthLeft;
            Mat eyeLeft = img(Rect( centerLeft.x - widthLeft / 2, centerLeft.y - heightLeft / 2, widthLeft, heightLeft));

            // right eye
            Point centerRight = Point( (int) (points[2 * 1] + points[2 * 5]) / 2, (int) (points[2 * 1 + 1] + points[2 * 5 + 1]) / 2 );
            int widthRight = abs(points[2 * 1] - points[2 * 5]);
            int heightRight = widthRight;
            Mat eyeRight = img(Rect( centerRight.x - widthRight / 2, centerRight.y - heightRight / 2, widthRight, heightRight));

            // mouth
            int widthMouth = abs(points[2 * 4] - points[ 2 * 3]);
            int heightMouth = widthMouth ;
            widthMouth = widthMouth * 1.5;
            Point centerMouth = Point( (int) (points[2 * 4] + points[2 * 3]) / 2, (int) (points[2 * 3 + 1] + points[2 * 4 + 1]) / 2 );
            Mat mouth = img(Rect( centerMouth.x - widthMouth / 2, centerMouth.y - heightMouth / 2, widthMouth, heightMouth ));

            // face
            int widthFace = (centerLeft.x + widthLeft) - (centerRight.x - widthRight);
            int heightFace = widthFace * 1.2;
            Mat face = img(Rect( centerRight.x - widthFace/4  , centerRight.y - heightFace/4, widthFace, heightFace ));

            imshow("img", img);
            imshow("img_gray", img_gray);
            imshow("mouth", mouth);
            imshow("eyeLeft", eyeLeft);
            imshow("eyeRight", eyeRight);
            //
            // extract label
            cout << "ImagePath: " << imgPath[img_id] << endl;
            string fileName = imgPath[img_id].substr(input.length() + 1, imgPath[i].length());
            string ex_code = fileName.substr(3, 2);

            // save image
            string curFileName = fileName;
            curFileName.replace(fileName.length() - 4, 4, "eyeLeft.tiff");
            resize(eyeLeft, eyeLeft, Size(EYE_IMG_WIDTH, EYE_IMG_HEIGHT));
            imwrite( output + "/" + curFileName, eyeLeft);
            fs << "img_" + to_string(img_id) + "_eyeLeft" << output + "/" + curFileName;

            curFileName = fileName;
            curFileName.replace(fileName.length() - 4, 4, "eyeRight.tiff");
            resize(eyeRight, eyeRight, Size(EYE_IMG_WIDTH, EYE_IMG_HEIGHT));
            imwrite( output + "/" + curFileName, eyeRight);
            fs << "img_" + to_string(img_id) + "_eyeRight" << output + "/" + curFileName;

            curFileName = fileName;
            curFileName.replace(fileName.length() - 4, 4, "mouth.tiff");
            resize(mouth, mouth, Size(MOUTH_IMG_WIDTH, MOUTH_IMG_HEIGHT));
            imwrite( output + "/" + curFileName, mouth);
            fs << "img_" + to_string(img_id) + "_mouth" << output + "/" + curFileName;

            curFileName = fileName;
            curFileName.replace(fileName.length() - 4, 4, "face.tiff");
//            Mat face = img(faces[i]);
            resize(face, face, Size(FACE_IMG_WIDTH, FACE_IMG_HEIGHT));
            imwrite( output + "/" + curFileName, face);
            fs << "img_" + to_string(img_id) + "_face" << output + "/" + curFileName;

        }
    }

}

CascadeClassifier loadCascade(string cascadePath){
    CascadeClassifier cascade;
    if (!cascade.load(cascadePath)) {
        cout << "--(!)Error loading cascade: " << cascadePath << endl;
    };
    return cascade;
}
vector<string> listFile(string folder){
    vector<string> imgPath;
    DIR *pDIR;
    struct dirent *entry;
    if( pDIR=opendir(folder.c_str()) ){
        while(entry = readdir(pDIR)){
            if (entry->d_type == DT_REG){		// if entry is a regular file
                std::string fname = entry->d_name;	// filename
                std::string::size_type size = fname.find(".tiff");
                if(size != std::string::npos){
                    imgPath.push_back(folder + "/" + fname);
                }
            }
        }
    }
    return imgPath;
}

//{
//Mat eyebrows;
//
////            Mat belowEye = img(Rect( center.x - width / 2, center.y + height / 2, width / 2, height / 2 ));
//cout << "eye width" <<  width << endl;
//Mat belowEye = img(Rect( center.x - 10, center.y - 10, 20, 20 ));
//int avg = mean(belowEye)[0];
//
//Mat thresholdImg;
//threshold(img_gray, thresholdImg, avg, 255, THRESH_BINARY_INV );
//int morph_size = 2;
//Mat element = getStructuringElement( MORPH_ELLIPSE, Size( 2*morph_size + 1, 2*morph_size+1 ), Point( morph_size, morph_size ) );
//morphologyEx(thresholdImg, thresholdImg, CV_MOP_CLOSE, element);
//
//// extract rectangle above eye
//Mat aboveEye = thresholdImg(Rect(points[2 * 5], points[2 * 5 + 1] - height * 1.7, points[2 * 6] - points[2 * 5], height));
//Mat sumColumn(aboveEye.rows, 1, CV_32FC1);
//aboveEye.convertTo(aboveEye, CV_32FC1);
//reduce(aboveEye, sumColumn, 1, CV_REDUCE_SUM, CV_32FC1);
//
//double minVal, maxVal;
//Point pMin, pMax;
//minMaxLoc(sumColumn, &minVal, &maxVal, &pMin, &pMax);
//
//cout << pMax.x << " " << pMax.y << endl;
//
//int browsWidth = points[2 * 6] - points[2 * 5];
//int browsHeight = height;
//Point centerBrows = Point( (points[2 * 6] + points[2 * 5]) / 2, points[2 * 5 + 1] - height * 1.3 + pMax.y);
//
//eyebrows = img(Rect( centerBrows.x - browsWidth / 2, centerBrows.y - browsHeight / 2 , browsWidth, browsHeight ));
//
//// draw histogram
//Mat hist(255 * aboveEye.cols, aboveEye.rows, CV_32FC1, 255);
//for(int h = 0; h < aboveEye.rows; h++){
//float value = sumColumn.at<float>(h);
//cout << value << " ";
//line(hist, Point(h, hist.rows), Point(h, 255 * aboveEye.cols - value), Scalar(0,0,0));
//}
//line(hist, Point(pMax.y, hist.rows), Point(pMax.y, 0), Scalar(255,0,0));
//resize(hist, hist, Size(100, 255));
//}