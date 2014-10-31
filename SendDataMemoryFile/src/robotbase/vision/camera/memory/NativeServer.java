package robotbase.vision.camera.memory;

public class NativeServer {
	static{
		System.load("native-camera-server");
	}
	public static native void setup();
	public static native void writeData(int data);
}
