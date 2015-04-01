package com.robotbase.carassistant;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import com.robotbase.carassistant.camera.CameraService;
import com.robotbase.carassistant.utils.VisionConfig;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    static {
        System.loadLibrary("opencv_java");
    }

    private SurfaceView mCamSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup surfaceview
        mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_cam);

        if(VisionConfig.CAMERA_ORIENTATION == 1)
            mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
                VisionConfig.getHeight());
        else
            mCamSV.getHolder().setFixedSize(VisionConfig.getHeight(), VisionConfig.getWidth()
                    );


        // Setup Bind Service
        VisionConfig.bindService(this, mConnection);
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

            mCamSV.getHolder().unlockCanvasAndPost(cover);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VisionConfig.unbindService(getApplicationContext(), mConnection);
    }
}
