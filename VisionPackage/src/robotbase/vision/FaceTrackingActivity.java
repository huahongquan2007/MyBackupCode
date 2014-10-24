package robotbase.vision;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import robotbase.action.RobotIntent;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.Toast;

public class FaceTrackingActivity extends Activity {
	private SurfaceView mCamSV;
	private FrameDataReceiver frameDataReceiver;
	private FrameOverlayReceiver frameOverlayReceiver;
	private Handler handler; // Handler for the separate Thread
	private Handler handlerOverlay; // Handler for the separate Thread
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_tracking);
		mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_cam);
		mCamSV.getHolder().setFixedSize(AndroidCameraService.camWidth, AndroidCameraService.camHeight);
		
		Log.i("MyLog", "FaceTrackActivity: onCreate");
		
		// Service
		if (VisionConfig.isAndroidCamera)
			startService(new Intent(this, AndroidCameraService.class));
		else
			startService(new Intent(this, CameraService.class));
		startService(new Intent(this, VisionService.class));
		
		
		frameDataReceiver = new FrameDataReceiver();
		IntentFilter filterVision;
		if(VisionConfig.isAndroidCamera){
			filterVision = new IntentFilter(AndroidCameraService.CAMERA_INTENT_BITMAP);
		}else{
			filterVision = new IntentFilter(CameraService.CAMERA_INTENT_BITMAP);
		}
		HandlerThread handlerThread = new HandlerThread("MyNewThread");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();
		handler = new Handler(looper);
		registerReceiver (frameDataReceiver, filterVision, null, handler);
		
		
		frameOverlayReceiver = new FrameOverlayReceiver();
		IntentFilter filterOverlayVision  = new IntentFilter(RobotIntent.CAM_FACE_TRACKING);
		HandlerThread handlerThreadOverlay = new HandlerThread("MyNewThreadOverlay");
		handlerThreadOverlay.start();
		Looper looperOverlay = handlerThreadOverlay.getLooper();
		handlerOverlay = new Handler(looperOverlay);
		registerReceiver (frameOverlayReceiver, filterOverlayVision, null, handlerOverlay);
			
		
	}
	public class FrameDataReceiver extends BroadcastReceiver {
		Bitmap bitmap = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","FaceTrackActivity: FrameData onReceive");
			
			if(VisionConfig.isAndroidCamera){
				bitmap = (Bitmap) intent.getParcelableExtra(AndroidCameraService.CAMERA_DATA);				
			}else{
				bitmap = (Bitmap) intent.getParcelableExtra(CameraService.CAMERA_DATA);
			}
				
			Canvas cover = mCamSV.getHolder().lockCanvas(null);
			if(cover == null) return;
			cover.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear Canvas
			cover.drawColor(Color.WHITE);	
			
			if(bitmap != null && cover != null)
			{
				cover.drawBitmap(bitmap, 0, 0, null);
			}else{
				Log.i("MyLog","FaceTrackActivity: BITMAP NULL");
			}
			
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
	}

	public class FrameOverlayReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","FaceTrackActivity: FrameOverlay onReceive");

		}
	
	}
	
	@Override
	protected void onDestroy() {
		Log.i("MyLog", "FaceTracking Activity onDestroy");

		stopService(new Intent(this, VisionService.class));
		if (VisionConfig.isAndroidCamera)
			stopService(new Intent(this, AndroidCameraService.class));
		else
			stopService(new Intent(this, CameraService.class));
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.face_tracking, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
