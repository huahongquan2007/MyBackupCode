package com.robotbase.carassistant;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.robotbase.carassistant.camera.CameraService;
import com.robotbase.carassistant.utils.VisionConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity extends ActionBarActivity {
    static {
        System.loadLibrary("opencv_java");
    }

    private SurfaceView mCamSV;

    // ----------- Car Detection
    private CarDetectionReceiver carDetectionReceiver;
    private String carDetectionResult = "";
    private long lastCarDetectionTime = 0;
    private ReentrantLock carDetectionLock = new ReentrantLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup surfaceview
        mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_cam);

        mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
                VisionConfig.getHeight());

        Log.i("Vision", "MainActivity: " + VisionConfig.getWidth());
        // Setup Bind Service
        VisionConfig.bindService(this, mConnection);
        VisionConfig.startService(this);

        // ---- Face detection
        carDetectionReceiver = new CarDetectionReceiver();
        IntentFilter filterCarDetection = new IntentFilter(com.robotbase.carassistant.Intent.ACTION_CAR_DETECTION);
        registerReceiver(carDetectionReceiver, filterCarDetection);
    }

    // Connection to Bind
    private boolean mBounded;
    private CameraService mCameraService;
    private Timer timer;

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mCameraService = null;

        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            CameraService.LocalBinder mLocalBinder = (CameraService.LocalBinder) service;
            mCameraService = mLocalBinder.getServerInstance();
            // Setup Timer get Frame
            timer = new Timer();
            timer.schedule(new GetImageTask(), 0, 1000 / mCameraService.getFPS());
        }
    };

    class GetImageTask extends TimerTask {
        private Bitmap bitmap;
        private Canvas cover;

        // Car detection
        private Paint ptDetect = new Paint();
        private String curCarDetectionResult;

        public GetImageTask() {
            // Car detection
            ptDetect.setColor(Color.GREEN);
            ptDetect.setTextSize(50);
            ptDetect.setStrokeWidth(3);
            ptDetect.setStyle(Paint.Style.STROKE);
        }
        @Override
        public void run() {
            if (mCameraService == null) {
                Log.e("Vision",
                        "CAP: GetImageTask NULL mCameraService");
                return;
            }
            if (mCameraService.isStarted() == false) {
                Log.e("Vision", "CAP: mCameraService isStarted = false");
                return;
            }

            bitmap = mCameraService.getFrameBitmap();


            cover = mCamSV.getHolder().lockCanvas(null);
            if (cover == null)
                return;
            cover.drawColor(Color.TRANSPARENT,
                    android.graphics.PorterDuff.Mode.CLEAR); // Clear Canvas
            cover.drawColor(Color.WHITE);
            cover.drawBitmap(bitmap, 0, 0, null);

            try{
                updateCarDetection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamSV.getHolder().unlockCanvasAndPost(cover);


        }
        private void updateCarDetection() throws Exception{

            carDetectionLock.lock();
            curCarDetectionResult = "";
            if (System.currentTimeMillis() - lastCarDetectionTime < 500) {
                curCarDetectionResult = carDetectionResult;
            }
            carDetectionLock.unlock();
            if (curCarDetectionResult.isEmpty())
                return;
            JSONArray data = new JSONArray(curCarDetectionResult);
            JSONObject curObj;
            JSONArray curPos;
            for (int i = 0; i < data.length(); i++) {
                curObj = data.getJSONObject(i);
                curPos = curObj.getJSONArray("position");
                cover.drawRect(
                        curPos.getInt(0),
                        curPos.getInt(1),
                        curPos.getInt(0) + curPos.getInt(2),
                        curPos.getInt(1) + curPos.getInt(3),
                        ptDetect);
            }
        }
    }

    // Receiver
    public class CarDetectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            String data = intent.getStringExtra("data");
            if (data != null) {
                //Log.d("Vision", "CAP: FaceDetectResult: " + data);
                carDetectionLock.lock();
                carDetectionResult = data;
                lastCarDetectionTime = System.currentTimeMillis();
                carDetectionLock.unlock();
            } else {
                Log.i("Vision", "CAP: FaceDetectResult: NULL");
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(carDetectionReceiver);
        VisionConfig.unbindService(getApplicationContext(), mConnection);
        VisionConfig.stopService(this);
    }
}
