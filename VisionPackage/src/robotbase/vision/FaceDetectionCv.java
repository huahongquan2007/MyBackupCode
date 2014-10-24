package robotbase.vision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;

import robotbase.action.RobotIntent;
import robotbase.vision.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FaceDetectionCv extends VisionAlgorithm{
	private File mCascadeFile;
	private int[] xArr, yArr, wArr, hArr;
	private int size;
	private long time;
	public FaceDetectionCv(float fps, Context ctx) {
		super(fps, ctx);
		initOpenCV(context);
		xArr = new int [100];
		yArr = new int [100];
		wArr = new int [100];
		hArr = new int [100];

	}
	
	@Override
	public void start(){
		System.loadLibrary(NativeFaceDetection.name);
		NativeFaceDetection.initCascade(mCascadeFile.getAbsolutePath());
		NativeFaceDetection.setPreviewSize(frameWidth, frameHeight);
	}
	@Override
	public void stop() {
	}
	@Override
	public String getResult(){
		return "No faces";
	}
	@Override
	public Bundle getResultBundle() {
		Bundle result = new Bundle();
		FaceInfo[] faceInfo = new FaceInfo[size];
		for (int i = 0; i < size; i++) {
			faceInfo[i] = new FaceInfo(xArr[i], yArr[i], wArr[i], hArr[i],time);
		}
		result.putParcelableArray("data", faceInfo);

		return result;
	}
	@Override
	public void run(byte[] frame) {
		size = NativeFaceDetection.getPos(frame, xArr, yArr, wArr, hArr, pixels.getNativeObjAddr());
		time = System.currentTimeMillis();
		Log.e("MyLog", "Face Info: " + String.valueOf(size));
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

	@Override
	public void runRGB(Mat frame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast() {
		try{
			Intent intent = new Intent();
			intent.putExtra("faceDetection", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_DETECTION);
			context.sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}

}
