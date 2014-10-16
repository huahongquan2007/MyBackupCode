package robotbase.vision;

import robotbase.action.RobotIntent;
import robotbase.vision.camera.CameraService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class VisionService extends Service{

	private VisionAlgorithmManager visionManager;
	private CameraDataReceiver cameraDataReceiver;
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
//		System.loadLibrary(NativeMotionDetection.name);
		System.loadLibrary(NativeFaceDetection.name);
		
		if(VisionConfig.isAndroidCamera){
			System.loadLibrary(NativeAndroidCamera.name);
		}
		
		// visionManager
		visionManager = new VisionAlgorithmManager();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("MyLog", "VisionService onStart");
		// Setup visionALgorithm
		FaceDetectionCv faceDetectionCv = new FaceDetectionCv(3, getApplicationContext());
		visionManager.addAlgo(faceDetectionCv);
		// Motion Detection
//		MotionDetection motionDetection = new MotionDetection(30);
//		visionManager.addAlgo(motionDetection);
		
		visionManager.start();
		
		// End setup visionAlgorithm
		cameraDataReceiver = new CameraDataReceiver();
		
		IntentFilter filterVision;
		if(VisionConfig.isAndroidCamera){
			filterVision = new IntentFilter(AndroidCameraService.CAMERA_INTENT_BYTE);
		}else{
			filterVision = new IntentFilter(CameraService.CAMERA_INTENT_BYTE);
		}

		registerReceiver(cameraDataReceiver, filterVision);
	}
	public class CameraDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] data;
			if(VisionConfig.isAndroidCamera){
				data = intent.getByteArrayExtra(AndroidCameraService.CAMERA_DATA);				
			}else{
				data = intent.getByteArrayExtra(CameraService.CAMERA_DATA);
			}

			if(data == null){
				Log.e("MyLog", "VisionService onReceive NULL DATA");
				return;
			}
			visionManager.update(data);
			visionDataBroadcast();
		}
	}

	private void visionDataBroadcast() {
		// Motion Detection
//		MotionDetection md = (MotionDetection)visionManager.getAlgo("MotionDetection");

		try{
			FaceDetectionCv fd = (FaceDetectionCv)visionManager.getAlgo("FaceDetectionCv");
			Intent intent = new Intent();
			intent.putExtra("faceDetection", fd.getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_DETECTION);
			sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}
	@Override
	public void onDestroy() {
		Log.e("MyLog", "VisionService onDestroy");
		unregisterReceiver(cameraDataReceiver);

		visionManager.stop();
		stopSelf();
		super.onDestroy();
	}
}
