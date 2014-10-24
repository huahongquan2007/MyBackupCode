package robotbase.vision;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import robotbase.action.RobotIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class FaceTrackingOld extends VisionAlgorithm{
	private float x = -1 ,y = -1 ,w = -1 ,h = -1;
	private int count = 0;
	private Mat histROI = null;
	private Rect track_window;
	private Rect result;
	private long time, lastProcessTime;

	private FaceDataReceiver faceDataReceiver;
	
	public FaceTrackingOld(float fps, Context ctx) {
		super(fps,ctx);
		
		faceDataReceiver = new FaceDataReceiver();
		IntentFilter filterVision = new IntentFilter(RobotIntent.CAM_FACE_DETECTION);
		context.registerReceiver(faceDataReceiver, filterVision);
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		context.unregisterReceiver(faceDataReceiver);
	}
	public class FaceDataReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","FaceTracking: FaceData onReceive");
			Bundle fdBundle = intent.getBundleExtra("faceDetection");
			Parcelable[] faceInfo = fdBundle.getParcelableArray("data");
			for (int i = 0; i < faceInfo.length; i++) {
				FaceInfo f = (FaceInfo)faceInfo[i];
				Log.i("MyLog","FaceTracking: X: " + String.valueOf(f.x) + " Y: " + String.valueOf(f.y) );
			}
			if(faceInfo.length > 0){
				FaceInfo f = (FaceInfo)faceInfo[0];
				x = f.x;
				y = f.y;
				w = f.w;
				h = f.h;
				time = f.time;
			}
			
		}
	}
	@Override
	public void run(byte[] frame) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void runRGB(Mat frame) {
		if(lastProcessTime < time){
			lastProcessTime = time;
			// Image -> HSV
			Mat hsv = new Mat();
			Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);
			Mat subImg = hsv.submat((int)y, (int)(y + h), (int)x, (int)(x + w) );
			
			Log.i("MyLog","FaceTracking: runRGB");
			// Center cua face moi != cac vung da co ==> Mat moi ==> Them ROI
			if(count == 0 && x > 0){
				count = 1;
				histROI = getROIforTracking(subImg);
				track_window = new Rect((int)x,(int)y,(int)w,(int)h);
			}
			else{
				// Else: Neu la mat cu ===> Tracking :Camshift
				Mat dst = getBackproject(hsv);
		        RotatedRect rect = Video.CamShift(dst, track_window, new TermCriteria(TermCriteria.EPS,10,1));
		        result = rect.boundingRect();
		        
		        Log.i("MyLog", "Face Tracking Result: X = " + String.valueOf(result.x));
			}

		}
	}
	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getResultBundle() {
		Bundle bd = new Bundle();
		FaceInfo faceInfo = new FaceInfo();
		faceInfo.x = result.x;
		faceInfo.y = result.y;
		faceInfo.w = result.width;
		faceInfo.h = result.height;
		
		
		bd.putParcelable("data", faceInfo);

		return bd;
	}

	private Mat getROIforTracking(Mat hsv){
	
	    Mat hist = new Mat();
	    int h_bins = 30; 
	    int s_bins = 32;

	    MatOfInt mHistSize = new MatOfInt (h_bins, s_bins);

	    MatOfFloat mRanges = new MatOfFloat(0, 179, 0, 255);
	    MatOfInt mChannels = new MatOfInt(0, 1);

	    Mat mask = new Mat();
	    boolean accumulate = false;
	    Imgproc.calcHist(Arrays.asList(hsv), mChannels, mask, hist, mHistSize, mRanges, accumulate);
	    
	    Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX, -1, new Mat());
		
		return hist;
	}
	private Mat getBackproject(Mat hsv){	       
	    Mat backproj = new Mat();
	    MatOfFloat mRanges = new MatOfFloat(0, 179, 0, 255);
	    MatOfInt mChannels = new MatOfInt(0, 1);
	    Imgproc.calcBackProject(Arrays.asList(hsv), mChannels, histROI, backproj, mRanges, 1);
	    return backproj;		
	}

	@Override
	public void broadcast() {
		if(result == null) return;
		try{
			Intent intent = new Intent();
			intent.putExtra("faceTracking", getResultBundle());
			intent.setAction(RobotIntent.CAM_FACE_TRACKING);
			context.sendBroadcast(intent); 				
		}
		catch(Exception e){
			e.printStackTrace();	
		}
	}
}
