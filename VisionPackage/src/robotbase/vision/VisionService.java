package robotbase.vision;

import java.util.Timer;
import java.util.TimerTask;

import robotbase.vision.BaseCameraService.LocalBinder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class VisionService extends Service{
	private Timer timer;

	private VisionAlgorithmManager visionManager;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {		
		super.onCreate();
		
		Log.e("MyLog", "VisionService onCreate");
		// Load Native Library
		System.loadLibrary("opencv_java");		

		// visionManager
		visionManager = new VisionAlgorithmManager();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("MyLog", "VisionService onStart");
		
		//=========================================== IMAGE GRAY ====================================
		// Setup visionALgorithm
//		FaceDetectionCv faceDetectionCv = new FaceDetectionCv(10, getApplicationContext());
//		visionManager.addAlgo(faceDetectionCv);
		FaceDetectionFacepp faceDetectionFacepp = new FaceDetectionFacepp(10, getApplicationContext());
		visionManager.addAlgo(faceDetectionFacepp);
//		FaceTrackingOpenCv faceTracking = new FaceTrackingOpenCv(3, getApplicationContext());
//		visionManager.addAlgo(faceTracking);
		// Motion Detection
//		MotionDetection motionDetection = new MotionDetection(30, getApplicationContext());
//		visionManager.addAlgo(motionDetection);
		
		visionManager.start();
		// End setup visionAlgorithm
		
		
		// Setup Bind Service
		VisionConfig.bindService(this, mConnection);
	}
	
	//Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(VisionService.this, "Service is disconnected", 1000)
					.show();
			mBounded = false;
			mCameraService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(VisionService.this, "Service is connected", 1000)
					.show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			mCameraService = mLocalBinder.getServerInstance();
			// Setup Timer get Frame
			timer = new Timer();
			int fps = mCameraService.getFPS();
			if(fps == 0){
				Log.e("MyLog","FPS == 0");
			}
			Log.e("MyLog","FPS == " + fps);
			timer.schedule(new VisionService.GetImageTask(), 0, (long)(1000/fps));
		}
	};
	class GetImageTask extends TimerTask {
		byte[] data;
		@Override
		public void run() {
			if(mCameraService == null){
				Log.e("MyLog", "VisionService GetImageTask NULL mCameraService");
				return;
			}
			data = mCameraService.getFrame();
			if(data == null){
				Log.e("MyLog", "VisionService GetImageTask NULL DATA");
				return;
			}
			visionManager.update(data);
			visionManager.broadcast();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("MyLog", "VisionService onDestroy stop");
		visionManager.stop();
		VisionConfig.unbindService(this, mConnection);
		timer.cancel();
		stopSelf();
	}
}