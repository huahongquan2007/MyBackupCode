#include <jni.h>
#include "com_example_panorama_NativePanorama.h"

#include "opencv2/opencv.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_com_example_panorama_NativePanorama_processPanorama
  (JNIEnv * env, jclass clazz, jlongArray imageArray, jlong resultAddress){
    Mat a(244,244, CV_8UC3);
    Mat b(244,244, CV_8UC3);
    Mat c(244,244, CV_8UC3);
}