package robotbase.vision;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import robotbase.action.RobotIntent;
import robotbase.vision.R;
import robotbase.vision.camera.CameraService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraPreviewActivity extends Activity {
	private SurfaceView mCamSV;
	private FrameDataReceiver frameDataReceiver;
	private FrameOverlayReceiver frameOverlayReceiver;
	private Handler handler; // Handler for the separate Thread
	private Handler handlerOverlay; // Handler for the separate Thread
	
	private int modeSV = -1;
	private String textSV = "";
	private float xSV =0 , ySV = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_preview);
		mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_cam);
		
		mCamSV.getHolder().setFixedSize(AndroidCameraService.camWidth, AndroidCameraService.camHeight);
		
		Log.i("MyLog", "CAP: onCreate");
		
		frameDataReceiver = new FrameDataReceiver();
		IntentFilter filterVision;
		if(VisionConfig.isAndroidCamera){
			filterVision = new IntentFilter(AndroidCameraService.CAMERA_INTENT_BITMAP);
		}else{
			filterVision = new IntentFilter(CameraService.CAMERA_INTENT_BITMAP);
		}
		
		
		// // registerReceiver(frameDataReceiver, filterVision);
		// Using Handler
		HandlerThread handlerThread = new HandlerThread("MyNewThread");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();
		// Create a handler for the service
		handler = new Handler(looper);
		// Register the broadcast receiver to run on the separate Thread
		registerReceiver (frameDataReceiver, filterVision, null, handler);
		
		
		// End Using Handler
		frameOverlayReceiver = new FrameOverlayReceiver();
		IntentFilter filterOverlayVision  = new IntentFilter(RobotIntent.CAM_TAKE_PICKTURE);
		// // registerReceiver(frameOverlayReceiver, filterOverlayVision);
		// Using Handler
		HandlerThread handlerThreadOverlay = new HandlerThread("MyNewThreadOverlay");
		handlerThreadOverlay.start();
		Looper looperOverlay = handlerThreadOverlay.getLooper();
		// Create a handler for the service
		handlerOverlay = new Handler(looperOverlay);
		// Register the broadcast receiver to run on the separate Thread
		registerReceiver (frameOverlayReceiver, filterOverlayVision, null, handlerOverlay);
		
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("MyLog", "CAP: onStart");
//		CameraPreviewActivity.this.finish();
	}
	
	@Override
	public void finish() {
		Log.i("MyLog", "CAP: FINISH");
		super.finish();
	}
	@Override
	protected void onDestroy() {
		unregisterReceiver(frameDataReceiver);
		unregisterReceiver(frameOverlayReceiver);
		super.onDestroy();
	}
	public class FrameDataReceiver extends BroadcastReceiver {
		Bitmap bitmap = null;
		Bitmap image = null;
		boolean isCaptured = false;
		long capturedTime = 0;
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","CAP: FrameData onReceive");
			
			if(isCaptured == false)
			{
				if(VisionConfig.isAndroidCamera){
					bitmap = (Bitmap) intent.getParcelableExtra(AndroidCameraService.CAMERA_DATA);				
				}else{
					bitmap = (Bitmap) intent.getParcelableExtra(CameraService.CAMERA_DATA);
				}
			}
			else{
				bitmap = image;
				if(System.currentTimeMillis() - capturedTime > 3000){
					CameraPreviewActivity.this.finish();
					return;
				}
			}
							
			Canvas cover = mCamSV.getHolder().lockCanvas(null);
			if(cover == null) return;
			cover.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear Canvas
			cover.drawColor(Color.WHITE);	
			
			if(bitmap != null && cover != null)
			{
				cover.drawBitmap(bitmap, 0, 0, null);
			}else{
				Log.i("MyLog","CAP: BITMAP NULL");
			}
			Paint pt = new Paint();
			pt.setColor(Color.RED);
			pt.setTextSize(50);
			pt.setStrokeWidth(3);
			// Draw circle
			switch(modeSV){
				case 0:
					cover.drawCircle(xSV, ySV, 10, pt);
					break;
				case 1:
					cover.drawText(textSV, xSV, ySV, pt);
					break;
			}
			
			
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
		public void onImageCaptured(String filePath){
			FileInputStream streamIn = null;
			try {
				streamIn = new FileInputStream(filePath);
				image = BitmapFactory.decodeStream(streamIn);
				streamIn.close();	
				isCaptured = true;
				capturedTime = System.currentTimeMillis();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	public class FrameOverlayReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","CAP: FrameOverlay onReceive");
				
			int mode = intent.getIntExtra("mode", -1);
			switch(mode){
				case 0:
					float x = intent.getFloatExtra("x", 0);
					float y = intent.getFloatExtra("y", 0);
					Log.i("MyLog","CAP: FrameOverlay: x = " + String.valueOf(x) + " y = " + String.valueOf(y));
					xSV = x;
					ySV = y;
					modeSV = 0;
					break;
				case 1:
					String text = intent.getStringExtra("text");
					
					xSV = AndroidCameraService.camWidth/2.0f - 25;
					ySV = AndroidCameraService.camHeight/2.0f + 25;
					textSV = text;
					modeSV = 1;
					break;
				case 2:
					modeSV = 2;
					// Save Image
					String fileName = intent.getStringExtra("text");
		            // Show a toast message on successful save
		            Toast.makeText(CameraPreviewActivity.this, "Image Saved " + fileName, Toast.LENGTH_SHORT).show();
		            CameraPreviewActivity.this.frameDataReceiver.onImageCaptured(fileName);
					break;
				default:
					modeSV = -1;
			}
		}
	
	}
}
