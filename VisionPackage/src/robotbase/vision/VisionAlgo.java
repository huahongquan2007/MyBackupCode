package robotbase.vision;

import java.util.Timer;
import java.util.TimerTask;

import robotbase.vision.BaseCameraService.LocalBinder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public abstract class VisionAlgo extends Service {

	private Timer timer;
	protected int frameWidth = VisionConfig.getWidth(), frameHeight = VisionConfig.getHeight(); 
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("MyLog", "VisionAlgo onStart");
		VisionConfig.bindService(this, mConnection);
		
		this.setup();
		
		return super.onStartCommand(intent, flags, startId);
	}

	// Connection to Bind
	boolean mBounded;
	BaseCameraService mCameraService;
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
				Log.e("MyLog", "FPS == 0");
			}
			Log.e("MyLog", "FPS == " + fps);
			timer.schedule(new GetImageTask(), 0, (long) (1000 / fps));
		}
	};

	class GetImageTask extends TimerTask {
		byte[] data;

		@Override
		public void run() {
			if (mCameraService == null) {
				// Log.e("MyLog",
				// "VisionAlgo GetImageTask NULL mCameraService");
				return;
			}
			data = mCameraService.getFrame();
			if (data == null) {
				// Log.e("MyLog", "VisionAlgo GetImageTask NULL DATA");
				return;
			}
			update(data);
			broadcast();
		}	
	}

	abstract protected void setup();
	abstract protected void update(byte[] frame);
	abstract protected void broadcast();
	abstract protected Bundle	getResultBundle();
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		VisionConfig.unbindService(this, mConnection);
		timer.cancel();
		stopSelf();
		Log.e("MyLog", "VisionAlgo onDestroy stop");
	}

}
