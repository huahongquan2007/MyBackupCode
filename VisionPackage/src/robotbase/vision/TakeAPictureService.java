//package robotbase.vision;
//import java.util.Arrays;
//import org.json.JSONException;
//import org.json.JSONObject;
//import robotbase.action.RobotIntent;
//import robotbase.utility.Utilities;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Parcelable;
//import android.util.Log;
//
//public class TakeAPictureService extends Service{
//	
//	private FaceDetectionReceiver faceDetectionReceiver;
//	private ListenRecognitionReceiver listenRecognitionReceiver;
//	private SpeechRecognitionReceiver speechRecognitionReceiver;
//	private enum ANSWER_STATE { NOT_STARTED, NOT_RECEIVED, YES, NO };
//	private ANSWER_STATE curAnswerState = ANSWER_STATE.NOT_STARTED;
//	public enum SERVICE_STATE {START, STOP, ROTATE, CAPTURE, WAIT_SHARE};
//	private SERVICE_STATE curState = SERVICE_STATE.STOP;
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void onCreate() {
//		Log.e("MyLog", "TAP Service: onCreate");
//		super.onCreate();
//	}
//
//	@Override
//	public void onStart(Intent intent, int startId) {
//		Log.i("MyLog", "TAP Service: onStart");
//		faceDetectionReceiver = new FaceDetectionReceiver();
//		IntentFilter filterFaceDetection  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
//		registerReceiver(faceDetectionReceiver, filterFaceDetection);
//	
//		listenRecognitionReceiver = new ListenRecognitionReceiver();
//		IntentFilter filterListenVision  = new IntentFilter(RobotIntent.SPEECH_RECOGNITION_NLP);
//		
//		registerReceiver(listenRecognitionReceiver, filterListenVision);
//		
//		speechRecognitionReceiver = new SpeechRecognitionReceiver();
//        IntentFilter filterSpeechToText = new IntentFilter(RobotIntent.SPEECH_RECOGNITION_TEXT); 
//		registerReceiver(speechRecognitionReceiver, filterSpeechToText); 
//	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		
//		Log.i("MyLog", "TAP Service: onDestroy");
//		unregisterReceiver(faceDetectionReceiver);
//		unregisterReceiver(listenRecognitionReceiver);
//		unregisterReceiver(speechRecognitionReceiver);
//		stopSelf();
//	}
//	public class SpeechRecognitionReceiver extends BroadcastReceiver{
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (RobotIntent.SPEECH_RECOGNITION_TEXT.equals(intent.getAction().toString())) { 
//				String[] textMessage = intent.getStringArrayExtra("data");
//				if(curAnswerState == ANSWER_STATE.NOT_RECEIVED){
//					for(int i =0; i < textMessage.length ; i++){
//						if(Utilities.findMe("yes",  textMessage[i])){
//							curAnswerState = ANSWER_STATE.YES;
//							return ;
//						}
//						if(Utilities.findMe("no",  textMessage[i])){
//							curAnswerState = ANSWER_STATE.NO;
//							return ;
//						}
//					}					
//				}
//			}
//		}
//	}
//	public class ListenRecognitionReceiver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Log.i("MyLog", "TAP Service: ListenRecognition: onReceive");
//						
//			if (RobotIntent.SPEECH_RECOGNITION_NLP.equals(intent
//					.getAction().toString())) { 
//				String nlp_data = intent.getStringExtra("data"); 
//				
//				try {
//					JSONObject  NLPObject = Utilities.stringToJSON(nlp_data);
//					if (NLPObject.getBoolean("success")){
//						
//						JSONObject nlp= NLPObject.getJSONObject("nlp");
//						
//						JSONObject  expression = nlp.getJSONObject("expression");
//						String keywords = expression.getString("keywords");
//						Log.d("MyLog", "TAP Service: NLP+ " + keywords);
//						if( ("computer_vision".equals(expression.getString("provider_name")) == false)){
//							return;
//						}
//						if( ("take_a_picture".equals(expression.getString("name")) == false)){
//							return;
//						}
//						// Process Command
//						curState = SERVICE_STATE.START;
//						Intent dialogIntent = new Intent(getBaseContext(), robotbase.vision.CameraPreviewActivity.class);
//						dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						getApplication().startActivity(dialogIntent);
//					} 
//					
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//				 
//			}
//
//		}
//	}
//	public class FaceDetectionReceiver extends BroadcastReceiver{
//		private long countDownTime = 0;
//		private long lastDetectionTime = 0;
//		String countText;
//		
//		public FaceDetectionReceiver() {
//		}
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
////			Log.i("MyLog", "TAP Service: FaceDetection: onReceive");
//			Bundle bundle = intent.getBundleExtra("bundle");
//			FaceInfo[] faceInfoList = null;
//			Parcelable[] pc = bundle.getParcelableArray("data");
//			if(pc != null)				
//			{
//				faceInfoList = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
////				Log.i("MyLog", "TAP Service: FaceListSize: " + String.valueOf(faceInfoList.length));
//			}
//			else
//			{
//				Log.i("MyLog", "TAP Service: FaceListSize: NULL");
//			}
//			switch (curState) {
//			case START:
//				Log.i("MyLog", "TAP Service: CurState: START");
//				curState = SERVICE_STATE.ROTATE;
//				countText = "zero";
//				// Send Speech
//				Intent intentTTSSTART = new Intent();
//				Bundle bundleTTSSTART = new Bundle();
//				bundleTTSSTART.putString("text", "Let's take a picture.");
//				intentTTSSTART.putExtras(bundleTTSSTART);
//				intentTTSSTART.setAction(RobotIntent.TEXT_TO_SPEECH);
//				sendBroadcast(intentTTSSTART);
//				break;
//			case ROTATE:
//				Log.i("MyLog", "TAP Service: CurState: ROTATE");
//				float x = 0, y = 0;
//				long faceTime = 0;
//				for(FaceInfo face : faceInfoList){
//					x += face.x + face.w / 2; 
//					y += face.y + face.h / 2;
//					faceTime = face.time;
//				}
//				if(faceTime <= lastDetectionTime) break;
//				
//				if(faceInfoList.length > 0)
//				{
//					x = x / faceInfoList.length; y = y / faceInfoList.length;
//					Intent targetIntent = new Intent();
//					targetIntent.putExtra("mode", 0);
//					targetIntent.putExtra("x", x);
//					targetIntent.putExtra("y", y);
//					targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
//					sendBroadcast(targetIntent);					
//					boolean isCenter = VisionHelperCenterOnTargets.run(context, x , y);
//					if (isCenter){
//						curState = SERVICE_STATE.CAPTURE;
//						countDownTime = System.currentTimeMillis();
//						Log.i("MyLog", "TAP Service: CurState: ROTATE -> CAPTURE");
//					}else{
//						Log.i("MyLog", "CenterOnTarget isCenter FALSE");
//					}
//				}
//				break;
//			case CAPTURE:
//				long curTime = System.currentTimeMillis();
//				int countSecond = Math.round((curTime - countDownTime) / 1500 );
//				
//				Log.i("MyLog", "TAP Service: CurState: CAPTURE: " + String.valueOf(countSecond) + " CurrentMillis: " + String.valueOf(curTime) + " CountTime: " + String.valueOf(countDownTime));
//				
//				Intent targetIntent = new Intent();
//				boolean speak = false;
//				switch(countSecond){
//				case 0:
//					if(countText.equals("zero")){
//						countText = "three";
//						speak = true;
//					}
//					break;
//				case 1:
//					if(countText.equals("three")) {
//						countText = "two";
//						speak = true;
//					}
//					break;
//				case 2:
//					if(countText.equals("two")){
//						countText = "one";
//						speak = true;
//					}
//					break;
//				case 3:
////					String filePath = frameHighQualityReceiver.saveImage();
//					targetIntent.putExtra("mode", 2);
//					// Save Image
////					targetIntent.putExtra("text", filePath);
//					// Reset State
//					curState = SERVICE_STATE.WAIT_SHARE;
//					curAnswerState = ANSWER_STATE.NOT_RECEIVED;
//					// Send Speech
//					Intent intentTTSOK = new Intent();
//					Bundle bundleTTSOK = new Bundle();
//					bundleTTSOK.putString("text", "OK. I took your photo.");
//					intentTTSOK.putExtras(bundleTTSOK);
//					intentTTSOK.setAction(RobotIntent.TEXT_TO_SPEECH);
//					sendBroadcast(intentTTSOK);
//					break;
//				}
//				if(countSecond < 3){
//					targetIntent.putExtra("mode", 1);
//					targetIntent.putExtra("text", String.valueOf(3 - countSecond));
//					
//					if(speak){
//						Intent intentTTS = new Intent();
//						Bundle bundleTTS = new Bundle();
//						Log.e("MyLog","TTS: onSend " + countText);
//						bundleTTS.putString("text", countText);
//						intentTTS.putExtras(bundleTTS);
//						intentTTS.setAction(RobotIntent.TEXT_TO_SPEECH);
//						sendBroadcast(intentTTS);	
//					}
//				}
//
//				targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
//				sendBroadcast(targetIntent);
//			case WAIT_SHARE:
//				if(curAnswerState == ANSWER_STATE.YES){
//					// Send Speech
//					Intent intentTTSOK = new Intent();
//					Bundle bundleTTSOK = new Bundle();
//					bundleTTSOK.putString("text", "OK. I shared your photo.");
//					intentTTSOK.putExtras(bundleTTSOK);
//					intentTTSOK.setAction(RobotIntent.TEXT_TO_SPEECH);
//					sendBroadcast(intentTTSOK);
//					curState = SERVICE_STATE.STOP;
//					curAnswerState = ANSWER_STATE.NOT_STARTED;
//					targetIntent = new Intent();
//					targetIntent.putExtra("mode", 3);					
//					targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
//					sendBroadcast(targetIntent);
//				}
//				break;
//			default:
//				break;
//			}
//		}
//
//	}
//
//}