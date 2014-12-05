package robotbase.vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import robotbase.action.RobotIntent;

import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FaceDetectionFacepp extends VisionAlgo {
    FaceDetecter facedetecter = null;
	byte[] ori;
	Mat originalFrame, scaledFrame, grayFrame;
	Face[] faceInfo;
	private long time;
	private int scaleWidth = 240;
	private int scaleHeight = 320;
	private float scale_w = (float)scaleWidth / VisionConfig.getWidth(), scale_h = (float)scaleHeight / VisionConfig.getHeight();
	
	
	@Override
	protected void setup() {
		facedetecter = new FaceDetecter();
        if (!facedetecter.init(getApplicationContext(), VisionConfig.FACE_REG_FACEAPI)) {
            Log.e("MyLog", "FaceDetection Facepp: CANNOT INITILIZE");
        }else{
        	Log.i("MyLog", "FaceDetection Facepp: INITILIZED SUCCESSFULLY");
        }
        ori = new byte[scaleWidth * scaleHeight];
        scaledFrame  = new Mat(scaleHeight, scaleWidth, CvType.CV_8UC1);
		originalFrame = new Mat(frameHeight, frameWidth, CvType.CV_8UC3);
		grayFrame = new Mat(frameHeight, frameWidth, CvType.CV_8UC1);
	}
	@Override
	public void update(byte[] frame) {
		Log.e("MyLog","FaceDetection Facepp: 1 ");
		originalFrame.put(0, 0, frame);
		Imgproc.cvtColor(originalFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.resize(grayFrame, scaledFrame, new Size(), scale_w, scale_h, Imgproc.INTER_LINEAR);
		Log.e("MyLog","FaceDetection Facepp: 2 ");
		// convert frame (rgb) to ori(gray)
		scaledFrame.get(0, 0, ori);
		Log.e("MyLog","FaceDetection Facepp: 3 ");
		faceInfo = facedetecter.findFaces( ori, scaleWidth, scaleHeight);
		
		Log.e("MyLog","FaceDetection Facepp: 4 ");
		time = System.currentTimeMillis();
		if(faceInfo == null){
//			Log.i("MyLog", "FaceDetection Facepp: faceInfo NULL, frame length " + frame.length + " ori length " + ori.length);
			return;
		}
		Log.e("MyLog","FaceDetection Facepp: Length: " + faceInfo.length);
	}

	@Override
	public void broadcast() {
		if(faceInfo ==null ) return;
		try{
			Intent intent = new Intent();
			intent.putExtra("bundle", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_DETECTION);
			getApplicationContext().sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}
	@Override
	protected Bundle getResultBundle() {
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

}
