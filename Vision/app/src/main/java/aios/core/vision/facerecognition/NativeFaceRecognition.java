package aios.core.vision.facerecognition;

public class NativeFaceRecognition {
    public static native String update(FaceRecognitionService faceRecognitionService, byte[] frame, int width, int height, String faceTrackingResult, String faceRecResult);
}