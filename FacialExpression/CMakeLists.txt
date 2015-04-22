cmake_minimum_required(VERSION 3.1)
project(FacialExpression)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(FacialExpression ${SOURCE_FILES} utility.cpp)
target_link_libraries( FacialExpression ${OpenCV_LIBS} )