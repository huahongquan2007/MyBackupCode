#include "FaceAlignment.h"
using namespace std;
using namespace cv;

vector<Mat_<double> > FernRegressor::Train(const vector<vector<double> >& candidate_pixel_intensity,
                                  const Mat_<double>& covariance,
                                  const Mat_<double>& candidate_pixel_locations,
                                  const Mat_<int>& nearest_landmark_index,
                                  const vector<Mat_<double> >& regression_targets,
                                  int fern_pixel_num){
    // selected_pixel_index_: fern_pixel_num*2 matrix, the index of selected pixels pairs in fern
    // selected_pixel_locations_: fern_pixel_num*4 matrix, the locations of selected pixel pairs
    //                            stored in the format (x_1,y_1,x_2,y_2) for each row 
    fern_pixel_num_ = fern_pixel_num;
    landmark_num_ = regression_targets[0].rows;
    selected_pixel_index_.create(fern_pixel_num,2);
    selected_pixel_locations_.create(fern_pixel_num,4);
    selected_nearest_landmark_index_.create(fern_pixel_num,2);
    int candidate_pixel_num = candidate_pixel_locations.rows;

    // select pixel pairs from candidate pixels, this selection is based on the correlation between pixel 
    // densities and regression targets
    // for details, please refer to "Face Alignment by Explicit Shape Regression" 
    // threshold_: thresholds for each pair of pixels in fern 
    
    threshold_.create(fern_pixel_num,1);
    // get a random direction
//    cout << "get a random direction" << endl;
//    RNG random_generator(getTickCount());
//    for(int i = 0;i < fern_pixel_num;i++){
//        // RNG random_generator(i);
//        Mat_<double> random_direction(landmark_num_ ,2);
//        random_generator.fill(random_direction,RNG::UNIFORM,-1.1,1.1);
//
//        normalize(random_direction,random_direction);
//        vector<double> projection_result(regression_targets.size(), 0);
//        // project regression targets along the random direction
//        for(int j = 0;j < regression_targets.size();j++){
//            double temp = 0;
//            temp = sum(regression_targets[j].mul(random_direction))[0];
//            projection_result[j] = temp;
//        }
//        Mat_<double> covariance_projection_density(candidate_pixel_num,1);
//        for(int j = 0;j < candidate_pixel_num;j++){
//            covariance_projection_density(j) = calculate_covariance(projection_result,candidate_pixel_intensity[j]);
//        }
//
//
//        // find max correlation
//        double max_correlation = -1;
//        int max_pixel_index_1 = 0;
//        int max_pixel_index_2 = 0;
//        for(int j = 0;j < candidate_pixel_num;j++){
//            for(int k = 0;k < candidate_pixel_num;k++){
//                double temp1 = covariance(j,j) + covariance(k,k) - 2*covariance(j,k);
//                if(abs(temp1) < 1e-10){
//                    continue;
//                }
//                bool flag = false;
//                for(int p = 0;p < i;p++){
//                    if(j == selected_pixel_index_(p,0) && k == selected_pixel_index_(p,1)){
//                        flag = true;
//                        break;
//                    }else if(j == selected_pixel_index_(p,1) && k == selected_pixel_index_(p,0)){
//                        flag = true;
//                        break;
//                    }
//                }
//                if(flag){
//                    continue;
//                }
//                double temp = (covariance_projection_density(j) - covariance_projection_density(k))
//                    / sqrt(temp1);
//                if(abs(temp) > max_correlation){
//                    max_correlation = temp;
//                    max_pixel_index_1 = j;
//                    max_pixel_index_2 = k;
//                }
//            }
//        }
//
//        selected_pixel_index_(i,0) = max_pixel_index_1;
//        selected_pixel_index_(i,1) = max_pixel_index_2;
//        selected_pixel_locations_(i,0) = candidate_pixel_locations(max_pixel_index_1,0);
//        selected_pixel_locations_(i,1) = candidate_pixel_locations(max_pixel_index_1,1);
//        selected_pixel_locations_(i,2) = candidate_pixel_locations(max_pixel_index_2,0);
//        selected_pixel_locations_(i,3) = candidate_pixel_locations(max_pixel_index_2,1);
//        selected_nearest_landmark_index_(i,0) = nearest_landmark_index(max_pixel_index_1);
//        selected_nearest_landmark_index_(i,1) = nearest_landmark_index(max_pixel_index_2);
//
//        // get threshold for this pair
//        double max_diff = -1;
//        for(int j = 0;j < candidate_pixel_intensity[max_pixel_index_1].size();j++){
//            double temp = candidate_pixel_intensity[max_pixel_index_1][j] - candidate_pixel_intensity[max_pixel_index_2][j];
//            if(abs(temp) > max_diff){
//                max_diff = abs(temp);
//            }
//        }
//
//        cout << "Max: " << max_diff << endl;
//        threshold_(i) = random_generator.uniform(-0.2*max_diff,0.2*max_diff);
//    }
    RNG rng(getTickCount());
    for (int i = 0; i < fern_pixel_num; i++) {
        // create a random direction to project target -> scalar
        Mat_<double> random_direction(landmark_num_, 2);
        rng.fill(random_direction, RNG::UNIFORM, -1.1, 1.1);
        normalize(random_direction, random_direction);

        // project target -> scalar
        int num_of_images = regression_targets.size();
        Mat_<double> projected_target(1, num_of_images);
        for (int j = 0; j < num_of_images; j++) {
            projected_target.at<double>(0, j) = sum(regression_targets[j].mul(random_direction))[0];
        }

        // calculate cov(i, y)
        Mat_<double> covariance_i_y(candidate_pixel_num, 1);

        for (int j = 0; j < candidate_pixel_num; j++) {
            covariance_i_y.at<double>(j, 0) = calculate_covariance(candidate_pixel_intensity[j], projected_target);
        }
        // calculate var(y)
        double var_y = calculate_covariance(projected_target, projected_target);

        // among P2 features, select a feature with highest correlation to the scalar
        double max_correlation = 0;
        int index_i = 0;
        int index_j = 0;
        for (int fi = 0; fi < candidate_pixel_num; fi++) {
            for (int fj = fi; fj < candidate_pixel_num; fj++) {

                double temp1 = covariance.at<double>(fi, fi) + covariance.at<double>(fj, fj) - 2 * covariance.at<double>(fi, fj);
                if(abs(temp1) < 1e-10){
                    continue;
                }
                bool isSelected = false;
                for(int p = 0;p < fi;p++){
                    if(fi == selected_pixel_index_.at<int>(p,0) && fj == selected_pixel_index_.at<int>(p,1)){
                        isSelected = true;
                        break;
                    }else if(fi == selected_pixel_index_.at<int>(p,1) && fj == selected_pixel_index_.at<int>(p,0)){
                        isSelected = true;
                        break;
                    }
                }
                if(isSelected){
                    continue;
                }
                double temp = (covariance_i_y.at<double>(fi, 0) - covariance_i_y.at<double>(fj, 0))
                              / sqrt(temp1);
                if(abs(temp) > max_correlation){
                    max_correlation = temp;
                    index_i = fi;
                    index_j = fj;
                }
            }
        }
        selected_pixel_index_.at<int>(i, 0) = index_i;
        selected_pixel_index_.at<int>(i, 1) = index_j;

        double max = -1;
        for(int j = 0;j < candidate_pixel_intensity[index_i].size() ;j++){
            // double temp = candidate_pixel_intensity[max_pixel_index_1][j] - candidate_pixel_intensity[max_pixel_index_2][j];
            double temp = candidate_pixel_intensity[index_i][j] - candidate_pixel_intensity[index_j][j];
            if(abs(temp) > max){
                max = abs(temp);
            }
        }

//        cout << "(" << i <<  "_max: " << max << ")";
        double threshold = rng.uniform( max * -0.2, max * 0.2 );

//        Mat_<double> location(2, 2, CV_32F); // x1 y1 ; x2 y2
//        location.at<double>(0, 0) = pixelLocation.at<double>(index_i, 0);
//        location.at<double>(0, 1) = pixelLocation.at<double>(index_i, 1);
//        location.at<double>(1, 0) = pixelLocation.at<double>(index_j, 0);
//        location.at<double>(1, 1) = pixelLocation.at<double>(index_j, 1);
//
        selected_pixel_locations_(i,0) = candidate_pixel_locations(index_i,0);
        selected_pixel_locations_(i,1) = candidate_pixel_locations(index_i,1);
        selected_pixel_locations_(i,2) = candidate_pixel_locations(index_j,0);
        selected_pixel_locations_(i,3) = candidate_pixel_locations(index_j,1);
        selected_nearest_landmark_index_(i,0) = nearest_landmark_index(index_i);
        selected_nearest_landmark_index_(i,1) = nearest_landmark_index(index_j);

        threshold_(i) = threshold;

//        Mat_<int> nearestLandmark(2, 1, CV_32F);
//        nearestLandmark.at<int>(0, 0) = nearestLandmarkOfPixel.at<int>(index_i, 0);
//        nearestLandmark.at<int>(1, 0) = nearestLandmarkOfPixel.at<int>(index_j, 0);

//        fernThreshold.push_back(threshold);
//        fernPairLocation.push_back(location);
//        fernPairNearestLandmark.push_back(nearestLandmark);
    }

    // determine the bins of each shape
    vector<vector<int> > shapes_in_bin;
    int bin_num = pow(2.0,fern_pixel_num);
    shapes_in_bin.resize(bin_num);
//    for(int i = 0;i < regression_targets.size();i++){
//        int index = 0;
//        for(int j = 0;j < fern_pixel_num;j++){
//            double density_1 = candidate_pixel_intensity[selected_pixel_index_(j,0)][i];
//            double density_2 = candidate_pixel_intensity[selected_pixel_index_(j,1)][i];
//            if(density_1 - density_2 >= threshold_(j)){
//                index = index + pow(2.0,j);
//            }
//        }
//        shapes_in_bin[index].push_back(i);
//    }
//
    // cluster shape into bins
//    vector< vector<int> > bins_index;
//    bins_index.resize( pow(2.0, fern_pixel_num) );

    for(int i = 0 ; i < regression_targets.size(); i++){
        int index = 0;

        for(int j = 0 ; j < fern_pixel_num; j++){
            int pixel1 = candidate_pixel_intensity[selected_pixel_index_(j, 0)][i];
            int pixel2 = candidate_pixel_intensity[selected_pixel_index_(j, 1)][i];

            if(pixel1 - pixel2 >= threshold_(j)){
                index += pow(2.0, j);
            }
        }
        shapes_in_bin[index].push_back(i);
    }


    // get bin output
//    cout << "BIN: ";
    vector<Mat_<double> > prediction;
    prediction.resize(regression_targets.size());
    bin_output_.resize(bin_num);
//    for(int i = 0;i < bin_num;i++){
//        Mat_<double> temp = Mat::zeros(landmark_num_,2, CV_64FC1);
//        int bin_size = shapes_in_bin[i].size();
//        for(int j = 0;j < bin_size;j++){
//            int index = shapes_in_bin[i][j];
//            temp = temp + regression_targets[index];
//        }
//        if(bin_size == 0){
//            bin_output_[i] = temp;
//            continue;
//        }
//        temp = (1.0/((1.0+1000.0/bin_size) * bin_size)) * temp;
//        bin_output_[i] = temp;
//        cout << "[" << i << "_"<< bin_size << "] ";
//        for(int j = 0;j < bin_size;j++){
//            int index = shapes_in_bin[i][j];
//            prediction[index] = temp;
//        }
//    }
//    cout << endl;
    // compute regression output
    // compute output for each shape in training
//    vector<Mat_<double>> deltaShape;
//    deltaShape.resize(regression_targets.size());

    for(int i = 0 ; i < bin_num ; i++){
        int bin_size = shapes_in_bin[i].size();
        Mat_<double> result = Mat::zeros (landmark_num_, 2, CV_32F);
        if(bin_size > 0){
            for(int j = 0 ; j < bin_size ; j++){
                int shape_idx = shapes_in_bin[i][j];
                result += regression_targets[shape_idx];
            }

            result = 1.0/((1.0 + 1000.0/bin_size) * bin_size) * result;
        }

        bin_output_[i] = result;

        for(int j = 0 ; j < bin_size; j++){
            int shape_idx = shapes_in_bin[i][j];
            prediction[shape_idx] = bin_output_[i];
        }
    }
    return prediction;
}


