package robotbase.vision;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import robotbase.action.NeckInstruction;
import robotbase.action.NeckIntent;
import robotbase.action.RobotIntent;
import robotbase.action.kobuki.KobukiCommand;
import robotbase.vision.BaseCameraService.LocalBinder;
import robotbase.vision.facepp.FaceppAPI;
import robotbase.vision.facepp.FaceppAsyncResponse;
import android.app.Activity;
import android.app.Notification;
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
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FacialLearningActivity extends Activity{
	private boolean sendServer = VisionConfig.FACE_REG_SEND_SERVER;
	
	public enum STATE{START, WAIT_PERMISSION, ASK_NAME, WAIT_NAME, WAIT_ONE_FACE, NOTIFY_START, SAVE_PICTURE, ROTATE_CENTER, MOVE, ASK_FINISH, WAIT_FINISH, STOP};
	private STATE curState = STATE.START;
	
	private enum WAIT_STATE{STOP, NO_FACE, ONE_FACE, TOO_MANY_FACES};
	private WAIT_STATE curWaitState = WAIT_STATE.STOP;
	
	private int curWaitTry = 0;
	
	private FaceRecognitionReceiver faceRecognitionReceiver;
	private FaceTrackingReceiver faceTrackingReceiver;
	private SurfaceView mCamSV;
	private Button enterBtn, finishBtn, finishBtn2;
	private EditText textBox;
	// Connection to Bind
	private boolean mBounded;
	private BaseCameraService mCameraService;
	private Timer timer;
	private String inputName = "";
	private Paint textPt, imgPt, textTrackingPt;
	
	Vector<FaceInfo> faceTrackingInfo = new Vector<FaceInfo>();
	
	Vector<FaceInfo> faceInfo = new Vector<FaceInfo>();
	Vector<Bitmap> imgVector = new Vector<Bitmap>();

	// Notification String
	String[] waitPermissionString = {"Hello, I need permission"};
	String[] yourNameString = {"Hello, What's your name?", "Hi, What's ya name?", "Hi, May I ask your name?", "Hi, How may I call your name?", "Hi, How may I call you?"};
	String[] notifyString = {"OK, Please look at me"};
	String[] ambiguousString = {"Such an ambiguos situation.", "Too many unknown faces"};
	String[] noFaceString = {"Hey Where are you?.", "I can't find you."};
	String[] askFinishString = {"Ok. Finish?.", "Ok. Done?."};
	
	private FaceppAPI facepp;
	
	private int isFinish = 0;
	private long lastAddTime = 0;
	private long lastMoveTime = 0;
	private boolean isMove = false;
	private boolean isWaitPermission = false;
	private long lastRotateTime = 0;
	private long lastReceiveFaceTime = 0;
	private long lastReceiveFaceTrackingTime = 0;
	private long lastProcessTime = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_facial_learning);
	    
		facepp = new FaceppAPI();
		
		mCamSV = (SurfaceView) findViewById(R.id.cameraView);
		mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
				VisionConfig.getHeight());
		textBox = (EditText) findViewById(R.id.nameTextBox);
		
		enterBtn = (Button) findViewById(R.id.enterBtn);
		enterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	inputName = textBox.getText().toString();
            	if(inputName.matches(""))
            	{
            		Toast.makeText(FacialLearningActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
            		return;
            	}
//            	startCapture = true;
            	Toast.makeText(FacialLearningActivity.this, "On Click: " + inputName, Toast.LENGTH_SHORT).show();
            }
        });

		finishBtn = (Button) findViewById(R.id.finishBtn);
		finishBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	isFinish = 1;
            }
        });
		
		
		finishBtn2 = (Button) findViewById(R.id.finishBtn2);
		finishBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	isFinish = 2;
            }
        });		
		faceRecognitionReceiver = new FaceRecognitionReceiver();
		IntentFilter filterFaceRecognition = new IntentFilter(
				RobotIntent.CAM_FACE_RECOGNITION);
		registerReceiver(faceRecognitionReceiver, filterFaceRecognition);
		
		
		faceTrackingReceiver = new FaceTrackingReceiver();
		IntentFilter filterFaceTracking = new IntentFilter(RobotIntent.CAM_FACE_TRACKING);
		registerReceiver(faceTrackingReceiver, filterFaceTracking);
		
		
		// Setup Bind Service
		VisionConfig.bindService(this, mConnection);
		
		// Setup Paint
		textPt = new Paint();
		imgPt = new Paint();
		textTrackingPt = new Paint();
		
		textPt.setColor(Color.GREEN);
		textPt.setTextSize(20);
		textPt.setStrokeWidth(2);
		textPt.setStyle(Paint.Style.STROKE);
		
		textTrackingPt.setColor(Color.BLUE);
		textTrackingPt.setTextSize(10);
		textTrackingPt.setStrokeWidth(1);
		textTrackingPt.setStyle(Paint.Style.STROKE);
		
		
		imgPt.setAlpha(200);
		
	}
	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(FacialLearningActivity.this,
					"Camera Service is disconnected", Toast.LENGTH_SHORT).show();
			mBounded = false;
			mCameraService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(FacialLearningActivity.this, "Camera Service is connected",
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
	// FaceTrackingReceiver
	public class FaceTrackingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("data");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if (pc != null) {
				faceTrackingInfo.clear();
				for (Parcelable face : pc) {
					FaceInfo cur = (FaceInfo) face;
					if(!faceTrackingInfo.contains(cur))
						faceTrackingInfo.add(cur);
				}
				lastReceiveFaceTrackingTime = System.currentTimeMillis();
			} else {
				
			}
		}

	}
	// Face Recognition
	public class FaceRecognitionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getBundleExtra("data");
			Parcelable[] pc = bundle.getParcelableArray("data");
			if (pc != null) {
				faceInfo.clear();
				for (Parcelable face : pc) {
					FaceInfo cur = (FaceInfo) face;
					if(!faceInfo.contains(cur))
						faceInfo.add(cur);
					Log.e("MyLog", "FRAct Name["+ faceInfo.size() +"/" + pc.length +"] = " + ((FaceInfo)face).name);
				}
				lastReceiveFaceTime = System.currentTimeMillis();
			} else {
				Log.i("MyLog", "FRAct: FaceListSize: NULL");
			}
		}

	}
	class GetImageTask extends TimerTask {

		byte[] data;
		private Mat img = new Mat(VisionConfig.getHeight(), VisionConfig.getWidth(), CvType.CV_8UC3);
		Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(), VisionConfig.getHeight(), Bitmap.Config.ARGB_8888);

		@Override
		public void run() {
			if (mCameraService == null) {
				Log.e("MyLog",
						"FRAct GetImageTask NULL mCameraService");
				return;
			}
			data = mCameraService.getFrame();
			img.put(0, 0, data);
			Utils.matToBitmap(img, bitmap);
			
			Canvas cover = mCamSV.getHolder().lockCanvas(null);
			if (cover == null)
				return;
			if (bitmap != null && cover != null) {
				cover.drawBitmap(bitmap, 0, 0, null);
			} else {
				Log.i("MyLog", "FRAct: BITMAP NULL");
			}
			
			
			// Draw Face Tracking
			FaceInfo[] faceTrackingInfoList = null;
			if(System.currentTimeMillis() - lastReceiveFaceTrackingTime < 500){
				faceTrackingInfoList = faceTrackingInfo.toArray(new FaceInfo[faceTrackingInfo.size()]);
				for(FaceInfo face : faceTrackingInfoList){
					if(face == null) break;
					cover.drawRect(face.x, face.y,face.x + face.w,face.y + face.h, textTrackingPt);
					cover.drawText(face.name, face.x, face.y, textTrackingPt);
				}
			}
			
			
			// Draw Faces
			FaceInfo[] faceInfoList = null;
			//System.currentTimeMillis() - lastAddTime > 1000 && 
			if(System.currentTimeMillis() - lastReceiveFaceTime < 500){
				faceInfoList = faceInfo.toArray(new FaceInfo[faceInfo.size()]);
				for(FaceInfo face : faceInfoList){
					if(face == null) break;
					cover.drawRect(face.x, face.y,face.x + face.w,face.y + face.h, textPt);
					cover.drawText(face.name, face.x, face.y, textPt);
				}
			}

			// check if a unknown face is in current view
			Vector<FaceInfo> unknownList = new Vector<FaceInfo>();
			Vector<FaceInfo> knownList = new Vector<FaceInfo>();
			if(faceInfoList != null){
//				Log.e("MyLog2", "FaceList Len: " + faceInfoList.length);
				for(FaceInfo face : faceInfoList){
					if(face.name.startsWith("unknown")){
//						Log.e("MyLog2", "FaceList Len: add unknown list " + face.name);
						unknownList.add(face);
					}else{
						knownList.add(face);
					}
				}				
			}
//			Log.e("MyLog2", "UnknownList Len: " + unknownList.size());
			// Process STATE
			if(lastProcessTime < lastReceiveFaceTime){
				lastProcessTime = lastReceiveFaceTime;
				stateProcessing(cover, img, bitmap, unknownList.toArray(new FaceInfo[unknownList.size()]), knownList.toArray(new FaceInfo[knownList.size()]));
			}

			
			// Draw imgVector
			int eachCol = (int) (VisionConfig.FACIAL_LEARN_NUM_IMG / 3);
			if(eachCol == 0) eachCol = 1; 
			for(int iImg = 0; iImg < imgVector.size(); iImg++){
				cover.drawBitmap(imgVector.get(iImg), (int)(iImg / eachCol) * VisionConfig.FACE_REG_WIDTH, (iImg % eachCol) * VisionConfig.FACE_REG_WIDTH, imgPt);
			}
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
		
	}
	private void resetVariable(){
		inputName = "";
		imgVector.clear();
		isFinish = 0;
		isWaitPermission = false;
		curState = STATE.START;
		curWaitState = WAIT_STATE.STOP;		
	}
	private void stateProcessing(Canvas cover, Mat matImg, Bitmap img, FaceInfo[] unknownList, FaceInfo[] knownList){
		Log.e("MyLog", "CurState = " + curState.toString());
		switch(curState){
		case START:
			resetVariable();
			curState = STATE.WAIT_PERMISSION;
			break;
		case WAIT_PERMISSION:
			// NO PERMISSION
			curState = STATE.ASK_NAME;
			
			// END NO PERMISSION
			Log.i("MyLog", "FRAct: GROUP INFO : " + facepp.hasCandidate());
			if(facepp.hasCandidate() == false){
				curState = STATE.ASK_NAME;
				break;
			}
			if(knownList.length > 0){
				curState = STATE.ASK_NAME;
			}else{
				if(isWaitPermission == false){
					isWaitPermission = true;
					Log.i("MyLog", "FRAct: WAIT_PERMISSION");
					robotbase.utility.Notification.voiceNotification(getApplicationContext(), waitPermissionString);		
				}
			}
		
			break;
		case ASK_NAME:
			Log.i("MyLog", "FRAct: ASK_NAME");
			if(unknownList.length > 0){
				robotbase.utility.Notification.voiceNotification(getApplicationContext(), yourNameString);
				curState = STATE.WAIT_NAME;
			}
			break;

		case WAIT_NAME:
			if(!inputName.matches("")){
				curState = STATE.NOTIFY_START;
			}
			break;
		case NOTIFY_START:
			Log.i("MyLog", "FRAct: NOTIFY_START");
			robotbase.utility.Notification.voiceNotification(getApplicationContext(), notifyString);
			curState = STATE.WAIT_ONE_FACE;
			break;
		case WAIT_ONE_FACE:
			for ( FaceInfo f : unknownList){
				Log.e("MyLog2", "Name: " + f.name + " X: " + f.x);	
			}
			
			int countUnknownFace = unknownList.length;
			WAIT_STATE nextWaitState = null;
			if(countUnknownFace > 1){
				nextWaitState = WAIT_STATE.TOO_MANY_FACES;
			}else if(countUnknownFace == 0){
				nextWaitState = WAIT_STATE.NO_FACE;
			}else if(countUnknownFace == 1){
				nextWaitState = WAIT_STATE.ONE_FACE;
			}
			if(curWaitState.equals(nextWaitState)){
				curWaitTry++;
				if(curWaitTry == 5){
					switch(curWaitState){
					case TOO_MANY_FACES:
						Log.i("MyLog", "FRAct: WAIT_ONE_FACE: ambiguous " + countUnknownFace + " faces");
						robotbase.utility.Notification.voiceNotification(getApplicationContext(), ambiguousString);					
						break;
					case NO_FACE:
						Log.i("MyLog", "FRAct: WAIT_ONE_FACE: NO FACE");
						robotbase.utility.Notification.voiceNotification(getApplicationContext(), noFaceString);
						break;
					case ONE_FACE:
						curWaitTry = 0;
						curState = STATE.ROTATE_CENTER;
						curWaitState = WAIT_STATE.STOP;
						break;
					default:
						break;
					}
				}
			}else{
				curWaitState = nextWaitState;
				curWaitTry = 0;
			}
			break;
		case SAVE_PICTURE:
			if(unknownList.length == 1){
				Log.i("MyLog", "FRAct: SAVE_PIC: countUnknown == 1");
				FaceInfo face = unknownList[0];
				// chup hinh
				if( ( System.currentTimeMillis() - lastAddTime) > 1000 / VisionConfig.FACIAL_LEARN_FPS && imgVector.size() < VisionConfig.FACIAL_LEARN_NUM_IMG)
				{
					int cX = (int)face.x, cY = (int)face.y, cW = (int)face.w, cH = (int)face.h;
					if((face.x - face.w/3) > 0){
						cX = (int) (face.x - face.w/3);
						cW += 2 * face.w/3;
						if((cX + cW) > img.getWidth()) cW = img.getWidth() - cX - 1;
					}
					if((face.y - face.h/3) > 0){
						cY = (int) (face.y - face.h/3);
						cH += 2 * face.w/3;
						if((cY + cH) > img.getHeight()) cH = img.getHeight() - cY - 1;
					}
					
					Bitmap curImg = Bitmap.createBitmap(img, cX, cY, cW, cH);
					Bitmap faceBitmap = Bitmap.createScaledBitmap(curImg, VisionConfig.FACE_REG_WIDTH, VisionConfig.FACE_REG_HEIGHT, false);
					imgVector.add(faceBitmap);
					lastAddTime = System.currentTimeMillis();
					if(imgVector.size() == VisionConfig.FACIAL_LEARN_NUM_IMG){
						if(sendServer){
							facepp.personCreate(inputName);
							
							facepp.personAddManyFace(inputName, imgVector);
							
			            	facepp.groupAddPerson(VisionConfig.FACE_REG_GROUP_NAME, inputName);
			            	facepp.personGetInfo(inputName);							
						}

		            	
		            	curState = STATE.ASK_FINISH;
					}
				}
				//
				if(curState != STATE.ASK_FINISH){
					curState = STATE.MOVE;
				}
			}else{
				curState = STATE.WAIT_ONE_FACE;
			}
			break;
		case ROTATE_CENTER:
			if(unknownList.length == 1){
				if(System.currentTimeMillis() - lastRotateTime > 100){
					lastRotateTime = System.currentTimeMillis();
					float x = unknownList[0].x + unknownList[0].w / 2 ;
					float y = unknownList[0].y + unknownList[0].h / 2 ;
					boolean isCenter = VisionHelperCenterOnTargets.run(
							getApplicationContext(), x , y);
					if (isCenter) {
						curState = STATE.SAVE_PICTURE;
					}									
				}
			}else{
				curState = STATE.WAIT_ONE_FACE;
			}

			break;
		case MOVE:
			curState = STATE.ROTATE_CENTER;
			
			FacialLearningActivity.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Move", Toast.LENGTH_SHORT).show();					
				}
			});
			
			int kobukiCommand = 0;
			
			if(Math.random() > 0.7){
				// rotate
				kobukiCommand = (Math.random() > 0.5) ? KobukiCommand.LEFT.ordinal() : KobukiCommand.RIGHT.ordinal() ;
			}
			else{
				// forward, backward
				kobukiCommand = (Math.random() > 0.85) ? KobukiCommand.FORWARD.ordinal() : KobukiCommand.BACKWARD.ordinal() ;
			}

			VisionHelperCenterOnTargets.move(getApplicationContext(), kobukiCommand);

			break;
		case ASK_FINISH:
			resetVariable();
			
			Log.i("MyLog", "FRAct: ASK_FINISH");
			robotbase.utility.Notification.voiceNotification(getApplicationContext(), askFinishString);
			curState = STATE.WAIT_FINISH;
			break;
		case WAIT_FINISH:
			int commandFinish = isFinish;
			if(commandFinish == 1){
				curState = STATE.STOP;
			}else if (commandFinish == 2){
				curState = STATE.START;
			}
			break;
		case STOP:
			if(sendServer){
				facepp.trainIdentify(VisionConfig.FACE_REG_GROUP_NAME);
			}
			sendBroadcast(new Intent(RobotIntent.CAM_FACIAL_LEARNING));
        	finish();
        	break;
		default:
			break;
		}
	}
	
	private boolean pauseThread(int timespan) {
		if(lastMoveTime == 0){
			lastMoveTime = System.currentTimeMillis();
		}else if(System.currentTimeMillis() - lastMoveTime > timespan){
			lastMoveTime = 0;
			isMove = false;
			return true;			
		}
		return false;
	}

	@Override
	protected void onDestroy() {
    	Toast.makeText(getApplicationContext(), "I am processing. Please wait.!", Toast.LENGTH_LONG).show();
		try{
			VisionConfig.unbindService(this, mConnection);
			timer.cancel();
			unregisterReceiver(faceRecognitionReceiver);
			unregisterReceiver(faceTrackingReceiver);
		}catch(Exception e){
			Log.i("MyLog", "FRAct: Error onDestroy: " + e.toString());
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	    	Log.i("MyLog","FRAct: Activity: FINISH ON KEY BACK");
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
