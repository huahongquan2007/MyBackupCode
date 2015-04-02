package com.robotbase.carassistant.cardetection;

public class NativeCarDetection {
    public native static String update(byte[] frame, int width, int height);
    public native static void initCascade(String path);
}
