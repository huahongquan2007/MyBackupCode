package com.example.bindservicesendimage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

// Steps for service
// 1. Create a new Project name "BindServiceUsingBinderClass" 
// 2. Create one Service in your application by extending the Service class 
// 3. Create <service> in Manifest
// 4. Create a class "LocalBinder" inside your service and extends "Binder" class in this class
// 5. Implement the onBind() method of the service and return the instance of the "LocalBinder" class

public class Server extends Service {
	IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public Server getServerInstance() {
			return Server.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("MyLog", "Server onBind");
		try {
			initCamera();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mBinder;
	}

	public String getTime() {
		SimpleDateFormat mDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return mDateFormat.format(new Date());
	}

	// =============== CAMERA =======================//
	private Camera mCam;
	public static int camWidth = 320, camHeight = 240;

	private SurfaceTexture texture;
	private YuvImage yuvImg = null;
	private Rect rect;
	int imageFormat, w, h;

	public void initCamera() throws IOException {
		mCam = Camera.open(1);
		Camera.Parameters p = mCam.getParameters();
		
		Size psize = p.getSupportedPreviewSizes().get(0); 
		camWidth = psize.width; camHeight = psize.height;
		
		p.setPreviewSize(camWidth, camHeight);
		mCam.setParameters(p);
		texture = new SurfaceTexture(10);
		mCam.setPreviewTexture(texture);
		mCam.startPreview();
		mCam.setPreviewCallback(new PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				Log.e("MyLog", "AndroidCameraService onPreviewFrame");
				if(yuvImg == null){
					Camera.Parameters parameters= camera.getParameters(); 
	                imageFormat=parameters.getPreviewFormat();  
	                w=parameters.getPreviewSize().width;  
	                h=parameters.getPreviewSize().height;
	                rect=new Rect(0,0,w,h);
				}                
                yuvImg = new YuvImage(data,imageFormat,w,h,null);
			}
		});
	}

	public Bitmap getFrame() {
		if (yuvImg == null)
			return null;

		ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
		yuvImg.compressToJpeg(rect, 100, outputstream);
		return BitmapFactory.decodeByteArray(outputstream.toByteArray(), 0,
				outputstream.size());
	}

	@Override
	public void onDestroy() {
		mCam.stopPreview();
		mCam.setPreviewCallback(null);
		mCam.release();
		super.onDestroy();
	}

}
