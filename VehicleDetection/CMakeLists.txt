cmake_minimum_required(VERSION 3.1)
project(VehicleDetection)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(VehicleDetection ${SOURCE_FILES})

find_package( OpenCV REQUIRED )
set(SOURCE_FILES_GEN_NEG generate_negatives.cpp)
add_executable(VehicleDetectionGenNegatives ${SOURCE_FILES_GEN_NEG} )

target_link_libraries( VehicleDetectionGenNegatives ${OpenCV_LIBS} )

set(SOURCE_FILES_TEST detect_test.cpp)
add_executable(VehicleDetectionDetectTest ${SOURCE_FILES_TEST} )

target_link_libraries( VehicleDetectionDetectTest ${OpenCV_LIBS} )

