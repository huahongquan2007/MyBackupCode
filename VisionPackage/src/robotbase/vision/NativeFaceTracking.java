package robotbase.vision;

import android.util.Log;

public class NativeFaceTracking {
	public static String name = "NativeFaceTracking";

	public static native int getPos(byte[] frame, int width, int height);
	public static native void setFaceDetection(int length, int[] x, int[] y, int[] w, int[] h);
	public static native FaceInfo[] getResult();
}
