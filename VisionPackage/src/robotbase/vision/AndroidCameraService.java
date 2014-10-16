package robotbase.vision;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.IBinder;
import android.util.Log;

public class AndroidCameraService extends Service {
	public static int camWidth = 320, camHeight = 240;
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
		super.onDestroy();
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("MyLog", "AndroidCameraService onCreate");
		// Load Native Library
		System.loadLibrary("opencv_java");
				 
		pixels = new Mat(camHeight, camWidth, CvType.CV_8UC4);
		bitmap = Bitmap.createBitmap(camWidth, camHeight, Bitmap.Config.ARGB_8888);
		try {
			mCam = Camera.open(1);
		
			Camera.Parameters p = mCam.getParameters();
			p.setPreviewSize(camWidth, camHeight);
			p.setPreviewFpsRange(30000, 30000);
			mCam.setParameters(p);
						
			texture = new SurfaceTexture(10);
			mCam.setPreviewTexture(texture);       
			mCam.startPreview();
			mCam.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
//					Log.e("MyLog", "AndroidCameraService onPreviewFrame");
					pixelsByte = new byte[camWidth * camHeight];
					NativeAndroidCamera.getFrame(data, camWidth, camHeight, pixels.getNativeObjAddr(), pixelsByte);

					Intent intent = new Intent();
					intent.putExtra(CAMERA_DATA, pixelsByte);
					intent.setAction(CAMERA_INTENT_BYTE);
					sendBroadcast(intent); 				
					
					Utils.matToBitmap(pixels, bitmap);
					Intent intentBM = new Intent();
					intentBM.putExtra(CAMERA_DATA, bitmap);
					intentBM.setAction(CAMERA_INTENT_BITMAP);
					sendBroadcast(intentBM); 				
					
				}});
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
}
