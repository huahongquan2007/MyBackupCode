cmake_minimum_required(VERSION 3.1)
project(FacialPointDetection)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)

add_executable(FacialPointDetection ${SOURCE_FILES} utility.h utility.cpp Configuration.cpp Configuration.h)

target_link_libraries( FacialPointDetection ${OpenCV_LIBS} )
