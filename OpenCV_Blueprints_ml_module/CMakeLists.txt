cmake_minimum_required(VERSION 3.2)
project(OpenCV_Blueprints_ml_module)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(PREPARE_DATASET_FILES prepare_dataset.cpp dirent.h)
add_executable(prepare_dataset ${PREPARE_DATASET_FILES})