LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE := jsoncpp
LOCAL_CPPFLAGS := -fexceptions
LOCAL_SRC_FILES := jsoncpp.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/json
LOCAL_LDLIBS := -L$(call host-path, $(LOCAL_PATH)/../../libs/armeabi)
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= /home/robotbase/Android/OpenCV-2.4.10-android-sdk
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

include $(CLEAR_VARS)

LOCAL_MODULE := NativeFaceDetection
LOCAL_SRC_FILES := aios_core_vision_facedetection_NativeFaceDetectionCV.cpp
LOCAL_LDLIBS += -llog -ldl
LOCAL_SHARED_LIBRARIES += opencv_java
LOCAL_C_INCLUDES += ${OPENCVROOT}/sdk/native/jni/include

# include jsoncpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/json
LOCAL_SHARED_LIBRARIES += jsoncpp

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := NativeFaceTracking
LOCAL_SRC_FILES := aios_core_vision_facetracking_NativeFaceTracking.cpp
LOCAL_LDLIBS += -llog -ldl
LOCAL_SHARED_LIBRARIES += opencv_java
LOCAL_C_INCLUDES += ${OPENCVROOT}/sdk/native/jni/include

# include jsoncpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/json
LOCAL_SHARED_LIBRARIES += jsoncpp

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := NativeFaceRecognition
LOCAL_SRC_FILES := aios_core_vision_facerecognition_NativeFaceRecognition.cpp
LOCAL_LDLIBS += -llog -ldl
LOCAL_SHARED_LIBRARIES += opencv_java
LOCAL_C_INCLUDES += ${OPENCVROOT}/sdk/native/jni/include

# include jsoncpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/json
LOCAL_SHARED_LIBRARIES += jsoncpp

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := NativeFaceLearning
LOCAL_SRC_FILES := aios_core_vision_facelearning_NativeFaceLearning.cpp
LOCAL_LDLIBS += -llog -ldl
LOCAL_SHARED_LIBRARIES += opencv_java
LOCAL_C_INCLUDES += ${OPENCVROOT}/sdk/native/jni/include

# include jsoncpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/json
LOCAL_SHARED_LIBRARIES += jsoncpp

include $(BUILD_SHARED_LIBRARY)