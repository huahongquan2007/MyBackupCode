cmake_minimum_required(VERSION 3.2)
project(OpenCV_Blueprints_ml_module)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(PREPARE_DATASET_FILES prepare_dataset.cpp dirent.h)
add_executable(prepare_dataset ${PREPARE_DATASET_FILES})
target_link_libraries( prepare_dataset ${OpenCV_LIBS} )

set(TRAIN_DATA train.cpp)
add_executable(train_data ${TRAIN_DATA})
target_link_libraries( train_data ${OpenCV_LIBS} )