#include <jni.h>
#include "com_example_panorama_NativePanorama.h"

#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"

using namespace std;
using namespace cv;

#include <android/log.h>

#define LOG_TAG    "NativePanoramaCV"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT void JNICALL Java_com_example_panorama_NativePanorama_processPanorama
  (JNIEnv * env, jclass clazz, jlongArray imageArray, jlong resultAddress){
    vector< Mat > imgArr;
    jsize a_len = env->GetArrayLength(imageArray);
    jlong *imgAddressArr = env->GetLongArrayElements(imageArray,0);

    for(int k=0;k<a_len;k++)
    {
        Mat & newimage=*(Mat*)imgAddressArr[k];

        cvtColor(newimage, newimage, CV_BGRA2BGR);

        imgArr.push_back(newimage);

        LOG("update: newImage: Rows %d Cols %d Channels %d", newimage.rows, newimage.cols, newimage.channels());
    }

    Mat & result  = *(Mat*) resultAddress;
//    Mat result;

    Stitcher stitcher = Stitcher::createDefault();
    stitcher.stitch(imgArr, result);

    LOG("update: NumOfFace: Rows %d Cols %d", result.rows, result.cols);

    env->ReleaseLongArrayElements(imageArray, imgAddressArr ,0);
}