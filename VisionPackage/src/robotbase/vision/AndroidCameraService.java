package robotbase.vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

public class AndroidCameraService extends BaseCameraService {
	private Camera mCam;
	private Mat pixels;
	private byte[] pixelsByte;
	private byte[] pixelsByteRgb;

	private SurfaceTexture texture;

	@Override
	public void onDestroy() {
		Log.i("MyLog", "AndroidCameraService onDestroy start");
		mCam.stopPreview();
		mCam.setPreviewCallback(null);
		mCam.release();
		Log.i("MyLog", "AndroidCameraService onDestroy end");
		stopSelf();
		super.onDestroy();
	}

	@Override
	public void initService() {
		Log.i("MyLog", "AndroidCameraService initService");
		// Load Native Library
		System.loadLibrary("opencv_java");
		System.loadLibrary(NativeAndroidCamera.name);

		pixels = new Mat(camHeight, camWidth, CvType.CV_8UC3);

		try {
			mCam = Camera.open(1);

			int minFps = 0, maxFps = 0;
			for (int i = 0; i < mCam.getParameters()
					.getSupportedPreviewFpsRange().size(); i++) {
				int[] size = mCam.getParameters().getSupportedPreviewFpsRange()
						.get(i);
				Log.d("MyLog", "supported fps: " + String.valueOf(size[0])
						+ " " + String.valueOf(size[1]));
				minFps = size[0];
				maxFps = size[1];
				if (size[0] > 10000) {
					break;
				}
			}
			for (int i = 0; i < mCam.getParameters()
					.getSupportedPreviewSizes().size(); i++) {
				Size size = mCam.getParameters().getSupportedPreviewSizes().get(i);
				Log.d("MyLog", "supported size: " + size.width + " " + size.height);
			}
			// Setup FPS
			setup(minFps/1000);

			Camera.Parameters p = mCam.getParameters();
			p.setPreviewSize(camHeight, camWidth);
			p.setPreviewFpsRange(minFps, maxFps);
			mCam.setParameters(p);
			texture = new SurfaceTexture(10);
			mCam.setPreviewTexture(texture);
			mCam.startPreview();
			pixelsByte = new byte[camWidth * camHeight];
			pixelsByteRgb = new byte[camWidth * camHeight * 3];
			mCam.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
//					Log.e("MyLog", "AndroidCameraService onPreviewFrame");
					NativeAndroidCamera.getFrame(data, camWidth, camHeight,
							pixels.getNativeObjAddr(), pixelsByte,
							pixelsByteRgb);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected byte[] getByteFrame() {
		return pixelsByteRgb;
	}
}