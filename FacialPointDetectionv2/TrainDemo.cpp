/*
Author: Bi Sai 
Date: 2014/06/18
This program is a reimplementation of algorithms in "Face Alignment by Explicit 
Shape Regression" by Cao et al.
If you find any bugs, please email me: soundsilencebisai-at-gmail-dot-com

Copyright (c) 2014 Bi Sai 
The MIT License (MIT)
Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

#include "FaceAlignment.h"
#include <string>
using namespace std;
using namespace cv;

int main(){
    int img_num = 1345;
    int candidate_pixel_num = 400;
    int fern_pixel_num = 5;
    int first_level_num = 10;
    int second_level_num = 50;
    int landmark_num = 29;
    int initial_number = 20;
    vector<Mat_<uchar> > images;
    vector<BoundingBox> bbox; 
    
    cout<<"Read images..."<<endl;
    for(int i = 0;i < img_num;i++){
        string image_name = "/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/COFW/trainingImages/";
        image_name = image_name + std::to_string(i+1) + ".jpg";
        Mat_<uchar> temp = imread(image_name,0);
        images.push_back(temp);
    }
    
    vector<Mat_<double> > ground_truth_shapes;
    vector<BoundingBox> bounding_box;
    ifstream fin;
    fin.open("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/COFW/boundingbox.txt");
    for(int i = 0;i < img_num;i++){
        BoundingBox temp;
        fin>>temp.start_x>>temp.start_y>>temp.width>>temp.height;
        temp.centroid_x = temp.start_x + temp.width/2.0;
        temp.centroid_y = temp.start_y + temp.height/2.0; 
        bounding_box.push_back(temp);
    }
    fin.close();

    fin.open("/home/robotbase/github/MyBackupCode/FacialPointDetection/Datasets/COFW/keypoints.txt");
    for(int i = 0;i < img_num;i++){
        Mat_<double> temp(landmark_num,2);
        for(int j = 0;j < landmark_num;j++){
            fin>>temp(j,0); 
        }
        for(int j = 0;j < landmark_num;j++){
            fin>>temp(j,1); 
        }
        ground_truth_shapes.push_back(temp);
    }

    fin.close(); 
    
    ShapeAlignment regressor;
    regressor.Train(images,ground_truth_shapes,bounding_box,first_level_num,second_level_num,
                    candidate_pixel_num,fern_pixel_num,initial_number);
    regressor.Save("/home/robotbase/github/test/FaceAlignment/newFaceAlignment/model.txt");

    waitKey(0);
    return 0;
}

