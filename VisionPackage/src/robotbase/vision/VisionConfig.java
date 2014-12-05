package robotbase.vision;

import robotbase.abilities.ShowGallery;
import robotbase.vision.camera.CameraService;
import robotbase.vision.camera.NativeCameraService;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class VisionConfig {
	public static final String USER_PERSON_NAME_PREFIX = "User-01";
	public static final String PERSON_NAME_SEPERATOR = "[RB]";
	public static final String FACE_REG_FACEAPI = "b24fb274d3e8e93697bfaf29ff3560e4";
	public static final String FACE_REG_FACEKEY = "IPxl_stLnE9ey2CSWynWFIKwViPP0hEi";
	public static final int FACE_REG_MAX_TRY = 1;
	public static final int FACE_REG_TIME_WAIT_TRAIN = 30 * 1000; // Send training command after 30s of no face tracking
	public static final String FACE_REG_GROUP_NAME = "Robotbase";
	public static final int FACE_REG_WIDTH = 150, FACE_REG_HEIGHT = 150;
	public static final int FACE_REG_IDENTIFY_WIDTH = 120, FACE_REG_IDENTIFY_HEIGHT = 120;
	public static final int FACE_REG_IDENTIFY_FPS = 5;
	public static final int FACE_REG_MAX_IMG_PER_REQUEST_WIDTH = 2;
	public static final int FACE_REG_MAX_IMG_PER_REQUEST_HEIGHT = 3;
	public static final int FACE_REG_MAX_IMG_PER_REQUEST = FACE_REG_MAX_IMG_PER_REQUEST_WIDTH * FACE_REG_MAX_IMG_PER_REQUEST_HEIGHT;
	
	public static final float FACIAL_LEARN_FPS = 4;
	public static final float FACIAL_LEARN_NUM_IMG_WIDTH = 5;
	public static final float FACIAL_LEARN_NUM_IMG_HEIGHT = 4;
	public static final float FACIAL_LEARN_NUM_IMG = FACIAL_LEARN_NUM_IMG_WIDTH * FACIAL_LEARN_NUM_IMG_HEIGHT;
	public static int isAndroidCamera = 2;
	public static final boolean FACE_REG_BACKUP = false;
	public static final boolean FACE_REG_SEND_SERVER = true; 
	public static final boolean USE_SERIAL = false;
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
	
		c.startService(new Intent(c, FaceDetectionFacepp.class));
		c.startService(new Intent(c, FaceTrackingOpenCv.class));
		
//		c.startService(new Intent(c, VisionService.class));
		
		c.startService(new Intent(c, TakePictureService.class));
//		c.startService(new Intent(c, ShowGallery.class));
		c.startService(new Intent(c, FaceRecognitionService.class));
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

		c.stopService(new Intent(c, FaceDetectionFacepp.class));
		c.stopService(new Intent(c, FaceTrackingOpenCv.class));
		
//		c.stopService(new Intent(c, VisionService.class));
		c.stopService(new Intent(c, TakePictureService.class));
//		c.stopService(new Intent(c, ShowGallery.class));
//		
		c.stopService(new Intent(c, FaceRecognitionService.class));
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
