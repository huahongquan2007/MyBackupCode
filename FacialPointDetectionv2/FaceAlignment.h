#ifndef FACE_ALIGNMENT_H
#define FACE_ALIGNMENT_H

#include <iostream>
#include <cstdio>
#include <cstdlib>
#include "cv.h"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <ctime>
#include <string>
#include <limits>
#include <algorithm>
#include <cmath>
#include <vector>
#include <fstream>
#include <numeric>   
#include <utility> 

class BoundingBox{
    public:
        double start_x;
        double start_y;
        double width;
        double height;
        double centroid_x;
        double centroid_y;
        BoundingBox(){
            start_x = 0;
            start_y = 0;
            width = 0;
            height = 0;
            centroid_x = 0;
            centroid_y = 0;
        }; 
};


class FernRegressor {
    private:
        int fern_pixel_num_;
        int landmark_num_;
        cv::Mat_<int> selected_nearest_landmark_index_;
        cv::Mat_<double> threshold_;
        cv::Mat_<int> selected_pixel_index_;
        cv::Mat_<double> selected_pixel_locations_;
        std::vector<cv::Mat_<double> > bin_output_;
    public:
        std::vector<cv::Mat_<double> > Train(const std::vector<std::vector<double> >& candidate_pixel_intensity, 
                                             const cv::Mat_<double>& covariance,
                                             const cv::Mat_<double>& candidate_pixel_locations,
                                             const cv::Mat_<int>& nearest_landmark_index,
                                             const std::vector<cv::Mat_<double> >& regression_targets,
                                             int fern_pixel_num);
        cv::Mat_<double> Predict(const cv::Mat_<uchar>& image,
                                 const cv::Mat_<double>& shape,
                                 const cv::Mat_<double>& rotation,
                                 const BoundingBox& bounding_box,
                                 double scale);
        void Read(std::ifstream& fin);
        void Write(std::ofstream& fout);
};

class NormalRegressor {
    public:
        std::vector<cv::Mat_<double> > Train(const std::vector<cv::Mat_<uchar> >& images,
                                             const std::vector<cv::Mat_<double> >& current_shapes,
                                             const std::vector<cv::Mat_<double> >& ground_truth_shapes,
                                             const std::vector<BoundingBox> & bounding_box,
                                             const cv::Mat_<double>& mean_shape,
                                             int second_level_num,
                                             int candidate_pixel_num,
                                             int fern_pixel_num,
                                             int curr_level_num,
                                             int first_level_num);  
        cv::Mat_<double> Predict(const cv::Mat_<uchar>& image, 
                                 const BoundingBox& bounding_box, 
                                 const cv::Mat_<double>& mean_shape,
                                 const cv::Mat_<double>& shape);
        void Read(std::ifstream& fin);
        void Write(std::ofstream& fout);
    private:
        std::vector<FernRegressor> ferns_;
        int second_level_num_;
};

class ShapeAlignment {
    public:
        ShapeAlignment();
        void Train(const std::vector<cv::Mat_<uchar> >& images, 
                   const std::vector<cv::Mat_<double> >& ground_truth_shapes,
                   const std::vector<BoundingBox>& bounding_box,
                   int first_level_num, int second_level_num,
                   int candidate_pixel_num, int fern_pixel_num,
                   int initial_num);
        cv::Mat_<double> Predict(const cv::Mat_<uchar>& image, const BoundingBox& bounding_box, int initial_num);
        void Read(std::ifstream& fin);
        void Write(std::ofstream& fout);
        void Load(std::string path);
        void Save(std::string path);
    private:
        int first_level_num_;
        int landmark_num_;
        std::vector<NormalRegressor> fern_cascades_;
        cv::Mat_<double> mean_shape_;
        std::vector<cv::Mat_<double> > training_shapes_;
        std::vector<BoundingBox> bounding_box_;
};

cv::Mat_<double> GetMeanShape(const std::vector<cv::Mat_<double> >& shapes,
                              const std::vector<BoundingBox>& bounding_box);
cv::Mat_<double> ProjectShape(const cv::Mat_<double>& shape, const BoundingBox& bounding_box);
cv::Mat_<double> ReProjectShape(const cv::Mat_<double>& shape, const BoundingBox& bounding_box);
void SimilarityTransform(const cv::Mat_<double>& shape1, const cv::Mat_<double>& shape2, 
                         cv::Mat_<double>& rotation,double& scale);
double calculate_covariance(const std::vector<double>& v_1, 
                            const std::vector<double>& v_2);
void visualizeImage(cv::Mat img, cv::Mat_<double> keypoints, int delay = 0, bool debug=false, std::string win_name="Images");

#endif
