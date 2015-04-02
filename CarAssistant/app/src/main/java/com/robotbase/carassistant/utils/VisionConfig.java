package com.robotbase.carassistant.utils;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.robotbase.carassistant.camera.CameraService;
import com.robotbase.carassistant.cardetection.CarDetectionService;

public class VisionConfig {
    // ----------- camera info ----------------
    public static final int CAMERA_ID = 0;
    // 0 : Back Camera
    // 1 : Front Camera

    public static final int CAMERA_ORIENTATION = 0;
    // 0 : landscape
    // 1 : portrait

    public static void startService(Context c){
        try{
//            c.startService(new Intent(c, CameraService.class));
            c.startService(new Intent(c, CarDetectionService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void stopService(Context c){
        try {
//            c.stopService(new Intent(c, CameraService.class));
            c.stopService(new Intent(c, CarDetectionService.class));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int getByteLength(){
        return getHeight() * getWidth() * getChannels();
    }
    public static int getWidth() {
        if(CAMERA_ORIENTATION == 0)
            return CameraService.camHeight;
        return CameraService.camWidth;
    }
    public static int getHeight() {
        if(CAMERA_ORIENTATION == 0)
            return CameraService.camWidth;
        return CameraService.camHeight;
    }
    public static int getChannels(){
        return CameraService.camChannels;
    }
    public static void bindService(Context c, ServiceConnection mConnection) {
        try{
            c.bindService(new Intent(c, CameraService.class), mConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void unbindService(Context c, ServiceConnection mConnection) {
        try{
            c.unbindService(mConnection);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
