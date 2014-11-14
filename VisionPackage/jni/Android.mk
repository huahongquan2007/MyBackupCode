LOCAL_PATH := $(call my-dir) 

include $(CLEAR_VARS)
LOCAL_MODULE := faceppapi
LOCAL_SRC_FILES = FaceLib/libs/$(TARGET_ARCH_ABI)/libfaceppapi.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := faceppofflineapi
LOCAL_SRC_FILES = FaceLib/libs/$(TARGET_ARCH_ABI)/libofflineapi.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := opencv-prebuilt
LOCAL_SRC_FILES = buildOpenCV/libs/$(TARGET_ARCH_ABI)/libopencv_java.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/buildOpenCV/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libnative_camera_r4.4.0
LOCAL_SRC_FILES = buildOpenCV/libs/$(TARGET_ARCH_ABI)/libnative_camera_r4.4.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS) 
# Sources
MY_SRC_FILES := \
	$(LOCAL_PATH)/Build_OpenNI2/libOpenNI2.so

MY_SRC_FILE_EXPANDED := $(wildcard $(MY_SRC_FILES))
LOCAL_SRC_FILES := $(MY_SRC_FILE_EXPANDED:$(LOCAL_PATH)/%=%)

# C/CPP Flags
LOCAL_CFLAGS += $(OPENNI2_CFLAGS)

# Includes
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/Build_OpenNI2/Include \
	$(LOCAL_PATH)/Build_OpenNI2/Common
LOCAL_MODULE := Openni2
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := opencv-prebuilt 
LOCAL_SRC_FILES := robotbase_vision_NativeMotionDetection.cpp utility.cpp
LOCAL_MODULE := NativeMotionDetection
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := opencv-prebuilt 
LOCAL_SRC_FILES := robotbase_vision_NativeFaceDetection.cpp
LOCAL_MODULE := NativeFaceDetection
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := opencv-prebuilt 
LOCAL_SRC_FILES := robotbase_vision_NativeFaceTracking.cpp
LOCAL_MODULE := NativeFaceTracking
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := opencv-prebuilt 
LOCAL_SRC_FILES := robotbase_vision_NativeAndroidCamera.cpp
LOCAL_MODULE := NativeAndroidCamera
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
# Dependencies	
LOCAL_SHARED_LIBRARIES := Openni2 opencv-prebuilt
LOCAL_SRC_FILES := robotbase_vision_camera_NativeOpenni2.cpp
# Output
LOCAL_MODULE := NativeOpenni2
LOCAL_LDLIBS += -llog -ldl
include $(BUILD_SHARED_LIBRARY)