package robotbase.vision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import robotbase.action.RobotIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FaceDetectionCv extends VisionAlgo{
	private File mCascadeFile;
	private int[] xArr, yArr, wArr, hArr;
	private int size;
	private long time;
	protected Mat pixels = null;
	@Override
	protected void setup() {
		initOpenCV(getApplicationContext());
		xArr = new int [100];
		yArr = new int [100];
		wArr = new int [100];
		hArr = new int [100];
		
		System.loadLibrary(NativeFaceDetection.name);
		NativeFaceDetection.initCascade(mCascadeFile.getAbsolutePath());
		NativeFaceDetection.setPreviewSize(frameWidth, frameHeight);
		pixels = new Mat(frameHeight, frameWidth, CvType.CV_8UC4);
	}

	@Override
	protected void update(byte[] frame) {
		size = NativeFaceDetection.getPos(frame, xArr, yArr, wArr, hArr, pixels.getNativeObjAddr());
		time = System.currentTimeMillis();
		Log.e("MyLog", "FaceDetectionCv: " + String.valueOf(size));
		
	}

	@Override
	protected void broadcast() {
		try{
			Intent intent = new Intent();
			intent.putExtra("bundle", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_DETECTION);
//			intent.setAction("hhq.face");
			getApplicationContext().sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}

	@Override
	protected Bundle getResultBundle() {
		Bundle result = new Bundle();
		FaceInfo[] faceInfo = new FaceInfo[size];
		for (int i = 0; i < size; i++) {
			faceInfo[i] = new FaceInfo(xArr[i], yArr[i], wArr[i], hArr[i],time);
		}
		result.putParcelableArray("data", faceInfo);

		return result;
	}
	private void initOpenCV(Context ctx) {
		InputStream is = ctx.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
//		InputStream is = ctx.getResources().openRawResource(R.raw.lbpcascade_frontalface);
		
		File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
//		mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
		mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(mCascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}

			is.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d("HHQ", "HHQ " + mCascadeFile.getAbsolutePath());
	}
}
