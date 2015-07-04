cmake_minimum_required(VERSION 3.2)
project(OpenCV_Blueprints_03_OpenCV_3)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(FACIAL_COMPONENTS_FILES facial_components.cpp)
add_executable(facial_components ${FACIAL_COMPONENTS_FILES})
target_link_libraries( facial_components ${OpenCV_LIBS} )

set(FEATURE_EXTRACTION_FILES feature_extraction.cpp)
add_executable(feature_extraction ${FEATURE_EXTRACTION_FILES})
target_link_libraries( feature_extraction ${OpenCV_LIBS} )
