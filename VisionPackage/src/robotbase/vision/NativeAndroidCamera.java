package robotbase.vision;

public class NativeAndroidCamera {
	public static String name = "NativeAndroidCamera";
	public static native void getFrame(byte[] data, int camWidth, int camHeight, long m, byte[] pixelsByte, byte[] pixelsByteRgb);
}
