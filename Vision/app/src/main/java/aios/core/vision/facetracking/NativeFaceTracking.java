package aios.core.vision.facetracking;

public class NativeFaceTracking {
    public static native String update(byte[] frame, int width, int height, String faceDetectionString);
}
