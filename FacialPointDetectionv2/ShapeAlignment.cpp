#include "FaceAlignment.h"
using namespace std;
using namespace cv;

ShapeAlignment::ShapeAlignment(){
    first_level_num_ = 0;
}

/**
 * @param images gray scale images
 * @param ground_truth_shapes a vector of N*2 matrix, where N is the number of landmarks
 * @param bounding_box BoundingBox of faces
 * @param first_level_num number of first level regressors
 * @param second_level_num number of second level regressors
 * @param candidate_pixel_num number of pixels to be selected as features
 * @param fern_pixel_num number of pixel pairs in a fern
 * @param initial_num number of initial shapes for each input image
 */
void ShapeAlignment::Train(const vector<Mat_<uchar> >& images,
                   const vector<Mat_<double> >& ground_truth_shapes,
                   const vector<BoundingBox>& bounding_box,
                   int first_level_num, int second_level_num,
                   int candidate_pixel_num, int fern_pixel_num,
                   int initial_num){
    cout<<"Start training..."<<endl;
    bounding_box_ = bounding_box;
    training_shapes_ = ground_truth_shapes;
    first_level_num_ = first_level_num;
    landmark_num_ = ground_truth_shapes[0].rows; 
    // data augmentation and multiple initialization 
    vector<Mat_<uchar> > augmented_images;
    vector<BoundingBox> augmented_bounding_box;
    vector<Mat_<double> > augmented_ground_truth_shapes;
    vector<Mat_<double> > current_shapes;
     
//    RNG random_generator(getTickCount());
//    for(int i = 0;i < images.size();i++){
//        for(int j = 0;j < initial_num;j++){
//            int index = 0;
//            do{
//                // index = (i+j+1) % (images.size());
//                index = random_generator.uniform(0, images.size());
//            }while(index == i);
//            augmented_images.push_back(images[i]);
//            augmented_ground_truth_shapes.push_back(ground_truth_shapes[i]);
//            augmented_bounding_box.push_back(bounding_box[i]);
//            // 1. Select ground truth shapes of other images as initial shapes
//            // 2. Project current shape to bounding box of ground truth shapes
//            Mat_<double> temp = ground_truth_shapes[index];
//            temp = ProjectShape(temp, bounding_box[index]);
//            temp = ReProjectShape(temp, bounding_box[i]);
//            current_shapes.push_back(temp);
//        }
//    }
    // generate more image, keypoints, curShape & inputShape
    // Use boundingBox to generate initialized locations for training data
    RNG rng;

    int total_image_original = images.size();

    for(int i = 0 ; i < images.size(); i++){
        // method 1: random
        int index = i;
        while(index == i){
            index = rng.uniform(0, images.size() - 1);
        }
        augmented_images.push_back(images[i]);
        augmented_ground_truth_shapes.push_back(ground_truth_shapes[i]);
        augmented_bounding_box.push_back(bounding_box[i]);


        // New
        current_shapes.push_back( ReProjectShape(ProjectShape(ground_truth_shapes[index], bounding_box[index]), bounding_box[i] ) );
        // Old
//        curShape.push_back( ProjectToImageCoordinate(ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]), boundingBoxes[i] ) );
    }
    for(int i = 0 ; i < total_image_original; i++){
        for(int j = 0 ; j < 19 ; j ++){

            // method 1: random
            int index = i;
            while(index == i){
                index = rng.uniform(0, total_image_original - 1);
            }


            // new
            current_shapes.push_back( ReProjectShape(ProjectShape(ground_truth_shapes[index], bounding_box[index]), bounding_box[i] ) );
            // old
            //curShape.push_back( ProjectToImageCoordinate(ProjectToBoxCoordinate(keypoints[index], boundingBoxes[index]), boundingBoxes[i] ) );

            // new
            augmented_images.push_back(images[i]);
            augmented_ground_truth_shapes.push_back(ground_truth_shapes[i]);
            augmented_bounding_box.push_back(bounding_box[i]);
            // old
//            images.push_back(images[i].clone());
//            keypoints.push_back(keypoints[i].clone());
//            boundingBoxes.push_back(boundingBoxes[i]);
        }
    }


    // get mean shape from training shapes
    mean_shape_ = GetMeanShape(ground_truth_shapes,bounding_box); 



    int visualizeIdx = 0;
    Mat_<double> initialize = current_shapes[visualizeIdx].clone();
    visualizeImage(augmented_images[visualizeIdx], initialize, 10 , false, "image_init");
    // train fern cascades
    fern_cascades_.resize(first_level_num);
    vector<Mat_<double> > prediction;
    for(int i = 0;i < first_level_num;i++){
        cout<<"Training fern cascades: "<<i+1<<" out of "<<first_level_num<< " with " << augmented_images.size() << " images" << endl;
        prediction = fern_cascades_[i].Train(augmented_images,current_shapes,
                augmented_ground_truth_shapes,augmented_bounding_box,mean_shape_,second_level_num,candidate_pixel_num,fern_pixel_num, i+1, first_level_num);
        
        // update current shapes 
        for(int j = 0;j < prediction.size();j++){
            current_shapes[j] = prediction[j] + ProjectShape(current_shapes[j], augmented_bounding_box[j]);
            current_shapes[j] = ReProjectShape(current_shapes[j],augmented_bounding_box[j]);
        }

        visualizeImage(augmented_images[visualizeIdx], current_shapes[visualizeIdx], 10 , false, "image_cur");
    } 
    
}


