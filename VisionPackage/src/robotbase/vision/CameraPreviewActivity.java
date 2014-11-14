package robotbase.vision;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import robotbase.abilities.gallery.GalleryConfig;
import robotbase.action.RobotIntent;
import robotbase.vision.BaseCameraService.LocalBinder;
import robotbase.vision.camera.NativeCameraService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraPreviewActivity extends Activity {
	private SurfaceView mCamSV;
	private TakeAPictureReceiver takeAPictureReceiver;
	private Handler handlerOverlay; // Handler for the separate Thread
	private String DIRECTORY_NAME = GalleryConfig.PHOTO_ALBUM;
	private int modeSV = -1;
	private String textSV = "";
	private float xSV = 0, ySV = 0;
	Paint pt = new Paint();
	private FaceDetectionReceiver faceDetectionReceiver;
	private Handler handleFaceDetection;
	private FaceInfo[] faceInfoList;
	private long faceDetectionTime = 0;
	
	Paint pt2 = new Paint();
	private FaceDetectionReceiver2 faceDetectionReceiver2;
	private Handler handleFaceDetection2;
	private FaceInfo[] faceInfoList2;
	private long faceDetectionTime2 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_preview);
		mCamSV = (SurfaceView) findViewById(R.id.camera_preview_surface_cam);

		mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
				VisionConfig.getHeight());

		Log.i("MyLog", "CAP: onCreate");

		// Setup Bind Service
		VisionConfig.bindService(this, mConnection);
		takeAPictureReceiver = new TakeAPictureReceiver();
		IntentFilter filterOverlayVision = new IntentFilter(
				RobotIntent.CAM_TAKE_PICKTURE);
		HandlerThread handlerThreadOverlay = new HandlerThread(
				"MyNewThreadOverlay");
		handlerThreadOverlay.start();
		Looper looperOverlay = handlerThreadOverlay.getLooper();
		handlerOverlay = new Handler(looperOverlay);
		registerReceiver(takeAPictureReceiver, filterOverlayVision, null,
				handlerOverlay);
		
		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterFaceDetection  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadFaceDetectionOverlay = new HandlerThread(
				"MyNewThreadFaceDetectionOverlay");
		handlerThreadFaceDetectionOverlay.start();
		Looper looperFaceDetectionOverlay = handlerThreadFaceDetectionOverlay.getLooper();
		handleFaceDetection = new Handler(looperFaceDetectionOverlay);
		registerReceiver(faceDetectionReceiver, filterFaceDetection, null,
				handleFaceDetection);
		
		
