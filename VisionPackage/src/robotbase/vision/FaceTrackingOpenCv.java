package robotbase.vision;

import java.util.Arrays;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import robotbase.action.RobotIntent;
import robotbase.vision.CameraPreviewActivity.FaceDetectionReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;

public class FaceTrackingOpenCv extends VisionAlgorithm {
	private FaceDetectionReceiver faceDetectionReceiver;
	private Handler handleFaceDetection;
	private FaceInfo[] faceInfoList = null;
	private long faceDetectionTime = 0;
	private int size = 0;
	public FaceTrackingOpenCv(float fps, Context ctx) {
		super(fps, ctx);
	}

	@Override
	public void start() {
		Log.e("MyLog", "FaceTrackingOpenCv start");
		System.loadLibrary(NativeFaceTracking.name);
		
		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterFaceDetection  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadFaceDetectionOverlay = new HandlerThread(
				"MyNewThreadFaceDetectionOverlay");
		handlerThreadFaceDetectionOverlay.start();
		Looper looperFaceDetectionOverlay = handlerThreadFaceDetectionOverlay.getLooper();
		handleFaceDetection = new Handler(looperFaceDetectionOverlay);
		context.registerReceiver(faceDetectionReceiver, filterFaceDetection, null,
				handleFaceDetection);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(byte[] frame) {
		size = NativeFaceTracking.getPos(frame, frameWidth, frameHeight);
	}

	@Override
	public Bundle getResultBundle() {
		Bundle result = new Bundle();
//		FaceInfo[] faceInfo = new FaceInfo[size];
//		int[] xArr, yArr, wArr, hArr;
//		xArr = new int [size];
//		yArr = new int [size];
//		wArr = new int [size];
//		hArr = new int [size];
//		String[] nameArr;
		FaceInfo[] faceInfo = NativeFaceTracking.getResult();
		for (int i = 0; i < size; i++) {
//			faceInfo[i] = new FaceInfo(xArr[i], yArr[i], wArr[i], hArr[i],time);
		}
		result.putParcelableArray("data", faceInfo);

		return result;
	}

	@Override
	public void broadcast() {
		if(size == 0 ) return;
		try{
			Log.e("MyLog", "FaceTrackingOpenCv broadcast");
			Intent intent = new Intent();
			intent.putExtra("data", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_TRACKING);
			context.sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}

	public class FaceDetectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("bundle");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if (pc != null) {
				faceInfoList = null;
				faceInfoList = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
				faceDetectionTime = System.currentTimeMillis();
				int[] xArr, yArr , wArr, hArr;
				int len = faceInfoList.length;
				xArr = new int [len];
				yArr = new int [len];
				wArr = new int [len];
				hArr = new int [len];
				for(int idx = 0 ; idx < len; idx++ ){
					xArr[idx] = (int) faceInfoList[idx].x;
					yArr[idx] = (int) faceInfoList[idx].y;
					wArr[idx] = (int) faceInfoList[idx].w;
					hArr[idx] = (int) faceInfoList[idx].h;
				}
				NativeFaceTracking.setFaceDetection(len, xArr, yArr, wArr, hArr);
			} else {
				Log.i("MyLog", "FaceTracking CV: FaceListSize: NULL");
			}
		}
	}
}