void ShapeAlignment::Write(ofstream& fout){
    fout<<first_level_num_<<endl;
    fout<<mean_shape_.rows<<endl;
    for(int i = 0;i < landmark_num_;i++){
        fout<<mean_shape_(i,0)<<" "<<mean_shape_(i,1)<<" "; 
    }
    fout<<endl;
    
    fout<<training_shapes_.size()<<endl;
    for(int i = 0;i < training_shapes_.size();i++){
        fout<<bounding_box_[i].start_x<<" "<<bounding_box_[i].start_y<<" "
            <<bounding_box_[i].width<<" "<<bounding_box_[i].height<<" "
            <<bounding_box_[i].centroid_x<<" "<<bounding_box_[i].centroid_y<<endl;
        for(int j = 0;j < training_shapes_[i].rows;j++){
            fout<<training_shapes_[i](j,0)<<" "<<training_shapes_[i](j,1)<<" "; 
        }
        fout<<endl;
    }
    
    for(int i = 0;i < first_level_num_;i++){
        fern_cascades_[i].Write(fout);
    } 
}

void ShapeAlignment::Read(ifstream& fin){
    fin>>first_level_num_;
    fin>>landmark_num_;
    mean_shape_ = Mat::zeros(landmark_num_,2,CV_64FC1);
    for(int i = 0;i < landmark_num_;i++){
        fin>>mean_shape_(i,0)>>mean_shape_(i,1);
    }
    
    int training_num;
    fin>>training_num;
    training_shapes_.resize(training_num);
    bounding_box_.resize(training_num);

    for(int i = 0;i < training_num;i++){
        BoundingBox temp;
        fin>>temp.start_x>>temp.start_y>>temp.width>>temp.height>>temp.centroid_x>>temp.centroid_y;
        bounding_box_[i] = temp;
        
        Mat_<double> temp1(landmark_num_,2);
        for(int j = 0;j < landmark_num_;j++){
            fin>>temp1(j,0)>>temp1(j,1);
        }
        training_shapes_[i] = temp1; 
    }

    fern_cascades_.resize(first_level_num_);
    for(int i = 0;i < first_level_num_;i++){
        fern_cascades_[i].Read(fin);
    }
} 


Mat_<double> ShapeAlignment::Predict(const Mat_<uchar>& image, const BoundingBox& bounding_box, int initial_num){
    // generate multiple initializations
    Mat_<double> result = Mat::zeros(landmark_num_,2, CV_64FC1);
    RNG random_generator(getTickCount());
    for(int i = 0;i < initial_num;i++){
        random_generator = RNG(i);
        int index = random_generator.uniform(0,training_shapes_.size());
        Mat_<double> current_shape = training_shapes_[index];
        BoundingBox current_bounding_box = bounding_box_[index];
        current_shape = ProjectShape(current_shape,current_bounding_box);
        current_shape = ReProjectShape(current_shape,bounding_box);
        for(int j = 0;j < first_level_num_;j++){
            Mat_<double> prediction = fern_cascades_[j].Predict(image,bounding_box,mean_shape_,current_shape);
            // update current shape
            current_shape = prediction + ProjectShape(current_shape,bounding_box);
            current_shape = ReProjectShape(current_shape,bounding_box);
            visualizeImage(image, current_shape, 10, false, "initial_num_first_level");
        }

        visualizeImage(image, current_shape, 0, false, "initial_num");
        result = result + current_shape; 
    }    

    return 1.0 / initial_num * result;
}

void ShapeAlignment::Load(string path){
    cout<<"Loading model..."<<endl;
    ifstream fin;
    fin.open(path);
    this->Read(fin); 
    fin.close();
    cout<<"Model loaded successfully..."<<endl;
}

void ShapeAlignment::Save(string path){
    cout<<"Saving model..."<<endl;
    ofstream fout;
    fout.open(path);
    this->Write(fout);
    fout.close();
}


