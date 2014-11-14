package robotbase.vision.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import robotbase.vision.BaseCameraService;
import robotbase.vision.VisionConfig;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NativeCameraService extends BaseCameraService {

	private VideoCapture mCamera;
	private Mat mRgbRotated, mRgb;
	private byte[] pixelsByteRgb;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private boolean connectCamera(int frameWidth, int frameHeight) {
		mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID + 1);
		if (mCamera == null)
			return false;

		if (mCamera.isOpened() == false)
			return false;
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, frameHeight);
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, frameWidth);
		return true;
	}

	@Override
	public void onDestroy() {
		mCamera.release();
		stopSelf();
		super.onDestroy();
	}

	@Override
	protected byte[] getByteFrame() {
		if (!mCamera.grab()) {
			return null;
		}
		mCamera.retrieve(mRgb, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		Core.transpose(mRgb, mRgbRotated);
		Core.flip(mRgbRotated, mRgbRotated, 0);
		mRgbRotated.get(0, 0, pixelsByteRgb);
		return pixelsByteRgb;
	}

	@Override
	public void initService() {
		// Load Native Library
		System.loadLibrary("opencv_java");
		mRgb = new Mat();
		mRgbRotated = new Mat();
		pixelsByteRgb = new byte[VisionConfig.getLengthRgb()];

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
	}
}
