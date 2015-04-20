cmake_minimum_required(VERSION 3.1)
project(FacialPointDetectionv2)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(FacialPointDetectionv2 ${SOURCE_FILES})