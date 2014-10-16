package robotbase.vision;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import robotbase.abilities.gallery.GalleryConfig;
import robotbase.action.NeckIntent;
import robotbase.action.RobotIntent;
import robotbase.utility.Utilities;
import robotbase.vision.camera.CameraService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;

public class TakeAPictureService extends Service{
	
	private FaceDetectionReceiver faceDetectionReceiver;
	private ListenRecognitionReceiver listenRecognitionReceiver;
	private FrameHighQualityReceiver frameHighQualityReceiver;
	public enum SERVICE_STATE {START, STOP, ROTATE, CAPTURE};
	private SERVICE_STATE curState = SERVICE_STATE.STOP;
	private String DIRECTORY_NAME = GalleryConfig.PHOTO_ALBUM;
	private Handler handlerHQImage; // Handler for the separate Thread
		
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.e("MyLog", "TAP Service: onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("MyLog", "TAP Service: onStart");
		faceDetectionReceiver = new FaceDetectionReceiver(this);
		IntentFilter filterFaceDetection  = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		registerReceiver(faceDetectionReceiver, filterFaceDetection);

		String MY_SPEECH_RECOGNITION_NLP = "robotbase.vision.NLP";
		
		listenRecognitionReceiver = new ListenRecognitionReceiver();
//			IntentFilter filterListenVision  = new IntentFilter(MY_SPEECH_RECOGNITION_NLP);
		IntentFilter filterListenVision  = new IntentFilter(RobotIntent.SPEECH_RECOGNITION_NLP);
		
		registerReceiver(listenRecognitionReceiver, filterListenVision);

		frameHighQualityReceiver = new FrameHighQualityReceiver();
		IntentFilter filterHighQualityImage;
		if(VisionConfig.isAndroidCamera){
			filterHighQualityImage = new IntentFilter(AndroidCameraService.CAMERA_INTENT_BITMAP);
		}else{
			filterHighQualityImage = new IntentFilter(CameraService.CAMERA_INTENT_BITMAP);
		}
		//registerReceiver(frameHighQualityReceiver, filterHighQualityImage);
		// Using Handler
		HandlerThread handlerThread = new HandlerThread("MyNewThread");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();
		// Create a handler for the service
		handlerHQImage = new Handler(looper);
		// Register the broadcast receiver to run on the separate Thread
		registerReceiver (frameHighQualityReceiver, filterHighQualityImage, null, handlerHQImage);
		
	
	}
	
	@Override
	public void onDestroy() {
		Log.i("MyLog", "TAP Service: onDestroy");
		unregisterReceiver(faceDetectionReceiver);
		unregisterReceiver(listenRecognitionReceiver);
		unregisterReceiver(frameHighQualityReceiver);
		super.onDestroy();
	}
	public class ListenRecognitionReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog", "TAP Service: ListenRecognition: onReceive");
			
//			Log.e("MyNLP", "NLP onReceive TAP");
//			String data = intent.getStringExtra("data");
//			if(data.equals("take_a_picture")){
//				curState = SERVICE_STATE.START;
//				Intent dialogIntent = new Intent(getBaseContext(), robotbase.vision.CameraPreviewActivity.class);
//				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				getApplication().startActivity(dialogIntent);				
//			}else{
//				Log.e("MyLog"," NLP ERROR : " + data);
//			}
//			return;
			
			// End hardcode
			
