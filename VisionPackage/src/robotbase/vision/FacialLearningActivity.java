package robotbase.vision;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import robotbase.vision.BaseCameraService.LocalBinder;
import robotbase.vision.CameraPreviewActivity.GetImageTask;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FacialLearningActivity extends Activity {
	SurfaceView mCamSV;
	Button enterBtn;
	// Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
	Timer timer;
	Bitmap bitmap = Bitmap.createBitmap(VisionConfig.getWidth(),
			VisionConfig.getHeight(), Bitmap.Config.ARGB_8888);
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		try{
			VisionConfig.unbindService(this, mConnection);
			VisionConfig.stopService(this);
			timer.cancel();
		}catch(Exception e){
			
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facial_learning);
		
		mCamSV = (SurfaceView) findViewById(R.id.cameraView);
		mCamSV.getHolder().setFixedSize(VisionConfig.getWidth(),
				VisionConfig.getHeight());
		
		enterBtn = (Button) findViewById(R.id.enterBtn);
		enterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	Toast.makeText(FacialLearningActivity.this, "On Click", Toast.LENGTH_SHORT).show();
            	
            }
        });
		// Setup Bind Service
		VisionConfig.startService(this);
		VisionConfig.bindService(this, mConnection);
		
	}
	public void onEnterBtnClicked(){
		
	}
	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(FacialLearningActivity.this,
					"Service is disconnected", Toast.LENGTH_SHORT).show();
			mBounded = false;
			mCameraService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(FacialLearningActivity.this, "Service is connected",
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
	class GetImageTask extends TimerTask {
		byte[] data;
		private Mat img = new Mat(VisionConfig.getHeight(),
				VisionConfig.getWidth(), CvType.CV_8UC3);
		
		@Override
		public void run() {
			if (mCameraService == null) {
				Log.e("MyLog",
						"CameraPreviewActivity GetImageTask NULL mCameraService");
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
				Log.i("MyLog", "CAP: BITMAP NULL");
			}
			
			mCamSV.getHolder().unlockCanvasAndPost(cover);
		}
	}
}
