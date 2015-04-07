package aios.core.vision.facetracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

import aios.core.vision.baseclasses.VisionBaseClass;

public class FaceTrackingService extends VisionBaseClass {
    private String result = "";
    private FaceDetectionReceiver faceDetectionReceiver;
    private String faceDetectionString = "";
    private long faceDetectionTime = 0;
    private long lastFaceDetectionTime = 0;
    private boolean needBroadcast = false;
    private ReentrantLock faceDetectionLock = new ReentrantLock();

    static {
        Log.i("Vision", "FaceTracking setup()");
        System.loadLibrary("NativeFaceTracking");
    }

    @Override
    protected void setup() {
        faceDetectionReceiver = new FaceDetectionReceiver();
        IntentFilter filterFaceDetection  = new IntentFilter(ai.vision.Intent.ACTION_FACE_DETECTION);
        registerReceiver(faceDetectionReceiver, filterFaceDetection);
    }
    @Override
    protected void finishProcess() {
        unregisterReceiver(faceDetectionReceiver);
    }
    @Override
    protected void update(byte[] frame) {
        try{
            if(faceDetectionLock.tryLock()){
                needBroadcast = false;
                if(lastFaceDetectionTime != faceDetectionTime){
                    lastFaceDetectionTime = faceDetectionTime;
                    result = NativeFaceTracking.update(frame, width, height, faceDetectionString);
                    needBroadcast = true;
                }
                faceDetectionLock.unlock();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void broadcast() {
        if(!result.isEmpty() && needBroadcast){
            try{
//                Log.i("Vision", "FaceTracking broadcast " + System.currentTimeMillis() + " " + result);
                Intent intent = new Intent();
                intent.putExtra("data", result);
                intent.setAction(ai.vision.Intent.ACTION_FACE_TRACKING);

                sendBroadcast(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int setupFPS() {
        return 10;
    }

    public class FaceDetectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(faceDetectionLock.tryLock()){
                    faceDetectionString = intent.getStringExtra("data");
                    faceDetectionTime = System.currentTimeMillis();
                    //Log.i("Vision", "FaceTracking onReceive " + faceDetectionString);
                    faceDetectionLock.unlock();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
