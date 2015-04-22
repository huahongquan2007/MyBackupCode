cmake_minimum_required(VERSION 3.1)
project(FacialPointDetection)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)

add_executable(FacialPointDetection ${SOURCE_FILES} utility.h utility.cpp Configuration.cpp Configuration.h ShapeAlignment.cpp ShapeAlignment.h NormalRegressor.cpp NormalRegressor.h FernRegressor.cpp FernRegressor.h)

target_link_libraries( FacialPointDetection ${OpenCV_LIBS} )


set(SOURCE_FILES_TEST main_test.cpp)

add_executable(FacialPointDetectionTest ${SOURCE_FILES_TEST} utility.h utility.cpp Configuration.cpp Configuration.h )

target_link_libraries( FacialPointDetectionTest ${OpenCV_LIBS} )

set(SOURCE_FILES_IBUG main_ibug.cpp)

add_executable(FacialPointDetectionIbug ${SOURCE_FILES_IBUG} utility.h utility.cpp Configuration.cpp Configuration.h ShapeAlignment.cpp ShapeAlignment.h NormalRegressor.cpp NormalRegressor.h FernRegressor.cpp FernRegressor.h)

target_link_libraries( FacialPointDetectionIbug ${OpenCV_LIBS} )

set(SOURCE_FILES_EX main_expression.cpp)

add_executable(FacialPointDetectionExpression ${SOURCE_FILES_EX} utility.h utility.cpp Configuration.cpp Configuration.h ShapeAlignment.cpp ShapeAlignment.h NormalRegressor.cpp NormalRegressor.h FernRegressor.cpp FernRegressor.h)

target_link_libraries( FacialPointDetectionExpression ${OpenCV_LIBS} )


set(SOURCE_FILES_EX_VISUAL main_visualizeExpression.cpp)

add_executable(FacialPointDetectionVisualizeExpression ${SOURCE_FILES_EX_VISUAL} utility.h utility.cpp Configuration.cpp Configuration.h ShapeAlignment.cpp ShapeAlignment.h NormalRegressor.cpp NormalRegressor.h FernRegressor.cpp FernRegressor.h)

target_link_libraries( FacialPointDetectionVisualizeExpression ${OpenCV_LIBS} )