package aios.core.vision.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import aios.core.vision.R;
import aios.core.vision.camera.CameraService;
import aios.core.vision.utils.VisionConfig;

public class CameraPreviewActivity extends ActionBarActivity {

    private SurfaceView mCamSV;

    // broadcast receiver
    // ----------- Face Detection
    private FaceDetectionReceiver faceDetectionReceiver;
    private String faceDetectionResult = "";
    private long lastFaceDetectionTime = 0;
    private ReentrantLock faceDetectionLock = new ReentrantLock();

    // ----------- Face tracking
    private FaceTrackingReceiver faceTrackingReceiver;
    private String faceTrackingResult = "";
    private long lastFaceTrackingTime = 0;
    private ReentrantLock faceTrackingLock = new ReentrantLock();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        Log.i("Vision", "CAP: onCreate");

        // setup surfaceview
        mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_view);

        mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
                VisionConfig.getHeight());

        // Setup Bind Service
        VisionConfig.bindService(this, mConnection);

        // Setup broadcast Receiver
        // ---- Face detection
        faceDetectionReceiver = new FaceDetectionReceiver();
        IntentFilter filterFaceDetection = new IntentFilter(aios.core.vision.Intent.ACTION_FACE_DETECTION);
        registerReceiver(faceDetectionReceiver, filterFaceDetection);
        // ---- Face tracking
        faceTrackingReceiver = new FaceTrackingReceiver();
        IntentFilter filterFaceTracking = new IntentFilter(aios.core.vision.Intent.ACTION_FACE_TRACKING);
        registerReceiver(faceTrackingReceiver, filterFaceTracking);
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
        private Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(),
                VisionConfig.getHeight(), Bitmap.Config.ARGB_8888);
        private Canvas cover;

        // Face detection
        private Paint ptDetect = new Paint();
        private String curFaceDetectionResult;

        // Face tracking
        private Paint ptTrack = new Paint();
        private String curFaceTrackingResult;

        public GetImageTask() {
            // Face detection
            ptDetect.setColor(Color.GREEN);
            ptDetect.setTextSize(50);
            ptDetect.setStrokeWidth(3);
            ptDetect.setStyle(Paint.Style.STROKE);

            // Face tracking
            ptTrack.setColor(Color.BLUE);
            ptTrack.setTextSize(50);
            ptTrack.setStrokeWidth(3);
            ptTrack.setStyle(Paint.Style.STROKE);
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

            // ------------------------------------
            // update extra information
            try{
                updateFaceDetection();
                updateFaceTracking();
            } catch (Exception e) {
            e.printStackTrace();
            }

            // ------------------------------------


            mCamSV.getHolder().unlockCanvasAndPost(cover);
        }

        private void updateFaceDetection() throws Exception{

            faceDetectionLock.lock();
            curFaceDetectionResult = "";
            if (System.currentTimeMillis() - lastFaceDetectionTime < 500) {
                curFaceDetectionResult = faceDetectionResult;
            }
            faceDetectionLock.unlock();
            if (curFaceDetectionResult.isEmpty())
                return;
            JSONArray data = new JSONArray(curFaceDetectionResult);
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
        private void updateFaceTracking() throws Exception{

            faceTrackingLock.lock();
            curFaceTrackingResult = "";
            if (System.currentTimeMillis() - lastFaceTrackingTime < 500) {
                curFaceTrackingResult = faceTrackingResult;
            }
            faceTrackingLock.unlock();

            if (curFaceTrackingResult.isEmpty())
                return;

            JSONArray data = new JSONArray(curFaceTrackingResult);
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
                        ptTrack);
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            VisionConfig.unbindService(this, mConnection);
            unregisterReceiver(faceDetectionReceiver);
            unregisterReceiver(faceTrackingReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Receiver
    public class FaceDetectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String data = intent.getStringExtra("data");
            if (data != null) {
                //Log.d("Vision", "CAP: FaceDetectResult: " + data);
                faceDetectionLock.lock();
                faceDetectionResult = data;
                lastFaceDetectionTime = System.currentTimeMillis();
                faceDetectionLock.unlock();
            } else {
                Log.i("Vision", "CAP: FaceDetectResult: NULL");
            }
        }
    }
    public class FaceTrackingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String data = intent.getStringExtra("data");
            if (data != null) {
                //Log.d("Vision", "CAP: FaceTrackingResult: " + data);
                faceTrackingLock.lock();
                faceTrackingResult = data;
                lastFaceTrackingTime = System.currentTimeMillis();
                faceTrackingLock.unlock();
            } else {
                Log.i("Vision", "CAP: FaceTrackingResult: NULL");
            }
        }
    }
}
