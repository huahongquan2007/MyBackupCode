package robotbase.vision;

public class NativeFaceDetection {
	
	public static String name = "NativeFaceDetection";
	
	public static native void initCascade(String absolutePath);

	public static native int getPos(byte[] frame, int[] x, int[] y, int[] w, int[] h, long l);

	public static native void setPreviewSize(int s_width, int s_height);
}
