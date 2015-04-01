package com.robotbase.carassistant.utils;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.robotbase.carassistant.camera.CameraService;

public class VisionConfig {
    // ----------- camera info ----------------
    public static final int CAMERA_ID = 0;
    // 0 : Back Camera
    // 1 : Front Camera

    public static void startService(Context c){
        try{
            c.startService(new Intent(c, CameraService.class));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void stopService(Context c){
        try {
            c.stopService(new Intent(c, CameraService.class));
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