void FernRegressor::Write(ofstream& fout){
    fout<<fern_pixel_num_<<endl;
    fout<<landmark_num_<<endl;
    for(int i = 0;i < fern_pixel_num_;i++){
        fout<<selected_pixel_locations_(i,0)<<" "<<selected_pixel_locations_(i,1)<<" "
            <<selected_pixel_locations_(i,2)<<" "<<selected_pixel_locations_(i,3)<<" "<<endl;
        fout<<selected_nearest_landmark_index_(i,0)<<endl;
        fout<<selected_nearest_landmark_index_(i,1)<<endl;
        fout<<threshold_(i)<<endl;
    }
    for(int i = 0;i < bin_output_.size();i++){
        for(int j = 0;j < bin_output_[i].rows;j++){
            fout<<bin_output_[i](j,0)<<" "<<bin_output_[i](j,1)<<" ";
        }
        fout<<endl;
    } 

}

void FernRegressor::Read(ifstream& fin){
    fin>>fern_pixel_num_;
    fin>>landmark_num_;
    selected_nearest_landmark_index_.create(fern_pixel_num_,2);
    selected_pixel_locations_.create(fern_pixel_num_,4);
    threshold_.create(fern_pixel_num_,1);
    for(int i = 0;i < fern_pixel_num_;i++){
        fin>>selected_pixel_locations_(i,0)>>selected_pixel_locations_(i,1)
            >>selected_pixel_locations_(i,2)>>selected_pixel_locations_(i,3);
        fin>>selected_nearest_landmark_index_(i,0)>>selected_nearest_landmark_index_(i,1);
        fin>>threshold_(i);
    }       
     
    int bin_num = pow(2.0,fern_pixel_num_);
    for(int i = 0;i < bin_num;i++){
        Mat_<double> temp(landmark_num_,2);
        for(int j = 0;j < landmark_num_;j++){
            fin>>temp(j,0)>>temp(j,1);
        }
        bin_output_.push_back(temp);
    }
}

