/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class robotbase_vision_NativeFaceTracking */

#ifndef _Included_robotbase_vision_NativeFaceTracking
#define _Included_robotbase_vision_NativeFaceTracking
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    getPos
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_robotbase_vision_NativeFaceTracking_getPos
  (JNIEnv *, jclass, jbyteArray, jint, jint);

/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    setFaceDetection
 * Signature: (I[I[I[I[I)V
 */
JNIEXPORT void JNICALL Java_robotbase_vision_NativeFaceTracking_setFaceDetection
  (JNIEnv *, jclass, jint, jintArray, jintArray, jintArray, jintArray);

/*
 * Class:     robotbase_vision_NativeFaceTracking
 * Method:    getResult
 * Signature: ()[Lrobotbase/vision/FaceInfo;
 */
JNIEXPORT jobjectArray JNICALL Java_robotbase_vision_NativeFaceTracking_getResult
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif