package aios.core.vision.facedetection;

public class NativeFaceDetectionCV {
    public native static String update(byte[] frame, int width, int height);
    public native static void initCascade(String path);
}
