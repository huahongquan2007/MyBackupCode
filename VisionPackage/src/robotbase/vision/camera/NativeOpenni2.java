package robotbase.vision.camera;

public class NativeOpenni2 {

	public static native void InitCamera();
	public static native void GetImage(long address);
	public static native void GetImageByte(byte[] data_byte);
	
	public static void loadlibs(){
		System.loadLibrary("opencv_java");
		System.loadLibrary("NativeOpenni2");
	}
}
