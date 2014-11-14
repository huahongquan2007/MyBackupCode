package robotbase.vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import robotbase.action.RobotIntent;
import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FaceDetectionFacepp extends VisionAlgorithm{
    FaceDetecter facedetecter = null;
	byte[] ori;
	Mat originalFrame, grayFrame;
	Face[] faceInfo;
	private long time;
    public FaceDetectionFacepp(float fps, Context ctx) {
		super(fps, ctx);
		facedetecter = new FaceDetecter();
        if (!facedetecter.init(context, "b24fb274d3e8e93697bfaf29ff3560e4")) {
            Log.e("MyLog", "FaceDetection Facepp: CANNOT INITILIZE");
        }else{
        	Log.i("MyLog", "FaceDetection Facepp: INITILIZED SUCCESSFULLY");
        }
        ori = new byte[frameWidth * frameHeight];
		originalFrame = new Mat(frameHeight, frameWidth, CvType.CV_8UC3);
		grayFrame = new Mat(frameHeight, frameWidth, CvType.CV_8UC1);
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		Log.e("MyLog", "Facepp onStop");
		try{
			facedetecter.release(context);
		}catch(Exception e){
			Log.e("MyLog", e.toString());
		}
        
		Log.e("MyLog", "Facepp release");
	}

	@Override
	public void run(byte[] frame) {

		originalFrame.put(0, 0, frame);
		Imgproc.cvtColor(originalFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// convert frame (rgb) to ori(gray)
		grayFrame.get(0, 0, ori);
		faceInfo = facedetecter.findFaces( ori, frameWidth, frameHeight);
		time = System.currentTimeMillis();
		if(faceInfo == null){
//			Log.i("MyLog", "FaceDetection Facepp: faceInfo NULL, frame length " + frame.length + " ori length " + ori.length);
			return;
		}
		Log.e("MyLog","FaceDetection Facepp: Length: " + faceInfo.length);
	 }


	@Override
	public Bundle getResultBundle() {
		Bundle result = new Bundle();
		int size = faceInfo.length;
		FaceInfo[] faceInfoArray = new FaceInfo[size];
		float left, right, top, bottom;
		for (int i = 0; i < size; i++) {
			left = faceInfo[i].left * frameWidth;
			top  = faceInfo[i].top * frameHeight;
			bottom = faceInfo[i].bottom * frameHeight;
			right = faceInfo[i].right * frameWidth;
			faceInfoArray[i] = new FaceInfo(left, top, right - left, bottom - top, time);
		}
		result.putParcelableArray("data", faceInfoArray);

		return result;
	}

	@Override
	public void broadcast() {
		if(faceInfo ==null ) return;
		try{
			Intent intent = new Intent();
			intent.putExtra("bundle", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_DETECTION);
			context.sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}

}
