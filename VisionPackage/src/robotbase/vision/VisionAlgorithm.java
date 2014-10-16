package robotbase.vision;

import java.io.ByteArrayOutputStream;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

public abstract class VisionAlgorithm {
	protected String name;
	protected Mat pixels = null;
	protected long lastUpdate;
	protected long frequence;
	protected int[] patch;
	
	protected int frameWidth = 320, frameHeight = 240; 
	
	public VisionAlgorithm(float fps){
		name = this.getClass().getSimpleName();
		frequence = (long) (1000 / fps);
		lastUpdate = System.currentTimeMillis();
		
		pixels = new Mat(frameHeight, frameWidth, CvType.CV_8UC4);
		int size = (int) (pixels.total()  * pixels.channels());
        patch = new int[size];   
	}
	public String getName(){
		return name;
	}
	public void update(byte[] frame){
		long curTime = System.currentTimeMillis();
		if(curTime - lastUpdate > frequence){
			lastUpdate = curTime;
			run(frame);
		}
	}
	public abstract Bundle getResultBundle();
	public Bitmap getFrame(){
		Bitmap frame = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);

		if(pixels != null && !pixels.empty())
		{
			Utils.matToBitmap(pixels, frame);
//			Log.e("MyLog", "Utils mat To bitmap");
		}
		return frame;
	}
	public Mat getMat(){
		return pixels;
	}
	public int[] getIntArray(){
		Log.e("MyLog", "Get Int Array: " + String.valueOf(patch.length));
		return patch;
	}
	public byte[] getFrameArray(){
		Bitmap bitmap = getFrame();
	
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
 	    
		return bStream.toByteArray();
	}
	abstract public void start();
	abstract public void stop();
	abstract public void run(byte[] frame);
	abstract public String getResult();
}