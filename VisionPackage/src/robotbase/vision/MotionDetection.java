//package robotbase.vision;
//import android.content.Context;
//import android.os.Bundle;
//
//public class MotionDetection extends VisionAlgorithm{
//	public MotionDetection(float fps, Context ctx) {
//		super(fps, ctx);
//	}
//
//	@Override
//	public void start() {
//		System.loadLibrary(NativeMotionDetection.name);
//	}
//
//	@Override
//	public void stop() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void run(byte[] frame) {
//		NativeMotionDetection.processFrame(frame, frameWidth, frameHeight, pixels.getNativeObjAddr());
//	}
//
//	@Override
//	public Bundle getResultBundle() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void broadcast() {
//		// TODO Auto-generated method stub
//		
//	}
//}