Mat_<double> FernRegressor::Predict(const Mat_<uchar>& image,
                     const Mat_<double>& shape,
                     const Mat_<double>& rotation,
                     const BoundingBox& bounding_box,
                     double scale){
    int index = 0;
    for(int i = 0;i < fern_pixel_num_;i++){
        int nearest_landmark_index_1 = selected_nearest_landmark_index_(i,0);
        int nearest_landmark_index_2 = selected_nearest_landmark_index_(i,1);
        double x = selected_pixel_locations_(i,0);
        double y = selected_pixel_locations_(i,1);
        double project_x = scale * (rotation(0,0)*x + rotation(0,1)*y) * bounding_box.width/2.0 + shape(nearest_landmark_index_1,0);
        double project_y = scale * (rotation(1,0)*x + rotation(1,1)*y) * bounding_box.height/2.0 + shape(nearest_landmark_index_1,1);

        project_x = std::max(0.0,std::min((double)project_x,image.cols-1.0));
        project_y = std::max(0.0,std::min((double)project_y,image.rows-1.0)); 
        double intensity_1 = (int)(image((int)project_y,(int)project_x));

        x = selected_pixel_locations_(i,2);
        y = selected_pixel_locations_(i,3);
        project_x = scale * (rotation(0,0)*x + rotation(0,1)*y) * bounding_box.width/2.0 + shape(nearest_landmark_index_2,0);
        project_y = scale * (rotation(1,0)*x + rotation(1,1)*y) * bounding_box.height/2.0 + shape(nearest_landmark_index_2,1);
        project_x = std::max(0.0,std::min((double)project_x,image.cols-1.0));
        project_y = std::max(0.0,std::min((double)project_y,image.rows-1.0));
        double intensity_2 = (int)(image((int)project_y,(int)project_x));

        if(intensity_1 - intensity_2 >= threshold_(i)){
            index = index + (int)(pow(2,i));
        }
    }
    return bin_output_[index];
   
}

