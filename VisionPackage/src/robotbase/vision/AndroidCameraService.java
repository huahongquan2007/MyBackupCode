package robotbase.vision;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.IBinder;
import android.util.Log;

public class AndroidCameraService extends Service {
//	public static int camWidth = 320, camHeight = 240;
	public static int camWidth = 240, camHeight = 320;
//	public static int camWidth = 640, camHeight = 480;
	private Camera mCam;
	private Mat pixels;
	private Bitmap bitmap;
	private byte[] pixelsByte;
	private SurfaceTexture texture;
	public static final String CAMERA_INTENT_BITMAP = "robotbase.vision.androidcamera.bitmap";
	public static final String CAMERA_INTENT_BYTE = "robotbase.vision.androidcamera.byte";
	public static final String CAMERA_DATA = "robotbase.vision.androidcamera.data";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onDestroy() {
		mCam.stopPreview();
		mCam.setPreviewCallback(null);
		mCam.release();
		stopSelf();
		super.onDestroy();
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("MyLog", "AndroidCameraService onCreate");
		// Load Native Library
		System.loadLibrary("opencv_java");
		System.loadLibrary(NativeAndroidCamera.name);
	 
		pixels = new Mat(camHeight, camWidth, CvType.CV_8UC4);
		
		bitmap = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888);
		
		try {
			mCam = Camera.open(1);

			int minFps = 0, maxFps = 0;
			for(int i =0; i< mCam.getParameters().getSupportedPreviewFpsRange().size(); i++){
                int[] size = mCam.getParameters().getSupportedPreviewFpsRange().get(i);
                Log.d("MyLog", "supported fps: " + String.valueOf(size[0]) + " " + String.valueOf(size[1]));
            	minFps = size[0]; maxFps = size[1];
                if(size[0] > 10000){
                	break;
                }
            }

			Camera.Parameters p = mCam.getParameters();
			p.setPreviewSize(camHeight, camWidth);
			p.setPreviewFpsRange(minFps, maxFps);
			mCam.setParameters(p);
//			texture = new SurfaceTexture(10);
//			mCam.setPreviewTexture(texture);       
			mCam.startPreview();
//			pixelsByte = new byte[camWidth * camHeight];
			mCam.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.e("MyLog", "AndroidCameraService onPreviewFrame");
//					NativeAndroidCamera.getFrame(data, camWidth, camHeight, pixels.getNativeObjAddr(), pixelsByte);
//					Intent intent = new Intent();
//					intent.putExtra(CAMERA_DATA, pixelsByte);
//					intent.setAction(CAMERA_INTENT_BYTE);
//					sendBroadcast(intent); 				
//					
//					Utils.matToBitmap(pixels, bitmap);
//					Intent intentBM = new Intent();
//					intentBM.putExtra(CAMERA_DATA, bitmap);
//					intentBM.setAction(CAMERA_INTENT_BITMAP);
//					sendBroadcast(intentBM); 				
					
				}});
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
}
