package robotbase.vision;

import android.os.Bundle;

public class MotionDetection extends VisionAlgorithm{
	public MotionDetection(float fps) {
		super(fps);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(byte[] frame) {
		NativeMotionDetection.processFrame(frame, frameWidth, frameHeight, pixels.getNativeObjAddr());
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getResultBundle() {
		// TODO Auto-generated method stub
		return null;
	}
}
