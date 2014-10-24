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
import robotbase.vision.TrackingTLDActivity.FaceDetectionReceiver;
import robotbase.vision.TrackingTLDActivity.FrameDataReceiver;
import robotbase.vision.camera.CameraService;
import robotbase.vision.tracking_tld.Tld;
import robotbase.vision.tracking_tld.Tld.ProcessFrameStruct;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class FaceTracking extends VisionAlgorithm {
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
	private Handler handlerDetection; // Handler for the separate Thread
	private float x, y, w, h;
	private int count = 0;
	private long time;

	// run
	Bitmap bitmap = Bitmap.createBitmap(AndroidCameraService.camWidth,
			AndroidCameraService.camHeight, Bitmap.Config.ARGB_8888);

	public FaceTracking(float fps, Context ctx) {
		super(fps, ctx);
	}

	@Override
	public void start() {
		Log.i("MyLog", "FaceTracking: start");
		_workingFrame = new Mat();
		_currentGray = new Mat();
		_lastGray = new Mat();

		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterOverlayVision = new IntentFilter(
				RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadOverlay = new HandlerThread(
				"MyNewThreadOverlay");
		handlerThreadOverlay.start();
		handlerDetection = new Handler(handlerThreadOverlay.getLooper());
		context.registerReceiver(faceDetectionReceiver, filterOverlayVision,
				null, handlerDetection);

		// TLD:
		initTLD(context);
	}

	public class FaceDetectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle fdBundle = intent.getBundleExtra("faceDetection");
			Parcelable[] faceInfo = fdBundle.getParcelableArray("data");
			for (int i = 0; i < faceInfo.length; i++) {
				FaceInfo f = (FaceInfo) faceInfo[i];
			}
			if (faceInfo.length > 0) {
				FaceInfo f = (FaceInfo) faceInfo[0];
				x = f.x;
				y = f.y;
				w = f.w;
				h = f.h;
				time = f.time;
				count = 1;
				if (_trackedBox == null) {
					_trackedBox = new Rect((int) x, (int) y, (int) w, (int) h);
					rectTrack = new android.graphics.Rect((int) x, (int) y,
							(int) (x + w), (int) (y + h));
					_tld = null;
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

		rectPaint = new Paint();
		rectPaint.setColor(Color.rgb(0, 255, 0));
		rectPaint.setStrokeWidth(5);
		rectPaint.setStyle(Style.STROKE);

	}

	@Override
	public void stop() {
		context.unregisterReceiver(faceDetectionReceiver);
	}

	@Override
	public void run(byte[] frame) {
		// Computation
		Mat originalFrame = new Mat(AndroidCameraService.camHeight,
				AndroidCameraService.camWidth, CvType.CV_8UC1);
		originalFrame.put(0, 0, frame);

		try {
			// Image is too big and this requires too much CPU for a phone, so
			// scale everything down...
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
						// // start from scratch, you have to select an init box
						// again !
						_trackedBox = null;
						_tld = null;
						throw eInit; // re-throw it as it will be dealt with
										// later
					}
				} else {
					_currentGray = _workingFrame.clone();
					Log.i("MyLog",
							"TLD: Tracking Current(" + _currentGray.cols()
									+ ", " + _currentGray.rows() + " | Last("
									+ _lastGray.cols() + ", "
									+ _lastGray.rows());

					_processFrameStruct = _tld.processFrame(_lastGray,
							_currentGray);
					drawPoints(originalFrame, _processFrameStruct.lastPoints,
							workingRatio, new Scalar(255, 0, 0));
					drawPoints(originalFrame,
							_processFrameStruct.currentPoints, workingRatio,
							new Scalar(0, 255, 0));
					drawBox(originalFrame,
							scaleUp(_processFrameStruct.currentBBox,
									workingRatio), new Scalar(0, 0, 255));
					Rect opencv_rect = scaleUp(_processFrameStruct.currentBBox,
							workingRatio);
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
			_errMessage = e.getClass().getSimpleName() + " / " + e.getMessage();
			Log.e("MyLog", "TLDView PROBLEM", e);
		}

		Utils.matToBitmap(originalFrame, bitmap);
	}

	@Override
	public void runRGB(Mat frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getResultBundle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void broadcast() {
		Intent intent = new Intent();
		intent.putExtra("data", rectTrack);
		intent.setAction(RobotIntent.CAM_FACE_TRACKING);
		context.sendBroadcast(intent);
	}
	
	// HELPER
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
