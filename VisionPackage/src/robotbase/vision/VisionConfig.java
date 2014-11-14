package robotbase.vision;

import robotbase.abilities.ShowGallery;
import robotbase.vision.camera.CameraService;
import robotbase.vision.camera.NativeCameraService;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class VisionConfig {
	public static int isAndroidCamera = 2;

	// 0 : Native Camera OpenCV
	// 1 : Camera Service OpenNI
	// 2 : Android Camera

	// public static int getLengthGray(){
	// return getHeight() * getWidth();
	// }
	public static int getLengthRgb() {
		return getHeight() * getWidth() * 3;
	}

	public static int getWidth() {
		switch (isAndroidCamera) {
		case 0:
			return NativeCameraService.camWidth;
		case 1:
			return CameraService.camWidth;
		case 2:
			return AndroidCameraService.camWidth;
		}
		return 0;
	}

	public static int getHeight() {
		switch (isAndroidCamera) {
		case 0:
			return NativeCameraService.camHeight;
		case 1:
			return CameraService.camHeight;
		case 2:
			return AndroidCameraService.camHeight;
		}
		return 0;
	}

	public static void startService(Context c) {
		switch (isAndroidCamera) {
		case 0:
			c.startService(new Intent(c, NativeCameraService.class));
			break;
		case 1:
			c.startService(new Intent(c, CameraService.class));
			break;
		case 2:
			c.startService(new Intent(c, AndroidCameraService.class));
			break;
		}
		
		c.startService(new Intent(c, VisionService.class));
		c.startService(new Intent(c, TakePictureService.class));
		c.startService(new Intent(c, ShowGallery.class));
	}

	public static void stopService(Context c) {
		switch (isAndroidCamera) {
		case 0:
			c.stopService(new Intent(c, NativeCameraService.class));
			break;
		case 1:
			c.stopService(new Intent(c, CameraService.class));
			break;
		case 2:
			c.stopService(new Intent(c, AndroidCameraService.class));
			break;
		}

		c.stopService(new Intent(c, VisionService.class));
		c.stopService(new Intent(c, TakePictureService.class));
		c.stopService(new Intent(c, ShowGallery.class));	
	}

	public static void bindService(Context c, ServiceConnection mConnection) {
		switch (isAndroidCamera) {
		case 0:
			c.bindService(new Intent(c, NativeCameraService.class), mConnection, Context.BIND_AUTO_CREATE);
			break;
		case 1:
			c.bindService(new Intent(c, CameraService.class), mConnection, Context.BIND_AUTO_CREATE);
			break;
		case 2:
			c.bindService(new Intent(c, AndroidCameraService.class), mConnection, Context.BIND_AUTO_CREATE);
			break;
		}
	}

	public static void unbindService(Context c, ServiceConnection mConnection) {
		c.unbindService(mConnection);
	}
}