			if (RobotIntent.SPEECH_RECOGNITION_NLP.equals(intent
					.getAction().toString())) { 
				String nlp_data = intent.getStringExtra("data"); 
				
				try {
					JSONObject  NLPObject = Utilities.stringToJSON(nlp_data);
					if (NLPObject.getBoolean("success")){
						
						JSONObject nlp= NLPObject.getJSONObject("nlp");
						
						JSONObject  expression = nlp.getJSONObject("expression");
						String keywords = expression.getString("keywords");
						Log.d("MyLog", "TAP Service: NLP+ " + keywords);
						if( ("computer_vision".equals(expression.getString("provider_name")) == false)){
							return;
						}
						if( ("take_a_picture".equals(expression.getString("name")) == false)){
							return;
						}
						// Process Command
						curState = SERVICE_STATE.START;
						Intent dialogIntent = new Intent(getBaseContext(), robotbase.vision.CameraPreviewActivity.class);
						dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplication().startActivity(dialogIntent);
					} 
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				 
			}

		}
	}
	public class FaceDetectionReceiver extends BroadcastReceiver{
		private Context context;
		private long countDownTime = 0;
		private long lastDetectionTime = 0;
		private int lastCount = -1;
		public FaceDetectionReceiver(Context _context) {
			context = _context;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i("MyLog", "TAP Service: FaceDetection: onReceive");
			Bundle bundle = intent.getBundleExtra("faceDetection");
			FaceInfo[] faceInfoList = null;
			Parcelable[] pc = bundle.getParcelableArray("data");
			if(pc != null)				
			{
				faceInfoList = Arrays.copyOf(pc, pc.length, FaceInfo[].class);
//				Log.i("MyLog", "TAP Service: FaceListSize: " + String.valueOf(faceInfoList.length));

			}
			else
			{
				Log.i("MyLog", "TAP Service: FaceListSize: NULL");
			}
			switch (curState) {
			case START:
				Log.i("MyLog", "TAP Service: CurState: START");
				curState = SERVICE_STATE.ROTATE;
				context.sendBroadcast(new NeckIntent(NeckIntent.PAN_JOINT_ID, 4).speed_intent);
				context.sendBroadcast(new NeckIntent(NeckIntent.TILT_JOINT_ID, 4).speed_intent);
						
				break;
			case ROTATE:
				Log.i("MyLog", "TAP Service: CurState: ROTATE");
				float x = 0, y = 0;
				long faceTime = 0;
				for(FaceInfo face : faceInfoList){
					x += face.x + face.w / 2; 
					y += face.y + face.h / 2;
					faceTime = face.time;
				}
				if(faceTime <= lastDetectionTime) break;
				
				if(faceInfoList.length > 0)
				{
					x = x / faceInfoList.length; y = y / faceInfoList.length;
					Intent targetIntent = new Intent();
					targetIntent.putExtra("mode", 0);
					targetIntent.putExtra("x", x);
					targetIntent.putExtra("y", y);
					targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
					sendBroadcast(targetIntent);					
					boolean isCenter = VisionHelperCenterOnTargets.run(context, x , y);
					if (isCenter){
						curState = SERVICE_STATE.CAPTURE;
						countDownTime = System.currentTimeMillis();
						Log.i("MyLog", "TAP Service: CurState: ROTATE -> CAPTURE");
					}
				}
				break;
			case CAPTURE:
				long curTime = System.currentTimeMillis();
				int countSecond = Math.round((curTime - countDownTime) / 2000 );
				
				Log.i("MyLog", "TAP Service: CurState: CAPTURE: " + String.valueOf(countSecond) + " CurrentMillis: " + String.valueOf(curTime) + " CountTime: " + String.valueOf(countDownTime));
				Intent targetIntent = new Intent();
				// Countdown 3 2 1!
				if(countSecond < 3){
					targetIntent.putExtra("mode", 1);
					targetIntent.putExtra("text", String.valueOf(3 - countSecond));
					
					String countText = "bug";
					boolean countTextActivate = false;			
					if(countSecond == 0)
					{
						if(lastCount == -1){
							countText = "three";
							countTextActivate = true;
							lastCount = 0;
						}
					}
					else if(countSecond < 2)
					{
						if(lastCount == 0){
							countText = "two";
							countTextActivate = true;
							lastCount = 1;
						}
					}
					else{
						if(lastCount == 1){
							countText = "one";
							countTextActivate = true;
							lastCount = 2;
						}
					}
					if(countTextActivate){
						Intent intentTTS = new Intent();
						Bundle bundleTTS = new Bundle();
						Log.e("MyLog","TTS: onSend " + countText);
						bundleTTS.putString("text", countText);
						intentTTS.putExtras(bundleTTS);
						intentTTS.setAction(RobotIntent.TEXT_TO_SPEECH);
						sendBroadcast(intentTTS);
					}

				}
				else if(countSecond == 3){
					String filePath = frameHighQualityReceiver.saveImage();
					targetIntent.putExtra("mode", 2);
					// Save Image
					targetIntent.putExtra("text", filePath);
					// Reset State
					curState = SERVICE_STATE.STOP;
					// Send Speech
					Intent intentTTS = new Intent();
					Bundle bundleTTS = new Bundle();
					bundleTTS.putString("text", "OK. I take your photo.");
					intentTTS.putExtras(bundleTTS);
					intentTTS.setAction(RobotIntent.TEXT_TO_SPEECH);
					sendBroadcast(intentTTS);
				}
				else{
					targetIntent.putExtra("mode", 1);
					targetIntent.putExtra("text", String.valueOf(3 - countSecond));
				}
				targetIntent.setAction(RobotIntent.CAM_TAKE_PICKTURE);
				sendBroadcast(targetIntent);


			default:
				break;
			}
		}
		
	}
	public class FrameHighQualityReceiver extends BroadcastReceiver {
		Bitmap bitmap = null;
        OutputStream output;
        SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
		@Override
		public void onReceive(Context context, Intent intent) {
			if(VisionConfig.isAndroidCamera){
				bitmap = (Bitmap) intent.getParcelableExtra(AndroidCameraService.CAMERA_DATA);				
			}else{
				bitmap = (Bitmap) intent.getParcelableExtra(CameraService.CAMERA_DATA);
			}
		}
		public String saveImage(){
			 // Find the SD Card path
            File filepath = Environment.getExternalStorageDirectory();
 
            // Create a new folder in SD Card
            File dir = new File(filepath.getAbsolutePath() + "/" + DIRECTORY_NAME + "/");
            dir.mkdirs();
 
            String fileName = df.format(Calendar.getInstance().getTime()) + ".png";
            // Create a name for the saved image
            File file = new File(dir, fileName);
            String filePath = file.getAbsolutePath();
            try {

                output = new FileOutputStream(file);
 
                // Compress into png format image from 0% - 100%
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
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
}