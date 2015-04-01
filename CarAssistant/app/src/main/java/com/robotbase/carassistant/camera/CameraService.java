package com.robotbase.carassistant.camera;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.robotbase.carassistant.utils.VisionConfig;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {
    private boolean autoStop = false;
    private boolean isActivite = false;
    private long lastTimerRequestFrame = 0;
    private Timer timer;
    private ToggleCameraReceiver toggleCameraReceiver;
    private int CAMERA_ID = VisionConfig.CAMERA_ID;
    public static int camWidth = 480, camHeight = 640, camChannels = 3;
    private Camera mCam;
    private int fps = 15;
    private SurfaceTexture texture;
    private byte[] callbackBuffer = new byte[camWidth * camHeight * 3];
    public ReentrantLock previewBufferLock = new ReentrantLock();
    private ReentrantLock countFrameLock = new ReentrantLock();
    private int countFrame = 0;
    byte[] frameData = null;
    private Bitmap lastBitmap = null;
    private Mat lastMat, originalMat, yuvMat;
    private boolean isStart = false;
    public CameraService() {
    }

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CameraService getServerInstance() {
            return CameraService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d("Vision", "CameraService onCreate");

        toggleCameraReceiver = new ToggleCameraReceiver();
        IntentFilter filterToggle = new IntentFilter(
                com.robotbase.carassistant.Intent.TOGGLE_CAMERA_SERVICE);
        registerReceiver(toggleCameraReceiver, filterToggle);

        turnOnCamera();
        super.onCreate();
    }
    private void setup(int fps){
        this.fps = fps;
    }
    public int getFPS(){
        return fps;
    }
    public byte[] getFrame(){
        for(int i = 0; i < 10 ; i++){
            try{
                if(countFrameLock.tryLock()){
                    if(countFrame > 0){
                        if(frameData == null)
                            frameData = new byte[(int)lastMat.total() * lastMat.channels()];
                        lastMat.get(0, 0, frameData);
                        lastTimerRequestFrame = System.currentTimeMillis();
                    }
                    countFrameLock.unlock();
                    break;
                }
                Thread.sleep(50);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return frameData;
    }
    public boolean isStarted(){

        if(isStart == false){
            if(countFrameLock.tryLock()){
                if(countFrame > 0) {
                    isStart = true;
                }
                countFrameLock.unlock();
            }
        }

        return isStart;
    }
    public Bitmap getFrameBitmap(){
//        Log.i("Vision", "getFrameBitmap");
        for(int i = 0; i < 10 ; i++){
            try{
                if(countFrameLock.tryLock()){
                    if(countFrame > 0){
                        if(lastBitmap == null){
                            if(VisionConfig.CAMERA_ORIENTATION == 0)
                                lastBitmap = Bitmap.createBitmap( camHeight,camWidth, Bitmap.Config.ARGB_8888);
                            else
                                lastBitmap = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888);
                        }

                        Log.i("VIsion", "Lastmat: " + lastMat.size().toString() + " BITMAP : " + lastBitmap.getWidth() + " " + lastBitmap.getHeight());
                        Utils.matToBitmap(lastMat, lastBitmap);
                        lastTimerRequestFrame = System.currentTimeMillis();
//                        Log.i("Vision", "getFrameBitmap matToBitmap");
                    }
                    countFrameLock.unlock();
                    break;
                }
                Thread.sleep(20);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return lastBitmap;
    }
    @Override
    public void onDestroy() {
        Log.d("Vision", "CameraService onDestroy ");
        try{
            unregisterReceiver(toggleCameraReceiver);
            if( isActivite )
                turnOffCamera();
        }catch(Exception e){
            e.printStackTrace();
        }

        super.onDestroy();
    }

    class ToggleCameraReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean command = intent.getBooleanExtra("status", true);
            Log.d("Vision", "CameraService ToggleCameraReceiver " + command);
            if (command) {
                //command true
                if ( !isActivite )
                    turnOnCamera();
            } else {
                //command false
                if ( isActivite )
                    turnOffCamera();
            }
        }
    }

    public boolean turnOnCamera() {
        Log.d("Vision", "CameraService turnOnCamera Before: " + isActivite);
        if ( !isActivite ) {

            // reset service var
            countFrameLock.lock();
            frameData = null;
            lastBitmap = null;
            isStart = false;
            countFrame = 0;
            if(VisionConfig.CAMERA_ORIENTATION == 1)
                lastMat = new Mat(camHeight, camWidth, CvType.CV_8UC3); // rows = 640, cols = 480
            else
                lastMat = new Mat(camWidth, camHeight, CvType.CV_8UC3); // rows = 480, cols = 640
            originalMat = new Mat(camWidth, camHeight, CvType.CV_8UC3); // rows = 480, cols = 640
            yuvMat = new Mat(camWidth + camWidth / 2, camHeight, CvType.CV_8UC1); // rows = 480, cols = 640

            countFrameLock.unlock();

            // reset service var

            turnOnAndroidCamera();

            if(autoStop){
                timer = new Timer();
                lastTimerRequestFrame = System.currentTimeMillis();
                timer.schedule(new AutoStopTimerTask(), 0,
                        (long) (2000));
            }

        }

        Log.d("Vision", "CameraService turnOnBaseCamera After: " + isActivite);
        return isActivite;
    }

    public boolean turnOffCamera() {
        Log.d("Vision", "CameraService turnOffBaseCamera Before: " + isActivite);
        if ( isActivite ) {
            turnOffAndroidCamera();
        }

        Log.d("Vision", "CameraService turnOffBaseCamera After: " + isActivite);
        return isActivite;
    }


    private void turnOnAndroidCamera(){
        try {
            mCam = Camera.open(CAMERA_ID);

            int minFps = 0, maxFps = 0;
            for (int i = 0; i < mCam.getParameters()
                    .getSupportedPreviewFpsRange().size(); i++) {
                int[] size = mCam.getParameters().getSupportedPreviewFpsRange()
                        .get(i);
                Log.d("Vision", "CameraService supported fps: " + String.valueOf(size[0])
                        + " " + String.valueOf(size[1]));
                minFps = size[0];
                maxFps = size[1];
                if (size[0] > 1000) {
                    break;
                }
            }

            for (int i = 0; i < mCam.getParameters()
                    .getSupportedPreviewSizes().size(); i++) {
                Camera.Size size = mCam.getParameters().getSupportedPreviewSizes().get(i);
                Log.d("Vision", "CameraService supported size: " + size.width + " " + size.height);
            }
            // Setup FPS
            setup(minFps/1000);

            Camera.Parameters p = mCam.getParameters();
            p.setPreviewSize(camHeight, camWidth); // width = camHeight, height = camWidth
            p.setPreviewFpsRange(minFps, maxFps);
            mCam.setParameters(p);
            texture = new SurfaceTexture(10);
            mCam.setPreviewTexture(texture);
            mCam.startPreview();
            mCam.addCallbackBuffer(callbackBuffer);
            mCam.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    previewBufferLock.lock();
//                    Log.d("Vision", "CameraService onPreview");

                    for(int i = 0; i < 10; i++){
                        if( countFrameLock.tryLock() ){
                            countFrame += 1;

                            // save to Mat
                            yuvMat.put(0, 0, data);
                            Imgproc.cvtColor(yuvMat, originalMat, Imgproc.COLOR_YUV420sp2RGB);

                            if(VisionConfig.CAMERA_ORIENTATION == 1)
                                lastMat = originalMat.t();
                            else
                                lastMat = originalMat.clone();

                            if(VisionConfig.CAMERA_ID == 1)
                                Core.flip(lastMat, lastMat, 0);


//                            Log.d("Vision", "Width: " + originalMat.width() + " Height: " + originalMat.height() + " Total: " + originalMat.total() * originalMat.channels());
                            // end save to Mat

                            camera.addCallbackBuffer(data);
                            countFrameLock.unlock();
                            break;
                        }
                        try{
                            Thread.sleep(50);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    previewBufferLock.unlock();
                }
            });

            Log.d("Vision", "CameraService after setPreviewCallback");
            isActivite = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void turnOffAndroidCamera(){
        try{
            mCam.stopPreview();
            mCam.setPreviewCallback(null);
            mCam.release();

            isActivite = false;
            Log.d("Vision", "CameraService turn off Android camera");
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    private class AutoStopTimerTask extends TimerTask {

        @Override
        public void run() {

            if (System.currentTimeMillis() - lastTimerRequestFrame > 30 * 1000) {
                Log.d("Vision", "CameraService AutoStop");
                turnOffCamera();
                cancel();
            }
        }
    }
}
