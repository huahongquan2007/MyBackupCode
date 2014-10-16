package robotbase.vision;

public class NativeMotionDetection{
	public static String name = "NativeMotionDetection";
	
	public static native int[] processFrame(byte[] frame, int width, int height, long l);
}