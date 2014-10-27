package robotbase.vision.camera;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NativeCameraService extends Service {
	public static int camWidth = 240, camHeight = 320;
	public static final String CAMERA_INTENT_BITMAP = "robotbase.vision.camera.bitmap";
	public static final String CAMERA_INTENT_BYTE = "robotbase.vision.camera.byte";
	public static final String CAMERA_DATA = "robotbase.vision.camera.data";
	private Timer timer;
	private VideoCapture mCamera;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private boolean connectCamera(int frameWidth, int frameHeight) {
		mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
		if (mCamera == null)
			return false;

		if (mCamera.isOpened() == false)
			return false;
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, frameWidth);
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, frameHeight);
		return true;
	}

	@Override
	public void onDestroy() {
		stopSelf();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("MyLog", "NativeCameraService: onStartCommand");
		// Load Native Library
		System.loadLibrary("opencv_java");

		try {
			if (!connectCamera(camWidth, camHeight))
				Log.e("MyLog", "Could not connect camera");
			else
				Log.d("MyLog", "Camera successfully connected");
		} catch (Exception e) {
			Log.e("MyLog",
					"MyServer.connectCamera throws an exception: "
							+ e.getMessage());
		}

		timer = new Timer();
		timer.schedule(new GetImageTask(), 0, 1000/20);
		return super.onStartCommand(intent, flags, startId);
	}

	class GetImageTask extends TimerTask {
		@Override
		public void run() {
//			Log.d("MyLog", "NativeCameraService: onGetImage");
		}
	}
}
