package robotbase.vision;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import robotbase.action.NeckInstruction;
import robotbase.action.NeckIntent;
import robotbase.action.RobotIntent;
import robotbase.action.kobuki.KobukiCommand;
import robotbase.utility.Notification;
import robotbase.utility.Utilities;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class TakePictureService extends Service {
	private FaceDetectionReceiver faceDetectionReceiver;
	private ListenRecognitionReceiver listenRecognitionReceiver;
	private SpeechRecognitionReceiver speechRecognitionReceiver;
	private float minCenterSize = VisionConfig.getWidth() / 10;
	
	public enum SERVICE_STATE {
		START, FIND_FACE, STOP, ROTATE, CENTER, CAPTURE, WAIT_PROCESS, RESET
	};
	private SERVICE_STATE curState;
	
	public enum WAIT_STATE {
		NONE, SHARE
	};
	private WAIT_STATE curWaitState;
	private String curWaitCommand = null;
	
	private Vector<FaceInfo> faceInfo = new Vector<FaceInfo>();
	private Timer timer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("MyLog", "TAP Service: onCreate");
		faceDetectionReceiver = new FaceDetectionReceiver();
		IntentFilter filterFaceDetection = new IntentFilter(
				RobotIntent.CAM_FACE_DETECTION);
		registerReceiver(faceDetectionReceiver, filterFaceDetection);

		listenRecognitionReceiver = new ListenRecognitionReceiver();
		IntentFilter filterListenVision = new IntentFilter(
				RobotIntent.SPEECH_RECOGNITION_NLP);

		registerReceiver(listenRecognitionReceiver, filterListenVision);

		speechRecognitionReceiver = new SpeechRecognitionReceiver();
		IntentFilter filterSpeechToText = new IntentFilter(
				RobotIntent.SPEECH_RECOGNITION_TEXT);
		registerReceiver(speechRecognitionReceiver, filterSpeechToText);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		sendBroadcast(new NeckIntent(NeckInstruction.PAN_JOINT_ID, 2, RobotIntent.NECK_SET_SPEED).data);
		sendBroadcast(new NeckIntent(NeckInstruction.TILT_JOINT_ID, 2, RobotIntent.NECK_SET_SPEED).data);
		
		resetState();
		timer = new Timer();
		timer.schedule(new TakePictureTask(), 0, 1000 / 20);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i("MyLog", "TAP Service: onDestroy");
		timer.cancel();
		unregisterReceiver(faceDetectionReceiver);
		unregisterReceiver(listenRecognitionReceiver);
		unregisterReceiver(speechRecognitionReceiver);
		stopSelf();
	}

	// FaceDetectionReceiver
	public class FaceDetectionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("bundle");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if (pc != null) {
				faceInfo.clear();
				for (Parcelable face : pc) {
					faceInfo.add((FaceInfo) face);
				}
			} else {
				Log.i("MyLog", "TAP Service: FaceListSize: NULL");
			}
		}

	}

	// NLP Receiver
	public class ListenRecognitionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog", "TAP Service: ListenRecognition: onReceive");

			if (RobotIntent.SPEECH_RECOGNITION_NLP.equals(intent.getAction()
					.toString())) {
				String nlp_data = intent.getStringExtra("data");

				try {
					JSONObject NLPObject = Utilities.stringToJSON(nlp_data);
					if (NLPObject.getBoolean("success")) {

						JSONObject nlp = NLPObject.getJSONObject("nlp");
						JSONObject expression = nlp.getJSONObject("expression");
						String keywords = expression.getString("keywords");

						Log.d("MyLog", "TAP Service: NLP+ " + keywords);
						nlpCheckTakeAPicture(expression, "computer_vision",
								"take_a_picture");
						String value = nlp.getJSONObject("params").getJSONObject("network").getString("value");
						nlpCheckShareThisPhoto(expression, "computer_vision", "share_this_photo", value);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}

		private void nlpCheckTakeAPicture(JSONObject expression,
				String provider_name, String expression_name)
				throws JSONException {
			if ((provider_name.equals(expression.getString("provider_name")) == false)) {
				return;
			}
			if ((expression_name.equals(expression.getString("name")) == false)) {
				return;
			}

			// Process Command
			if (curState != SERVICE_STATE.START) {
				curState = SERVICE_STATE.START;
				Intent dialogIntent = new Intent(getBaseContext(),
						robotbase.vision.CameraPreviewActivity.class);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(dialogIntent);
			}
		}
		private void nlpCheckShareThisPhoto(JSONObject expression,
				String provider_name, String expression_name, String value)
				throws JSONException {
			if ((provider_name.equals(expression.getString("provider_name")) == false)) {
				return;
			}
			if ((expression_name.equals(expression.getString("name")) == false)) {
				return;
			}

			// Process Command
			if (curState == SERVICE_STATE.WAIT_PROCESS) {
				curWaitState = WAIT_STATE.SHARE;
				curWaitCommand = value;
				Log.i("MyLog","TAP Service: NLP COMMAND SHARE");
			}
		}
	}

	// SpeechRecognition Receiver
	public class SpeechRecognitionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	class TakePictureTask extends TimerTask {
		private long countDownTime = 0;
		private long lastDetectionTime = 0;
		private long startWaitProcessTime = 0;
		String countText = "";

		@Override
		public void run() {
			if (curState != null) {
//				if( (curState == SERVICE_STATE.ROTATE || curState == SERVICE_STATE.CENTER) && faceInfo.isEmpty()){
//					Log.i("MyLog", "TAP Service: CurState: Empty->FIND_FACE");
//					curState = SERVICE_STATE.FIND_FACE;
//				}
				
				switch (curState) {
				case START:
					Log.i("MyLog", "TAP Service: CurState: START");
					curState = SERVICE_STATE.FIND_FACE;
					countText = "zero";
					// Send Speech
					
					Notification.voiceNotification(getApplicationContext(), "Let's take a picture.");

					break;
				case FIND_FACE:
					Log.i("MyLog", "TAP Service: CurState: FIND_FACE");
					// Neu chua co mat -> rotate
					// Neu co mat -> state = ROTATE
					if(!faceInfo.isEmpty())
						curState = SERVICE_STATE.ROTATE;
					else{
						VisionHelperCenterOnTargets.rotate(getApplicationContext());
					}
					break;
				case ROTATE:
					Log.i("MyLog", "TAP Service: CurState: ROTATE");
					
					float x = 0,
					y = 0;
					long faceTime = 0;
					for (FaceInfo face : faceInfo) {
						x += face.x + face.w / 2;
						y += face.y + face.h / 2;
						faceTime = face.time;
					}
					
					if (faceTime <= lastDetectionTime)
						break;
					else
						lastDetectionTime = faceTime;
					
					if (faceInfo.size() > 0) {
						x = x / faceInfo.size();
						y = y / faceInfo.size();
						Intent targetIntent = new Intent();
						targetIntent.putExtra("mode", 0);
						targetIntent.putExtra("x", x);
						targetIntent.putExtra("y", y);
						targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
						sendBroadcast(targetIntent);
						boolean isCenter = VisionHelperCenterOnTargets.run(
								getApplicationContext(), x, y);
						if (isCenter) {
							// Next State
							curState = SERVICE_STATE.CENTER;
							countDownTime = System.currentTimeMillis();
							Log.i("MyLog",
									"TAP Service: CurState: ROTATE -> CAPTURE");
						} else {
							Log.i("MyLog", "CenterOnTarget isCenter FALSE");
						}
					}
					break;
				case CENTER:
					// If size > minSize
					float avgSize = 0;
					long lastFaceTime = 0;
					for (FaceInfo face : faceInfo) {
						avgSize += (face.h + face.w) / 2;
						lastFaceTime = face.time;
					}
					avgSize = avgSize / faceInfo.size();
					if (lastFaceTime <= lastDetectionTime)
						break;
					else
						lastDetectionTime = lastFaceTime;
					
					if(avgSize > minCenterSize)
						curState = SERVICE_STATE.CAPTURE;
					else{
						VisionHelperCenterOnTargets.move(getApplicationContext(), KobukiCommand.FORWARD.ordinal());
						curState = SERVICE_STATE.ROTATE;
					}
					break;
				case CAPTURE:
					long curTime = System.currentTimeMillis();
					int countSecond = Math
							.round((curTime - countDownTime) / 1500);

					Log.i("MyLog",
							"TAP Service: CurState: CAPTURE: "
									+ String.valueOf(countSecond)
									+ " CurrentMillis: "
									+ String.valueOf(curTime) + " CountTime: "
									+ String.valueOf(countDownTime));

					Intent targetIntent = new Intent();
					boolean speak = false;
					switch (countSecond) {
					case 0:
						if (countText.equals("zero")) {
							countText = "three";
							speak = true;
						}
						break;
					case 1:
						if (countText.equals("three")) {
							countText = "two";
							speak = true;
						}
						break;
					case 2:
						if (countText.equals("two")) {
							countText = "one";
							speak = true;
						}
						break;
					case 3:
						targetIntent.putExtra("mode", 2);
						// Next State
						curState = SERVICE_STATE.WAIT_PROCESS;
						startWaitProcessTime = System.currentTimeMillis();
						// Send Speech
						Notification.voiceNotification(getApplicationContext(), "OK. I took your photo.");

						break;
					}
					if (countSecond < 3) {
						targetIntent.putExtra("mode", 1);
						targetIntent.putExtra("text",
								String.valueOf(3 - countSecond));

						if (speak) {
							Notification.voiceNotification(getApplicationContext(), countText);
						}
					}

					targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
					sendBroadcast(targetIntent);
					break;
				case WAIT_PROCESS:
					Intent waitProcessIntent = new Intent();
					switch (curWaitState) {
					case SHARE:
						Log.i("MyLog","TAP WAIT PROCESS: Send MODE 3");
						waitProcessIntent.putExtra("mode", 3);
						waitProcessIntent.putExtra("text", curWaitCommand);
						curState = SERVICE_STATE.RESET;
						break;
					default:
						break;
					}
					waitProcessIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
					sendBroadcast(waitProcessIntent);
					if(System.currentTimeMillis() - startWaitProcessTime > 10000)
						curState = SERVICE_STATE.RESET;
					break;
				case RESET:
					resetState();
					break;
				case STOP:
					break;
				default:
					break;
				}

				faceInfo.clear();
			}

		}
	}

	// Utilities
	private void resetState() {
		curState = SERVICE_STATE.STOP;
		curWaitState = WAIT_STATE.NONE;
		VisionHelperCenterOnTargets.reset(this);
	}
}