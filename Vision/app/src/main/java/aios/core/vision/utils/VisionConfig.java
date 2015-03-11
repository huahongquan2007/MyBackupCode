package aios.core.vision.utils;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import aios.core.vision.camera.CameraService;
import aios.core.vision.facedetection.FaceDetectionCVService;
import aios.core.vision.facelearning.FaceLearningService;
import aios.core.vision.facerecognition.FaceRecognitionService;
import aios.core.vision.facetracking.FaceTrackingService;

public class VisionConfig {
    // ----------- camera info ----------------
    public static final int CAMERA_ID = 1;
    // 0 : Back Camera
    // 1 : Front Camera

    // ----------- faceplusplus ----------------
    public static final String USER_PERSON_NAME_PREFIX = "User-01";
    public static final String PERSON_NAME_SEPERATOR = "[RB]";
    public static final String FACE_REG_RETRY_NAME = "RB_RETRY";
    public static final String FACE_REG_FACEAPI = "b24fb274d3e8e93697bfaf29ff3560e4";
    public static final String FACE_REG_FACEKEY = "SDay5MP4bdqk9eZDlvLnYkMhqIzhJ0eb";
    public static final int FACE_REG_MAX_TRY = 1;
    public static final int FACE_REG_THRESHOLD = 50;
    public static final int FACE_REG_TIME_WAIT_TRAIN = 30 * 1000; // Send training command after 30s of no face tracking
    public static final String FACE_REG_GROUP_NAME = "techcrunch";
    public static final boolean FACE_REG_BACKUP = false;
    public static final boolean FACE_REG_BACKUP_TO_SDCARD = true;

    public static void startService(Context c){
        try{
            c.startService(new Intent(c, CameraService.class));
            c.startService(new Intent(c, FaceDetectionCVService.class));
            c.startService(new Intent(c, FaceTrackingService.class));
            c.startService(new Intent(c, FaceRecognitionService.class));
            c.startService(new Intent(c, FaceLearningService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void stopService(Context c){
        try {
            c.stopService(new Intent(c, CameraService.class));
            c.stopService(new Intent(c, FaceDetectionCVService.class));
            c.stopService(new Intent(c, FaceTrackingService.class));
            c.stopService(new Intent(c, FaceRecognitionService.class));
            c.stopService(new Intent(c, FaceLearningService.class));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int getByteLength(){
        return getHeight() * getWidth() * getChannels();
    }
    public static int getWidth() {
        return CameraService.camWidth;
    }
    public static int getHeight() {
        return CameraService.camHeight;
    }
    public static int getChannels(){
        return CameraService.camChannels;
    }
    public static void bindService(Context c, ServiceConnection mConnection) {
         c.bindService(new Intent(c, CameraService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindService(Context c, ServiceConnection mConnection) {
        c.unbindService(mConnection);
    }
}
