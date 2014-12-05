package robotbase.vision;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import robotbase.action.RobotIntent;
import robotbase.utility.Utilities;
import robotbase.vision.BaseCameraService.LocalBinder;
import robotbase.vision.facepp.FaceppAPI;
import robotbase.vision.facepp.FaceppAsyncResponse;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class FaceRecognitionService extends Service implements
		FaceppAsyncResponse {
	private FaceppAPI facepp;
	ListenRecognitionReceiver listenRecognitionReceiver;
	FaceTrackingReceiver faceTrackingReceiver;
	FacialLearningReceiver facialLearningReceiver;
	
	// Lists
	Vector<FaceInfo> broadcastList = new Vector<FaceInfo>();
	Vector<FaceInfo> processList = new Vector<FaceInfo>();
	HashMap<String, Integer> waitList   = new HashMap<String, Integer>();
	HashMap<String, Vector<Bitmap>> waitBitmap  = new HashMap<String, Vector<Bitmap>> ();
	HashMap<String, String > resultMap  = new HashMap<String, String>();
	HashMap<String, Integer> unknownMap = new HashMap<String, Integer>();
	// Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
	Timer timer;
	
	
	long lastFaceTrackingTime = 0;
	long lastPersonAddTime = 0;
	long lastTrainTime = 0;
	long lastAddToWaitList = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		VisionConfig.bindService(this, mConnection);

		faceTrackingReceiver = new FaceTrackingReceiver();
		IntentFilter filterFaceDetection = new IntentFilter(
				RobotIntent.CAM_FACE_TRACKING);
		registerReceiver(faceTrackingReceiver, filterFaceDetection);

		listenRecognitionReceiver = new ListenRecognitionReceiver();
		IntentFilter filterNLP = new IntentFilter(
				RobotIntent.SPEECH_RECOGNITION_NLP);
		registerReceiver(listenRecognitionReceiver, filterNLP);
		
		facialLearningReceiver = new FacialLearningReceiver();
		IntentFilter filterFacialLearn = new IntentFilter(
				RobotIntent.CAM_FACIAL_LEARNING);
		registerReceiver(facialLearningReceiver, filterFacialLearn);
		
		facepp = new FaceppAPI(this);
		super.onCreate();
	}


	class GetImageTask extends TimerTask {
		byte[] data;
		private Mat img = new Mat(VisionConfig.getHeight(),
				VisionConfig.getWidth(), CvType.CV_8UC3);
		private Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(),
				VisionConfig.getHeight(), Config.ARGB_8888);

		@Override
		public void run() {
			if (mCameraService == null) {
				Log.e("MyLog", "FRecService: GetImageTask NULL mCameraService");
				return;
			}
			data = mCameraService.getFrame();
			if (data == null) {
				Log.e("MyLog", "FRecService: GetImageTask NULL DATA");
				return;
			}
					
			img.put(0, 0, data);
			Utils.matToBitmap(img, bitmap);
			
			// Get processInfo
			FaceInfo[] arr = processList.toArray(new FaceInfo[processList.size()]);
			
			broadcastList.clear();
//			Log.i("MyLog", "FRecService: before For " + arr.length);
			for(FaceInfo face : arr){
				if(resultMap.containsKey(face.name)){
//					Log.i("MyLog", "FRecService: result contains " + face.name);
					face.name = resultMap.get(face.name);
					
					boolean foundInBroadcast = false;
					for(FaceInfo t : broadcastList){
						if(t.name.equals(face.name))
							foundInBroadcast = true;
					}
					if(!broadcastList.contains(face) && !foundInBroadcast)
						broadcastList.add(face);
					continue;
				}
				
				if(waitList.containsKey(face.name)){
					Log.i("MyLog", "FRecService: waitList contains " + face.name);
					int curVal = waitList.get(face.name) + 1;
					if(curVal < VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST){
						if(System.currentTimeMillis() - lastAddToWaitList > 1000/VisionConfig.FACE_REG_IDENTIFY_FPS){
							Log.i("MyLog", "FRecService: curVal < VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST: " + curVal +"/" +VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST);
							lastAddToWaitList = System.currentTimeMillis();
							addWaitList(face, bitmap, curVal);
						}					
					}else if(curVal == VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST){
						Log.i("MyLog", "FRecService: sendWaitList(face.name); ");
						waitList.put(face.name, curVal);
						sendWaitList(face.name);
					}else{
						Log.i("MyLog", "FRecService: in waitList. Do nothing ");
					}
				}else{
					Log.i("MyLog", "FRecService: no lists contains " + face.name);
					addWaitList(face, bitmap, 0);
				}
				
				

				continue;
			}
			processList.clear();
			// Broadcast
			if (broadcastList.size() > 0) {
				Bundle result = new Bundle();
				FaceInfo[] faceInfo = broadcastList
						.toArray(new FaceInfo[broadcastList.size()]);
				result.putParcelableArray("data", faceInfo);

				Intent broadcastIntent = new Intent();
				broadcastIntent.putExtra("data", result);
				broadcastIntent.setAction(RobotIntent.CAM_FACE_RECOGNITION);
				sendBroadcast(broadcastIntent);
			}
			
			if(System.currentTimeMillis() - lastFaceTrackingTime > VisionConfig.FACE_REG_TIME_WAIT_TRAIN && lastPersonAddTime > lastTrainTime)
			{
				lastTrainTime = System.currentTimeMillis();
				sendTrainingCommand();
			}
			
		}
	}
	public void sendTrainingCommand(){
		Log.i("MyLog", "FRecService: SendTrainingCommand ");
    	facepp.trainIdentify(VisionConfig.FACE_REG_GROUP_NAME);
	}
	public void addWaitList(FaceInfo face, Bitmap bitmap, int curVal){
		Log.i("MyLog", "FRecService: addWaitListAndSend " + face.name);

		int cX = (int)face.x, cY = (int)face.y, cW = (int)face.w, cH = (int)face.h;
		if((face.x - face.w/3) > 0){
			cX = (int) (face.x - face.w/3);
			cW += 2 * face.w/3;
			if((cX + cW) > bitmap.getWidth()) cW = bitmap.getWidth() - cX - 1;
		}
		if((face.y - face.h/3) > 0){
			cY = (int) (face.y - face.h/3);
			cH += 2 * face.w/3;
			if((cY + cH) > bitmap.getHeight()) cH = bitmap.getHeight() - cY - 1;
		}
		
		Bitmap curImg = Bitmap.createBitmap(bitmap, cX, cY, cW, cH);
		Bitmap faceBitmap = Bitmap.createScaledBitmap(curImg, VisionConfig.FACE_REG_IDENTIFY_WIDTH, VisionConfig.FACE_REG_IDENTIFY_HEIGHT, false);

		if(curVal == 0){
			Vector<Bitmap> vec = new Vector<Bitmap>();
			vec.add(faceBitmap);
			waitBitmap.put(face.name, vec);
		}else{
			Vector<Bitmap> vec2 = waitBitmap.get(face.name);
			vec2.add(faceBitmap);
			waitBitmap.put(face.name, vec2);
		}
		waitList.put(face.name, curVal);	
	}
	public void sendWaitList(String name){
		Vector<Bitmap> vec = waitBitmap.get(name);
		Bitmap sendBitmap = VisionUtilities.combineImages(vec, VisionConfig.FACE_REG_IDENTIFY_WIDTH, VisionConfig.FACE_REG_IDENTIFY_HEIGHT, VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST_WIDTH, VisionConfig.FACE_REG_MAX_IMG_PER_REQUEST_HEIGHT);

		////////////////?????TEST////////////////
		VisionUtilities.saveImage(sendBitmap);
		//////////////?????TEST////////////////
		
		
	    Log.i("MyLog", "FRecService: recognizeFace(facepp.getByteFromBitmap(sendBitmap), name); ");
	    recognizeFace(facepp.getByteFromBitmap(sendBitmap), name);
	}
	public void recognizeFace(byte[] data, String name){
		facepp.recognitionIdentify(VisionConfig.FACE_REG_GROUP_NAME, data , name);
	}
	@Override
	public void processFinish(String input, String output) {
		Log.i("MyLog", "FRecService: ++++ GET RESULT [" + input + "]/[" + output + "]");
		// check if start with unknown
		if(output.startsWith("unknown")){
//			Log.i("MyLog", "FRecService: ++++ GET RESULT: output.startsWith('unknown')");
			if(unknownMap.containsKey(input)){				
				int value = unknownMap.get(input);
				if(value >= VisionConfig.FACE_REG_MAX_TRY - 1){
					Log.i("MyLog", "FRecService: ++++ GET RESULT: value >= VisionConfig.FACE_REG_MAX_TRY");
					unknownMap.remove(input);
					resultMap.put(input, output);
				}else{
					unknownMap.put(input, value + 1);
//					Log.i("MyLog", "FRecService: ++++ GET RESULT: unknownMap.put " + input + " / " + (value + 1) ); 
				}
			}else{
				Log.i("MyLog", "FRecService: ++++ GET RESULT: output.startsWith('unknown') curValue " + 1);
				if(VisionConfig.FACE_REG_MAX_TRY == 1){
					Log.i("MyLog", "FRecService: ++++ GET RESULT: VisionConfig.FACE_REG_MAX_TRY == 1");
					resultMap.put(input, output);
				}else{
					unknownMap.put(input, 1);
				}
				
			}
		}else if(output.startsWith("NO_FACE")){
//			Log.i("MyLog", "FRecService: no face found " + output);
		}else{
			if(unknownMap.containsKey(input)) unknownMap.remove(input);
			resultMap.put(input, output);
//			Log.i("MyLog", "FRecService: not start with unknown " + output);
		}
		// remove from waitList
		waitList.remove(input);
	}

	@Override
	public void trainFinish() {
		Log.i("MyLog", "FRecService: trainFinish : reset resultMap and unknownMap");
		resultMap.clear();
		unknownMap.clear();
	}
	@Override
	public void updateLastPersonAddTime() {
		lastPersonAddTime = System.currentTimeMillis();		
	}
	// FaceTrackingReceiver
	public class FaceTrackingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("data");
			Parcelable[] pc = bundle.getParcelableArray("data");

			if (pc != null) {
				lastFaceTrackingTime = System.currentTimeMillis();
				for (Parcelable face : pc) {
					FaceInfo curFace = (FaceInfo) face;
					processList.add(curFace);
				}
//				Log.i("MyLog", "FRecService: FaceTracking " + pc.length);
			} else {
				Log.i("MyLog", "FRecService: FaceListSize: NULL");
			}
		}

	}
	public class FacialLearningReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog3", "FacialLearningReceiver");
			trainFinish();
		}
		
	}
	
	public class ListenRecognitionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent){
			try {
				String nlp_data = intent.getStringExtra("data");
				Log.i("MyLog", "FRecService: ListenRecognition: onReceive " + nlp_data);
				
				JSONObject NLPObject = Utilities.stringToJSON(nlp_data);
				if (NLPObject.getBoolean("success")) {
					JSONObject expression = NLPObject.getJSONObject("nlp").getJSONObject("expression");

					if (("computer_vision".equals(expression.getString("provider_name")) == false) || ("remember_me".equals(expression.getString("name")) == false)) {
						return;
					}
					
					// Process Command
					Intent facialLearnIntent = new Intent(getBaseContext(),
							robotbase.vision.FacialLearningActivity.class);
					facialLearnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplication().startActivity(facialLearnIntent);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e2){
				e2.printStackTrace();
			}
		}
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
			int fps = mCameraService.getFPS();
			if (fps == 0) {
				fps = 1;
				Log.e("MyLog", "FPS == 0");
			}
			Log.e("MyLog", "FPS == " + fps);
			timer.schedule(new FaceRecognitionService.GetImageTask(), 0,
					(long) (1000 / fps));
		}
	};
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopSelf();
		try {
			Log.i("MyLog", "FRecService: onDestroy");
			VisionConfig.unbindService(this, mConnection);
			timer.cancel();
			unregisterReceiver(listenRecognitionReceiver);
			unregisterReceiver(faceTrackingReceiver);
			unregisterReceiver(facialLearningReceiver);
		} catch (Exception e) {
			Log.e("MyLog", "FRecService: onDestroy Error " + e.toString());
		}
	}
}