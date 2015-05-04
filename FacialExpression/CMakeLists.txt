cmake_minimum_required(VERSION 3.1)
project(FacialExpression)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(FacialExpression ${SOURCE_FILES} utility.cpp FacialExpression.cpp)
target_link_libraries( FacialExpression ${OpenCV_LIBS} )

set(SOURCE_FILES_VISUALIZE main_visualizeData.cpp)
add_executable(FacialExpressionVisualize ${SOURCE_FILES_VISUALIZE} utility.cpp FacialExpression.cpp)
target_link_libraries( FacialExpressionVisualize ${OpenCV_LIBS} )