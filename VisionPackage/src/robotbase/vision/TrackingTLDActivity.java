package robotbase.vision;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import robotbase.action.RobotIntent;
import robotbase.vision.camera.CameraService;
import robotbase.vision.tracking_tld.Tld;
import robotbase.vision.tracking_tld.Tld.ProcessFrameStruct;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class TrackingTLDActivity extends Activity {
	private SurfaceView mCamSV;
	private FrameDataReceiver frameDataReceiver;
	private Handler handler;

	// TLD:
	private ProcessFrameStruct _processFrameStruct = null;
	private Properties _tldProperties;
	private Tld _tld = null;
	private String _errMessage;
    private int _canvasImgYOffset;
    private int _canvasImgXOffset;
	private Rect _trackedBox = null;
	private Paint rectPaint;	
	private android.graphics.Rect rectTrack;

	private static final Size WORKING_FRAME_SIZE = new Size(144, 80);
	private Mat _workingFrame;
	private Mat _currentGray;
	private Mat _lastGray;
	private FaceDetectionReceiver faceDetectionReceiver;
	private Handler handlerOverlay; // Handler for the separate Thread
	private float x,y,w,h;
	private int count = 0;
	private long time;
	@Override
	protected void onDestroy() {
		stopService(new Intent(this, VisionService.class));
		if (VisionConfig.isAndroidCamera)
			stopService(new Intent(this, AndroidCameraService.class));
		else
			stopService(new Intent(this, CameraService.class));
		unregisterReceiver(faceDetectionReceiver);
		unregisterReceiver(frameDataReceiver);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_tld);

		Log.i("MyLog", "TrackingTLDActivity: onCreate");

		System.loadLibrary("opencv_java");
		_workingFrame = new Mat();
		_currentGray = new Mat();
		_lastGray = new Mat();
		mCamSV = (SurfaceView) findViewById(R.id.tld_surface_view);
//		mCamSV.getHolder().setFixedSize(AndroidCameraService.camWidth,
//				AndroidCameraService.camHeight);

		// Service
		if (VisionConfig.isAndroidCamera)
			startService(new Intent(this, AndroidCameraService.class));
		else
			startService(new Intent(this, CameraService.class));
		startService(new Intent(this, VisionService.class));
		
		frameDataReceiver = new FrameDataReceiver();
		IntentFilter filterVision = (VisionConfig.isAndroidCamera) ? new IntentFilter(
				AndroidCameraService.CAMERA_INTENT_BYTE) : new IntentFilter(
				CameraService.CAMERA_INTENT_BYTE);

		registerReceiver(frameDataReceiver, filterVision);
		
		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterOverlayVision  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadOverlay = new HandlerThread("MyNewThreadOverlay");
		handlerThreadOverlay.start();
		Looper looperOverlay = handlerThreadOverlay.getLooper();
		handlerOverlay = new Handler(looperOverlay);
		registerReceiver (faceDetectionReceiver, filterOverlayVision, null, handlerOverlay);
		
		// TLD:
		initTLD(this);
	}
	public class FaceDetectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle fdBundle = intent.getBundleExtra("faceDetection");
			Parcelable[] faceInfo = fdBundle.getParcelableArray("data");
			for (int i = 0; i < faceInfo.length; i++) {
				FaceInfo f = (FaceInfo)faceInfo[i];
//				Log.i("MyLog","FaceTracking: X: " + String.valueOf(f.x) + " Y: " + String.valueOf(f.y) );
			}
			if(faceInfo.length > 0){
				FaceInfo f = (FaceInfo)faceInfo[0];
				x = f.x;
				y = f.y;
				w = f.w;
				h = f.h;
				time = f.time;
				count = 1;
				if(_trackedBox == null){
//					_trackedBox = new Rect((int)x,(int)y,(int)w,(int)h);
//					rectTrack = new android.graphics.Rect((int)x , (int)y, (int)(x + w), (int)(y + h));
//					_tld = null;
				}
			}
			
		}
	}
	private void initTLD(Context context) {
		// Init the PROPERTIES
		InputStream propsIS = null;
		try {
			propsIS = context.getResources().openRawResource(
					R.raw.vision_tld_parameters);
			_tldProperties = new Properties();
			_tldProperties.load(propsIS);
		} catch (IOException e) {
			Log.e("MyLog", "Can't load properties", e);
		} finally {
			if (propsIS != null) {
				try {
					propsIS.close();
				} catch (IOException e) {
					Log.e("MyLog", "Can't close props", e);
				}
			}
		}

		// LISTEN for touches of the screen, to define the BOX to be tracked
		final AtomicReference<Point> trackedBox1stCorner = new AtomicReference<Point>();
		rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(0, 255, 0));
		rectPaint.setStrokeWidth(5);
		rectPaint.setStyle(Style.STROKE);

		mCamSV.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// re-init
				_errMessage = null;
				_tld = null;
				Point corner = new Point((int)(event.getX() - _canvasImgXOffset), (int)(event.getY()	- _canvasImgYOffset));
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					trackedBox1stCorner.set(corner);
					Log.i("MyLog", "Touch: 1st corner: " + corner);
					break;
				case MotionEvent.ACTION_UP:
					_trackedBox = new Rect(trackedBox1stCorner.get(), corner);
					Log.i("MyLog", "Touch: Tracked box DEFINED: " + _trackedBox);
					break;
				case MotionEvent.ACTION_MOVE:
					Log.i("MyLog", "Touch: Tracked box MOVE: ");
					rectTrack = new android.graphics.Rect(
							(int) trackedBox1stCorner.get().x + _canvasImgXOffset,
							(int) trackedBox1stCorner.get().y + _canvasImgYOffset, 
							(int) corner.x	+ _canvasImgXOffset, 
							(int) corner.y	+ _canvasImgYOffset);

					break;
				}
				return true;
			}
		});
	}

	public class FrameDataReceiver extends BroadcastReceiver {
		byte[] mData = null;
		Bitmap bitmap = Bitmap.createBitmap(AndroidCameraService.camWidth, AndroidCameraService.camHeight, Bitmap.Config.ARGB_8888);
		private long previousTime = 0;
		@Override
		public void onReceive(Context context, Intent intent) {
			if (System.currentTimeMillis() - previousTime == 0)
			return;
			float fps = 1000 / (System.currentTimeMillis() - previousTime);
			previousTime = System.currentTimeMillis();
			
			
			Log.i("MyLog", "TrackingTLDActivity: FrameData onReceive");

			if (VisionConfig.isAndroidCamera) {
				mData = intent
						.getByteArrayExtra(AndroidCameraService.CAMERA_DATA);
			} else {
				mData = intent
						.getByteArrayExtra(CameraService.CAMERA_DATA);
			}
			
			// Computation
			Mat originalFrame = new Mat(AndroidCameraService.camHeight, AndroidCameraService.camWidth, CvType.CV_8UC1); 
			originalFrame.put(0, 0, mData);
			
			try{
				// Image is too big and this requires too much CPU for a phone, so scale everything down...
				Imgproc.resize(originalFrame, _workingFrame, WORKING_FRAME_SIZE);
				final Size workingRatio = new Size(originalFrame.width() / WORKING_FRAME_SIZE.width, originalFrame.height() / WORKING_FRAME_SIZE.height);
//				// usefull to see what we're actually working with...
				_workingFrame.copyTo(originalFrame.submat(originalFrame.rows() - _workingFrame.rows(), originalFrame.rows(), 0, _workingFrame.cols()));
//				
				if(_trackedBox != null){
					if(_tld == null){ // run the 1st time only
						_lastGray = _workingFrame.clone();
						_tld = new Tld(_tldProperties);
						Rect scaledDownTrackedBox = scaleDown(_trackedBox, workingRatio);
						Log.i("MyLog", "Working Ration: " + workingRatio + " / Tracking Box: " + _trackedBox + " / Scaled down to: " + scaledDownTrackedBox);
						try {
							_tld.init(_lastGray, scaledDownTrackedBox);
						}catch(Exception eInit){
//					        // start from scratch, you have to select an init box again !
							_trackedBox = null;
							_tld = null;
							throw eInit; // re-throw it as it will be dealt with later
						}
					}else{
						_currentGray = _workingFrame.clone();
						Log.i("MyLog", "TLD: Tracking Current(" + _currentGray.cols() + ", " + _currentGray.rows() + " | Last(" + _lastGray.cols() + ", " + _lastGray.rows());

						_processFrameStruct = _tld.processFrame(_lastGray, _currentGray);
						drawPoints(originalFrame, _processFrameStruct.lastPoints, workingRatio, new Scalar(255, 0, 0));
						drawPoints(originalFrame, _processFrameStruct.currentPoints, workingRatio, new Scalar(0, 255, 0));
						drawBox(originalFrame, scaleUp(_processFrameStruct.currentBBox, workingRatio), new Scalar(0, 0, 255));
						Rect opencv_rect = scaleUp(_processFrameStruct.currentBBox, workingRatio);
						if(opencv_rect != null)
						{
							rectTrack.set(opencv_rect.x, opencv_rect.y, opencv_rect.x + opencv_rect.width, opencv_rect.y + opencv_rect.height);
							if(opencv_rect.width > AndroidCameraService.camWidth){
								Log.i("MyLog","INVALID OPENCV RECT");
								_trackedBox = null;
								_tld = null;
							}
						}
//							
						_currentGray.copyTo(_lastGray);
//						
//						// overlay the current positive examples on the real image(needs converting at the same time !)
//						copyTo(_tld.getPPatterns(), originalFrame);
					}
				}
			} catch(Exception e) {
		        _errMessage = e.getClass().getSimpleName() + " / " + e.getMessage();
		        Log.e("MyLog", "TLDView PROBLEM", e);
			}
			
			Utils.matToBitmap(originalFrame, bitmap);
			Canvas cover = mCamSV.getHolder().lockCanvas(null);
			if (cover == null)
				return;
			cover.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR); // Clear Canvas
			cover.drawColor(Color.WHITE);
			if (bitmap != null && cover != null) {
				cover.drawBitmap(bitmap, 0, 0, null);
			} else {
				Log.i("MyLog", "TrackingTLDActivity: BITMAP NULL");
			}
			if(rectTrack != null)
				cover.drawRect(rectTrack, rectPaint);
						
			Paint pt = new Paint();
			pt.setColor(Color.RED);
			pt.setTextSize(30);
			pt.setStrokeWidth(3);
			pt.setStyle(Paint.Style.STROKE);
			
			cover.drawText("FPS " + String.valueOf((float) (fps)), 30, 470, pt);
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
	}
	private static void drawPoints(Mat image, final Point[] points, final Size scale, final Scalar colour){
		if(points != null){
			for(Point point : points){
				Core.circle(image, scaleUp(point, scale), 2, colour);
			}
		}
	}
	
	private static void drawBox(Mat image, final Rect box, final Scalar colour){
		if(box != null){
			Core.rectangle(image, box.tl(), box.br(), colour);
		}
	}
	
	
	/* SCALING */
	
	private static Point scaleUp(Point point, Size scale){
		if(point == null || scale == null) return null;
		return new Point(point.x * scale.width, point.y * scale.height);
	}
	
	private static Point scaleDown(Point point, Size scale){
		if(point == null || scale == null) return null;
		return new Point(point.x / scale.width, point.y / scale.height);
	}
	
	private static Rect scaleUp(Rect rect, Size scale) {
		if(rect == null || scale == null) return null;
		return new Rect(scaleUp(rect.tl(), scale), scaleUp(rect.br(), scale));
	}
	
	private static Rect scaleDown(Rect rect, Size scale) {
		if(rect == null || scale == null) return null;
		return new Rect(scaleDown(rect.tl(), scale), scaleDown(rect.br(), scale));
	}
}
