cmake_minimum_required(VERSION 3.1)
project(VehicleTracking)

set(OUTPUT_SRC_DIR ${CMAKE_BINARY_DIR})
file(COPY haarcascade_frontalface_alt.xml DESTINATION ${OUTPUT_SRC_DIR})

find_package( OpenCV REQUIRED )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES_CMT main.cpp CMT.cpp)
add_executable(VehicleTrackingCMT ${SOURCE_FILES_CMT} )
target_link_libraries( VehicleTrackingCMT ${OpenCV_LIBS} )



#add subdirectories
add_subdirectory(3rdparty/cvblobs)
add_subdirectory(3rdparty/libconfig)
add_subdirectory(libopentld)

link_directories(${OpenCV_LIB_DIR})

include_directories(main
    libopentld/imacq
    libopentld/mftracker
    libopentld/tld
    3rdparty/cvblobs
)
include_directories(3rdparty/libconfig)

set(SOURCE_FILES_TLD main_tld.cpp)
add_executable(VehicleTrackingTLD ${SOURCE_FILES_TLD} )
target_link_libraries( VehicleTrackingTLD libopentld cvblobs config++ ${OpenCV_LIBS} )




