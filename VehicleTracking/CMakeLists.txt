cmake_minimum_required(VERSION 3.1)
project(VehicleTracking)

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)

add_executable(VehicleTracking ${SOURCE_FILES} )

target_link_libraries( VehicleTracking ${OpenCV_LIBS} )

set(OUTPUT_SRC_DIR ${CMAKE_BINARY_DIR})
file(COPY haarcascade_frontalface_alt.xml DESTINATION ${OUTPUT_SRC_DIR})