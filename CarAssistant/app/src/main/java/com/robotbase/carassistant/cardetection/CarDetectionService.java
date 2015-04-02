package com.robotbase.carassistant.cardetection;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.robotbase.carassistant.R;
import com.robotbase.carassistant.baseclasses.VisionBaseClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

public class CarDetectionService extends VisionBaseClass {
    String result = "";
    private ReentrantLock resultLock = new ReentrantLock();
    static {
        Log.i("Vision", "CarDetect setup()");
        System.loadLibrary("NativeCarDetection");
    }
    @Override
    protected void setup() {
        initOpenCV(getApplicationContext());
    }

    @Override
    protected void finishProcess() {

    }

    @Override
    protected void update(byte[] frame){
        try{
        Log.i("Vision", "FaceDetect update frame length " + frame.length);
            result = NativeCarDetection.update(frame, width, height);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void broadcast() {
        if(!result.isEmpty()){

            try{
                Log.i("Vision", "FaceDetect broadcast " + System.currentTimeMillis() + " " + result);
                Intent intent = new Intent();
                intent.putExtra("data", result);
                intent.setAction(com.robotbase.carassistant.Intent.ACTION_CAR_DETECTION);

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

    private void initOpenCV(Context ctx) {
        InputStream is = ctx.getResources().openRawResource(R.raw.car_cascade);

        File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);

        File mCascadeFile = new File(cascadeDir, "car_cascade.xml");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        NativeCarDetection.initCascade(mCascadeFile.getAbsolutePath());
    }
}