//		faceDetectionReceiver2 = new FaceDetectionReceiver2();
//		IntentFilter filterFaceDetection2  = new IntentFilter("hhq.face");
//		HandlerThread handlerThreadFaceDetectionOverlay2 = new HandlerThread(
//				"MyNewThreadFaceDetectionOverlay2");
//		handlerThreadFaceDetectionOverlay2.start();
//		Looper looperFaceDetectionOverlay2 = handlerThreadFaceDetectionOverlay2.getLooper();
//		handleFaceDetection2 = new Handler(looperFaceDetectionOverlay2);
//		registerReceiver(faceDetectionReceiver2, filterFaceDetection2, null,
//				handleFaceDetection2);		

				
		pt.setColor(Color.GREEN);
		pt.setTextSize(50);
		pt.setStrokeWidth(3);
		pt.setStyle(Paint.Style.STROKE);
		
		pt2.setColor(Color.BLUE);
		pt2.setTextSize(50);
		pt2.setStrokeWidth(3);
		pt2.setStyle(Paint.Style.STROKE);
	}
	public class FaceDetectionReceiver extends BroadcastReceiver{

		public FaceDetectionReceiver() {

		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("bundle");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if(pc != null)				
			{
				faceInfoList = null;
				faceInfoList = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
				faceDetectionTime = System.currentTimeMillis();
				Log.i("MyLog", "TAP Service: FaceListSize: " + String.valueOf(faceInfoList.length));
			}
			else
			{
				Log.i("MyLog", "TAP Service: FaceListSize: NULL");
			}			
		}
	
	}
	public class FaceDetectionReceiver2 extends BroadcastReceiver{

		public FaceDetectionReceiver2() {

		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("faceDetection");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if(pc != null)				
			{
				faceInfoList2 = null;
				faceInfoList2 = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
				faceDetectionTime2 = System.currentTimeMillis();
				Log.i("MyLog", "TAP Service: FaceListSize: " + String.valueOf(faceInfoList2.length));
			}
			else
			{
				Log.i("MyLog", "TAP Service: FaceListSize: NULL");
			}			
		}
	
	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.i("MyLog", "CAP: onStart");
	}

	@Override
	public void finish() {
		Log.i("MyLog", "CAP: FINISH");
		try{
			unregisterReceiver(takeAPictureReceiver);
			unregisterReceiver(faceDetectionReceiver);
//			unregisterReceiver(faceDetectionReceiver2);
			VisionConfig.unbindService(this, mConnection);
			timer.cancel();
		}catch(Exception e){
			Log.e("MyLog",e.toString());
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {

		
		super.onDestroy();
	}

	public class TakeAPictureReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog", "CAP: FrameOverlay onReceive");

			int mode = intent.getIntExtra("mode", -1);
			switch (mode) {
			case 0:
				float x = intent.getFloatExtra("x", 0);
				float y = intent.getFloatExtra("y", 0);
				Log.i("MyLog", "CAP: FrameOverlay: x = " + String.valueOf(x)
						+ " y = " + String.valueOf(y));
				xSV = x;
				ySV = y;
				modeSV = 0;
				break;
			case 1:
				String text = intent.getStringExtra("text");

				xSV = VisionConfig.getWidth() / 2.0f - 25;
				ySV = VisionConfig.getHeight() / 2.0f + 25;
				textSV = text;
				modeSV = 1;
				break;
			case 2:
				modeSV = 2;
				CameraPreviewActivity.this.onImageCaptured();

				break;
			case 3:
				modeSV = 3;
				Intent intentSP = new Intent();
				intentSP.putExtra("value", intent.getStringExtra("text"));
				intentSP.putExtra("data", fileName);
				intentSP.setAction(RobotIntent.SHARE_PHOTO);
				sendBroadcast(intentSP);
				break;
			default:
				modeSV = -1;
			}
		}

	}

	// Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
	private Timer timer;
	
	private Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(),
			VisionConfig.getHeight(), Bitmap.Config.ARGB_8888);
	private Bitmap image = null;
	private String fileName = null;
	private boolean isCaptured = false;
	private long capturedTime = 0;
	private OutputStream output;
	private SimpleDateFormat df = new SimpleDateFormat(
			"EEE, d MMM yyyy, HH:mm");

	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(CameraPreviewActivity.this,
					"Service is disconnected", Toast.LENGTH_SHORT).show();
			mBounded = false;
			mCameraService = null;

		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(CameraPreviewActivity.this, "Service is connected",
					Toast.LENGTH_SHORT).show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			mCameraService = mLocalBinder.getServerInstance();
			// Setup Timer get Frame
			timer = new Timer();
			timer.schedule(new GetImageTask(), 0,
					1000 / mCameraService.getFPS());
		}
	};

	class GetImageTask extends TimerTask {
		byte[] data;
		private Mat img = new Mat(VisionConfig.getHeight(),
				VisionConfig.getWidth(), CvType.CV_8UC3);
		
		@Override
		public void run() {
			if (mCameraService == null) {
				Log.e("MyLog",
						"CameraPreviewActivity GetImageTask NULL mCameraService");
				return;
			}

			if (isCaptured == false) {
				data = mCameraService.getFrame();
				if (data == null) {
					Log.e("MyLog", "CameraPreviewActivity GetImageTask NULL DATA");
					return;
				}

				img.put(0, 0, data);
				Utils.matToBitmap(img, bitmap);
				data = null;
			} else {
				bitmap = image;
				if (System.currentTimeMillis() - capturedTime > 10000) {
					Log.e("MyLog", "CAP: CAPTURED TIME > 10000");
					CameraPreviewActivity.this.finish();
					return;
				}
			}

			Canvas cover = mCamSV.getHolder().lockCanvas(null);
			if (cover == null)
				return;
			cover.drawColor(Color.TRANSPARENT,
					android.graphics.PorterDuff.Mode.CLEAR); // Clear Canvas
			cover.drawColor(Color.WHITE);

			if (bitmap != null && cover != null) {
				cover.drawBitmap(bitmap, 0, 0, null);
			} else {
				Log.i("MyLog", "CAP: BITMAP NULL");
			}
			// Draw circle
			switch (modeSV) {
			case 0:
				if(faceInfoList != null && (System.currentTimeMillis() - faceDetectionTime) < 1000){
					for(FaceInfo face : faceInfoList){
						Log.i("MyLog", "TAP Service: GetImageTask: " + face.x + " " + face.y + " " + face.w + " " + face.h);
						cover.drawRect(face.x, face.y,face.x + face.w,face.y + face.h,pt);
					}
				}
				if(faceInfoList2 != null && (System.currentTimeMillis() - faceDetectionTime2) < 1000){
					for(FaceInfo face : faceInfoList2){
						Log.i("MyLog", "TAP Service: GetImageTask: " + face.x + " " + face.y + " " + face.w + " " + face.h);
						cover.drawRect(face.x, face.y,face.x + face.w,face.y + face.h,pt2);
					}
				}
				break;
			case 1:
				cover.drawText(textSV, xSV, ySV, pt);
				break;
			}

			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
	}

	public void onImageCaptured() {
		// Save Image
		fileName = saveImage();
		// Show a toast message on successful save
		Toast.makeText(CameraPreviewActivity.this, "Image Saved " + fileName,
				Toast.LENGTH_SHORT).show();
		image = bitmap;
		isCaptured = true;
		capturedTime = System.currentTimeMillis();
	}

	public String saveImage() {
		// Find the SD Card path
		File externalPath = Environment.getExternalStorageDirectory();

		// Create a new folder in SD Card
		File dir = new File(externalPath.getAbsolutePath() + "/" + DIRECTORY_NAME
				+ "/");
		dir.mkdirs();

		String fileName = df.format(Calendar.getInstance().getTime()) + ".png";
		// Create a name for the saved image
		File file = new File(dir, fileName);
		String filePath = file.getAbsolutePath();
		try {
			output = new FileOutputStream(file);
			// Compress into png format image from 0% - 100%
			if (bitmap != null) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			}
			output.flush();
			output.close();
		}

		catch (Exception e) {
			filePath = "SORRY, THERE IS AN ERROR WHILE SAVING!";
			e.printStackTrace();
		}
		return filePath;
	}
}
