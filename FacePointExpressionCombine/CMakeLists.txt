cmake_minimum_required(VERSION 3.2)
project(FacePointExpressionCombine)

find_package( OpenCV REQUIRED )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(FacePointExpressionCombine ${SOURCE_FILES} utility.h utility.cpp Configuration.cpp Configuration.h
 ShapeAlignment.cpp ShapeAlignment.h NormalRegressor.cpp NormalRegressor.h
FernRegressor.cpp FernRegressor.h FacialExpression.cpp FacialExpression.cpp)

target_link_libraries( FacePointExpressionCombine ${OpenCV_LIBS} )