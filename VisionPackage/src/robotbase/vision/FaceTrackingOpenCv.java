package robotbase.vision;

import java.util.Arrays;

import robotbase.action.RobotIntent;
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

public class FaceTrackingOpenCv extends VisionAlgo{
	private FaceDetectionReceiver faceDetectionReceiver;
	private Handler handleFaceDetection;
	private FaceInfo[] faceInfoList = null;
	private long lastProcessTime;
	private int size = 0;
	
	@Override
	protected void setup() {
		Log.e("MyLog", "FaceTrackingOpenCv setup");
		System.loadLibrary(NativeFaceTracking.name);
		
		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterFaceDetection  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		HandlerThread handlerThreadFaceDetectionOverlay = new HandlerThread(
				"MyNewThreadFaceDetectionOverlay");
		handlerThreadFaceDetectionOverlay.start();
		Looper looperFaceDetectionOverlay = handlerThreadFaceDetectionOverlay.getLooper();
		handleFaceDetection = new Handler(looperFaceDetectionOverlay);
		getApplicationContext().registerReceiver(faceDetectionReceiver, filterFaceDetection, null,
				handleFaceDetection);	
	}

	@Override
	protected void update(byte[] frame) {
		if(faceInfoList != null && faceInfoList.length > 0){
			size = NativeFaceTracking.getPos(frame, frameWidth, frameHeight);
			lastProcessTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void broadcast() {
		if(size == 0 ) return;
		try{
			if(System.currentTimeMillis() - lastProcessTime < 1000){
				Log.e("MyLog", "FaceTrackingOpenCv broadcast");
				Intent intent = new Intent();
				intent.putExtra("data", getResultBundle());
				intent.setAction(RobotIntent.CAM_FACE_TRACKING);
				getApplicationContext().sendBroadcast(intent); 				
			}
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}

	@Override
	protected Bundle getResultBundle() {
		Bundle result = new Bundle();

		FaceInfo[] faceInfo = NativeFaceTracking.getResult();
		result.putParcelableArray("data", faceInfo);

		return result;
	}

	public class FaceDetectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try{
				Bundle bundle = intent.getBundleExtra("bundle");
				Parcelable[] pc = bundle.getParcelableArray("data");
				if (pc != null) {
					faceInfoList = null;
					faceInfoList = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
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
			}catch(Exception e){
				Log.i("MyLog", "FaceTracking: FaceDetectionReceiver " + e.toString());
			}
		}
	}
}
