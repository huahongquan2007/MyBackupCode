package robotbase.vision;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import robotbase.action.RobotIntent;
import robotbase.vision.camera.CameraService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

public class VisionService extends Service{

	private VisionAlgorithmManager visionManager;
	private VisionAlgorithmManager visionManagerRGB;
	
	private CameraDataReceiver cameraDataReceiver;
//	private RGBCameraDataReceiver rgbCameraDataReceiver;
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
		visionManagerRGB = new VisionAlgorithmManager();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("MyLog", "VisionService onStart");
		
		//=========================================== IMAGE GRAY ====================================
		// Setup visionALgorithm
		FaceDetectionCv faceDetectionCv = new FaceDetectionCv(3, getApplicationContext());
		visionManager.addAlgo(faceDetectionCv);
		// Motion Detection
//		MotionDetection motionDetection = new MotionDetection(30, getApplicationContext());
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
		
		//=========================================== IMAGE RGB ====================================
		// Setup
//		FaceTracking faceTracking = new FaceTracking(10, getApplicationContext());
//		visionManagerRGB.addAlgo(faceTracking);
//		
//		visionManagerRGB.start();
//		rgbCameraDataReceiver = new RGBCameraDataReceiver();		
//		IntentFilter filterVisionRGB;
//		if(VisionConfig.isAndroidCamera){
//			filterVisionRGB = new IntentFilter(AndroidCameraService.CAMERA_INTENT_BITMAP);
//		}else{
//			filterVisionRGB = new IntentFilter(CameraService.CAMERA_INTENT_BITMAP);
//		}
//		registerReceiver(rgbCameraDataReceiver, filterVisionRGB);
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
			visionManager.broadcast();
		}
	}
//	public class RGBCameraDataReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Bitmap data;
//			Mat dataMat = new Mat(), dataRGB = new Mat();
//			if(VisionConfig.isAndroidCamera){
//				data = (Bitmap)intent.getParcelableExtra(AndroidCameraService.CAMERA_DATA);				
//			}else{
//				data = (Bitmap)intent.getParcelableExtra(CameraService.CAMERA_DATA);
//			}
//
//			if(data == null){
//				Log.e("MyLog", "VisionService onReceive NULL DATA");
//				return;
//			}
//			Utils.bitmapToMat(data, dataMat);
//			Imgproc.cvtColor(dataMat, dataRGB, Imgproc.COLOR_RGBA2RGB);
//			
//			visionManagerRGB.updateRGB(dataRGB);
//			visionManagerRGB.broadcast();
//		}
//	}


	@Override
	public void onDestroy() {
		Log.e("MyLog", "VisionService onDestroy");
		unregisterReceiver(cameraDataReceiver);

		visionManager.stop();
		stopSelf();
		super.onDestroy();
	}
}
