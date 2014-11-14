package robotbase.vision.camera;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

public class CameraService extends Service {
	public static int camWidth = 240, camHeight = 320; 
	public static final String CAMERA_INTENT_BITMAP = "robotbase.vision.camera.bitmap";
	public static final String CAMERA_INTENT_BYTE_GRAY = "robotbase.vision.camera.byte";
	public static final String CAMERA_INTENT_BYTE_RGB = "robotbase.vision.camera.rgb";
	public static final String CAMERA_DATA = "robotbase.vision.camera.data";
	private Timer timer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("CAMERA SERVICE", "START SERVICE");
		NativeOpenni2.loadlibs();
		NativeOpenni2.InitCamera();
		timer= new Timer();
		timer.schedule(new GetImageTask(), 0, 40);
	}

	class GetImageTask extends TimerTask {

		@Override
		public void run() {
			Mat data = new Mat();
			NativeOpenni2.GetImage(data.getNativeObjAddr());

			if (data.empty())
				return;

			if (data.cols() <= 100 && data.rows() <= 100)
				return;
			
			byte[] data_byte = new byte[data.width() * data.height()];
			NativeOpenni2.GetImageByte(data_byte);
			
//			Imgproc.cvtColor(data, dataGray, Imgproc.COLOR_BGR2GRAY);
//
//			byte[] data_byte = new byte[(int) (dataGray.total() * 
//					dataGray.channels())];
//			dataGray.get(0, 0, data_byte);
			
//			Bitmap data_bmp = Bitmap.createBitmap(data.width(),
//					data.height(), Bitmap.Config.ARGB_8888);
//			Utils.matToBitmap(data, data_bmp);
			
			sendBroadcast(new Intent(CAMERA_INTENT_BYTE_GRAY).putExtra(CAMERA_DATA, data_byte));
//			sendBroadcast(new Intent(CAMERA_INTENT_BITMAP).putExtra(CAMERA_DATA, data_bmp));
		}

	}
}
