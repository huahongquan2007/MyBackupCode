cmake_minimum_required(VERSION 2.8)
find_package( OpenCV REQUIRED )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(source ./FaceAlignment.h ./FernCascade.cpp ./Fern.cpp ShapeAlignment.cpp ./Utils.cpp ./TrainDemo.cpp)
add_executable(TrainDemo.out ${source})
target_link_libraries(TrainDemo.out ${OpenCV_LIBS})

set(source ./FaceAlignment.h ./FernCascade.cpp ./Fern.cpp ShapeAlignment.cpp ./Utils.cpp ./TestDemo.cpp)
add_executable(TestDemo.out ${source})
target_link_libraries(TestDemo.out ${OpenCV_LIBS})


