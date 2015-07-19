package com.example.panorama;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class Panorama extends Activity {

    private Button captureBtn, saveBtn;

    private SurfaceView mSurfaceViewOnTop;
    private SurfaceHolder mSurfaceOnTopHolder;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private boolean isPreview;
    private boolean safeToTakePicture = true;
    private Camera mCam;
    private int CAMERA_ID = 0;
    private int FRONT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);

        isPreview = false;
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);

        mSurfaceViewOnTop = (SurfaceView)findViewById(R.id.surfaceViewOnTop);
        mSurfaceViewOnTop.setZOrderOnTop(true);    // necessary
        mSurfaceOnTopHolder = mSurfaceViewOnTop.getHolder();
        mSurfaceOnTopHolder.setFormat(PixelFormat.TRANSPARENT);

        captureBtn = (Button) findViewById(R.id.capture);
        captureBtn.setOnClickListener(captureOnClickListener);

        saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(saveOnClickListener);
    }

    View.OnClickListener captureOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCam != null && safeToTakePicture){
                mCam.takePicture(shutterCallback, rawCallback, jpegCallback);
                safeToTakePicture = false;
            }

        }
    };

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d("Panorama", "onShutter");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Panorama", "onPictureTaken - raw");

        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Panorama", "onPictureTaken - jpeg");

            mCam.startPreview();
            safeToTakePicture = true;

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            Bitmap viewBitmap = Bitmap.createBitmap(mSurfaceView.getWidth(), mSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas c = new Canvas(viewBitmap);
//            mSurfaceView.draw(c);

            Canvas canvas = null;
            try {
                canvas = mSurfaceOnTopHolder.lockCanvas(null);
                synchronized (mSurfaceOnTopHolder) {
                    // Clear canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    // Draw image
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(90);
//                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap, mSurfaceViewOnTop.getWidth(), mSurfaceViewOnTop.getHeight(), false);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                    float scale = 1.0f * mSurfaceView.getHeight() / bitmap.getHeight();
                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap, (int)(scale * bitmap.getWidth()), mSurfaceView.getHeight() , false);

                    Paint paint = new Paint();
                    paint.setAlpha(200);
                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    mSurfaceOnTopHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    };

    View.OnClickListener saveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    SurfaceHolder.Callback mSurfaceCallback
            = new SurfaceHolder.Callback(){

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Camera.Parameters myParameters = mCam.getParameters();
            Camera.Size myBestSize = getBestPreviewSize(width, height, myParameters);

            if(myBestSize != null){
                myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
                mCam.setParameters(myParameters);
                // TODO: may not work on all devices
                mCam.setDisplayOrientation(90);
                mCam.startPreview();
                isPreview = true;

                Toast.makeText(getApplicationContext(),
                        "Best Size:\n" +
                                String.valueOf(myBestSize.width) + " : " + String.valueOf(myBestSize.height),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCam.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

    };

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCam = Camera.open(CAMERA_ID);
    }

    @Override
    protected void onPause() {

        if(isPreview){
            mCam.stopPreview();
        }

        mCam.release();
        mCam = null;
        isPreview = false;

        super.onPause();
    }
}
