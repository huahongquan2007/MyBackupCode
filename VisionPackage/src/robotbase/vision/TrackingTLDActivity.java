package robotbase.vision;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
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
import robotbase.vision.BaseCameraService.LocalBinder;
import robotbase.vision.CameraPreviewActivity.GetImageTask;
import robotbase.vision.TakePictureService.TakePictureTask;
import robotbase.vision.camera.CameraService;
import robotbase.vision.tracking_tld.Tld;
import robotbase.vision.tracking_tld.Tld.ProcessFrameStruct;
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
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class TrackingTLDActivity extends Activity {
	private SurfaceView mCamSV;
	private Vector<FaceInfo> faceInfo = new Vector<FaceInfo>();
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

	private static final Size WORKING_FRAME_SIZE = new Size(80, 144);
	private Mat _workingFrame;
	private Mat _currentGray;
	private Mat _lastGray;
	private FaceDetectionReceiver faceDetectionReceiver;
	private Handler handlerOverlay; // Handler for the separate Thread
	private float x, y, w, h;
	private int count = 0;
	private long time;

	// Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
	private Timer timer;
	private Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(),
			VisionConfig.getHeight(), Bitmap.Config.ARGB_8888);
	
	// Tracking
	boolean isTracking = false;
	
	@Override
	protected void onDestroy() {
		stopService(new Intent(this, VisionService.class));
		VisionConfig.stopService(this);
		VisionConfig.unbindService(this, mConnection);
		unregisterReceiver(faceDetectionReceiver);
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
		// TLD:
		initTLD(this);

		// Service
		VisionConfig.startService(this);
		// Setup Bind Service
		VisionConfig.bindService(this, mConnection);

		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterOverlayVision = new IntentFilter(
				RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadOverlay = new HandlerThread(
				"MyNewThreadOverlay");
		handlerThreadOverlay.start();
		Looper looperOverlay = handlerThreadOverlay.getLooper();
		handlerOverlay = new Handler(looperOverlay);
		registerReceiver(faceDetectionReceiver, filterOverlayVision, null,
				handlerOverlay);

	}

	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			mBounded = false;
			mCameraService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			mCameraService = mLocalBinder.getServerInstance();
			// Setup Timer get Frame
			timer = new Timer();
			timer.schedule(new GetImageTask(), 0,
					1000 / mCameraService.getFPS());
		}
	};

	public class FaceDetectionReceiver extends BroadcastReceiver {
		int delay = 0;
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("bundle");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if (pc != null) {
				faceInfo.clear();
				for (Parcelable face : pc) {
					faceInfo.add((FaceInfo) face);
				}
				if(isTracking == false && faceInfo.size() > 0){
					// First Face
					FaceInfo firstFace = faceInfo.firstElement();
					
					if(delay < 5){
						delay++;
						return;
					}
					
					_trackedBox = new Rect((int)firstFace.x, (int)firstFace.y, (int)firstFace.w, (int)firstFace.h);
					rectTrack = new android.graphics.Rect(
							(int) firstFace.x
							,
							(int) firstFace.y
							, (int) firstFace.x + (int)firstFace.w
							, (int) firstFace.y + (int)firstFace.h
							);
					
					isTracking = true;
				}

			} else {
				Log.i("MyLog", "TLD: FaceListSize: NULL");
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
				Point corner = new Point(
						(int) (event.getX() - _canvasImgXOffset), (int) (event
								.getY() - _canvasImgYOffset));
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
							(int) trackedBox1stCorner.get().x
									+ _canvasImgXOffset,
							(int) trackedBox1stCorner.get().y
									+ _canvasImgYOffset, (int) corner.x
									+ _canvasImgXOffset, (int) corner.y
									+ _canvasImgYOffset);

					break;
				}
				return true;
			}
		});
	}

	private class GetImageTask extends TimerTask {
		byte[] data;
		private Mat img = new Mat(VisionConfig.getHeight(),
				VisionConfig.getWidth(), CvType.CV_8UC3);
		private Mat originalFrame = new Mat(VisionConfig.getHeight(),
				VisionConfig.getWidth(), CvType.CV_8UC1);
		@Override
		public void run() {
			if (mCameraService == null) {
				Log.e("MyLog",
						"CameraPreviewActivity GetImageTask NULL mCameraService");
				return;
			}
			data = mCameraService.getFrame();
			if (data == null) {
				Log.e("MyLog",
						"CameraPreviewActivity GetImageTask NULL DATA");
				return;
			}
			img.put(0, 0, data);
//			Utils.matToBitmap(originalFrame, bitmap);
			data = null;
			
			///////////////////// TRACKING PART /////////////////////////
			Imgproc.cvtColor(img, originalFrame, Imgproc.COLOR_RGB2GRAY);
			try {
				// Image is too big and this requires too much CPU for a phone,
				// so scale everything down...
				Imgproc.resize(originalFrame, _workingFrame, WORKING_FRAME_SIZE);
				final Size workingRatio = new Size(originalFrame.width()
						/ WORKING_FRAME_SIZE.width, originalFrame.height()
						/ WORKING_FRAME_SIZE.height);
				// // usefull to see what we're actually working with...
				_workingFrame.copyTo(originalFrame.submat(originalFrame.rows()
						- _workingFrame.rows(), originalFrame.rows(), 0,
						_workingFrame.cols()));
				//
				if (_trackedBox != null) {
					if (_tld == null) { // run the 1st time only
						_lastGray = _workingFrame.clone();
						_tld = new Tld(_tldProperties);
						Rect scaledDownTrackedBox = scaleDown(_trackedBox,
								workingRatio);
						Log.i("MyLog", "Working Ration: " + workingRatio
								+ " / Tracking Box: " + _trackedBox
								+ " / Scaled down to: " + scaledDownTrackedBox);
						try {
							_tld.init(_lastGray, scaledDownTrackedBox);
						} catch (Exception eInit) {
							// // start from scratch, you have to select an init
							// box again !
							_trackedBox = null;
							_tld = null;
							throw eInit; // re-throw it as it will be dealt with
											// later
						}
					} else {
						_currentGray = _workingFrame.clone();
						Log.i("MyLog",
								"TLD: Tracking Current(" + _currentGray.cols()
										+ ", " + _currentGray.rows()
										+ " | Last(" + _lastGray.cols() + ", "
										+ _lastGray.rows());

						_processFrameStruct = _tld.processFrame(_lastGray,
								_currentGray);
						drawPoints(originalFrame,
								_processFrameStruct.lastPoints, workingRatio,
								new Scalar(255, 0, 0));
						drawPoints(originalFrame,
								_processFrameStruct.currentPoints,
								workingRatio, new Scalar(0, 255, 0));
						drawBox(originalFrame,
								scaleUp(_processFrameStruct.currentBBox,
										workingRatio), new Scalar(0, 0, 255));
						Rect opencv_rect = scaleUp(
								_processFrameStruct.currentBBox, workingRatio);
						if (opencv_rect != null) {
							rectTrack.set(opencv_rect.x, opencv_rect.y,
									opencv_rect.x + opencv_rect.width,
									opencv_rect.y + opencv_rect.height);
							if (opencv_rect.width > AndroidCameraService.camWidth) {
								Log.i("MyLog", "INVALID OPENCV RECT");
								_trackedBox = null;
								_tld = null;
							}
						}
						//
						_currentGray.copyTo(_lastGray);
						//
						// // overlay the current positive examples on the real
						// image(needs converting at the same time !)
						// copyTo(_tld.getPPatterns(), originalFrame);
					}
				}
			} catch (Exception e) {
				_errMessage = e.getClass().getSimpleName() + " / "
						+ e.getMessage();
				Log.e("MyLog", "TLDView PROBLEM", e);
			}

			Utils.matToBitmap(originalFrame, bitmap);
			
			///////////////////// TRACKING PART /////////////////////////			
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
			if (rectTrack != null)
				cover.drawRect(rectTrack, rectPaint);
			else
				Log.e("MyLog", "TLD: rectTrack NULL");
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
	}

	private static void drawPoints(Mat image, final Point[] points,
			final Size scale, final Scalar colour) {
		if (points != null) {
			for (Point point : points) {
				Core.circle(image, scaleUp(point, scale), 2, colour);
			}
		}
	}

	private static void drawBox(Mat image, final Rect box, final Scalar colour) {
		if (box != null) {
			Core.rectangle(image, box.tl(), box.br(), colour);
		}
	}

	/* SCALING */

	private static Point scaleUp(Point point, Size scale) {
		if (point == null || scale == null)
			return null;
		return new Point(point.x * scale.width, point.y * scale.height);
	}

	private static Point scaleDown(Point point, Size scale) {
		if (point == null || scale == null)
			return null;
		return new Point(point.x / scale.width, point.y / scale.height);
	}

	private static Rect scaleUp(Rect rect, Size scale) {
		if (rect == null || scale == null)
			return null;
		return new Rect(scaleUp(rect.tl(), scale), scaleUp(rect.br(), scale));
	}

	private static Rect scaleDown(Rect rect, Size scale) {
		if (rect == null || scale == null)
			return null;
		return new Rect(scaleDown(rect.tl(), scale),
				scaleDown(rect.br(), scale));
	}
}