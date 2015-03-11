package aios.core.vision.baseclasses;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import aios.core.vision.camera.CameraService;
import aios.core.vision.utils.VisionConfig;

public abstract class VisionBaseClass extends Service {
    private int fps = 0;
    protected int width = VisionConfig.getWidth();
    protected int height = VisionConfig.getHeight();

    public VisionBaseClass() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fps = setupFPS();
        this.setup();

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
            timer.schedule(new GetImageTask(), 0, 1000 / fps);
        }
    };

    abstract protected void setup();
    abstract protected void finishProcess();

    abstract protected void update(byte[] frame);

    abstract protected void broadcast();

    abstract protected int setupFPS();

    class GetImageTask extends TimerTask {
        private byte[] data = new byte[VisionConfig.getByteLength()];

        @Override
        public void run() {
            if (mCameraService == null) {
                Log.e("Vision",
                        "VisionBaseClass: GetImageTask NULL mCameraService");
                return;
            }
            if (mCameraService.isStarted() == false) {
//                Log.e("Vision", "VisionBaseClass: mCameraService isStarted = false");
                return;
            }

            data = mCameraService.getFrame();
            update(data);
            broadcast();
        }

    }

    @Override
    public void onDestroy() {
        try {
            VisionConfig.unbindService(this, mConnection);
            finishProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
