#include "aios_core_vision_facelearning_NativeFaceLearning.h"

JNIEXPORT jstring JNICALL Java_aios_core_vision_facelearning_NativeFaceLearning_update
  (JNIEnv * env, jclass job, jbyteArray frameArray){
  return env->NewStringUTF("string from jni");
}