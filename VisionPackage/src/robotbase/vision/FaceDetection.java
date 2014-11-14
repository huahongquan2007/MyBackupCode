package robotbase.vision;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.util.Log;

public class FaceDetection extends VisionAlgorithm{
	public FaceDetection(float fps, Context ctx) {
		super(fps, ctx);
	}
	private Face[] mFaces;
	protected Camera cam;
	
	FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			if (faces.length == 0) {
				Log.e("HHQ", " No Face Detected! ");
			} else {
				Log.e("HHQ", String.valueOf(faces.length)
						+ " Face Detected :) ");
			}
			mFaces = faces;
		}
	};
	
	public void setCamera(Camera c) {
		cam = c;
		cam.setFaceDetectionListener(faceDetectionListener);
	}
	@Override
	public void start(){
		cam.startFaceDetection();
	}
	@Override
	public void stop() {
		cam.stopFaceDetection();
	}
//	@Override
//	public String getResult(){
//		if(mFaces != null && mFaces.length > 0){
//	        JSONObject jsonObj = new JSONObject();
//	        try {
//				jsonObj.put("x", String.valueOf(mFaces[0].rect.top));
//				jsonObj.put("y", String.valueOf(mFaces[0].rect.left));
//				jsonObj.put("w", String.valueOf(mFaces[0].rect.width()));
//				jsonObj.put("h", String.valueOf(mFaces[0].rect.height()));
//				
//				return jsonObj.toString();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return "No faces";
//	}

	@Override
	public void run(byte[] frame) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Bundle getResultBundle() {
		if(mFaces != null && mFaces.length > 0){
	        Bundle result = new Bundle();
	        return result;
		}
		return null;
	}

	@Override
	public void broadcast() {
		// TODO Auto-generated method stub
		
	}
}
