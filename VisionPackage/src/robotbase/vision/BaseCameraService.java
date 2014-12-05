package robotbase.vision;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public abstract class BaseCameraService extends Service{
	public int thanh = 1;
	protected long lastTime = 0, frameDelay = 0;
	protected byte[] lastFrame = null;
	protected int fps = 15;
	public static int camWidth = 480, camHeight = 640;
	public static final String CAMERA_INTENT_BITMAP = "robotbase.vision.androidcamera.bitmap";
	public static final String CAMERA_INTENT_BYTE_GRAY = "robotbase.vision.androidcamera.byte";
	public static final String CAMERA_INTENT_BYTE_RGB = "robotbase.vision.androidcamera.rgb";
	public static final String CAMERA_DATA = "robotbase.vision.androidcamera.data";

	IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
		public BaseCameraService getServerInstance() {
			return BaseCameraService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("MyLog", "Server onBind");
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		Log.d("MyLog", "BaseCameraServer onCreate");
		// Setup FPS
		setup(fps);
		initService();
		super.onCreate();
	}

	protected void setup(int fps){
		this.fps = fps;
		frameDelay = 1000 / fps;
	}
	public int getFPS(){
		return fps;
	}
	public byte[] getFrame(){
		if(System.currentTimeMillis() - lastTime > frameDelay){
			lastFrame = getByteFrame();
		}
		return lastFrame;
	}
	abstract public void initService();
	abstract protected byte[] getByteFrame();
}