package com.example.panorama;

public class NativePanorama {
    // TODO tell user to rebuild be for javah
    public native static void processPanorama(long[] imageArray, long outputAddress);
}
