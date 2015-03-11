package aios.core.vision.facelearning;
import android.util.Log;

import aios.core.vision.baseclasses.VisionBaseClass;

public class FaceLearningService extends VisionBaseClass {
    private String result;
    static{
        Log.d("Vision", "FaceRec: setup()");
        System.loadLibrary("NativeFaceLearning");
    }
    @Override
    protected void setup() {

    }

    @Override
    protected void finishProcess() {

    }

    @Override
    protected void update(byte[] frame) {
        result = NativeFaceLearning.update(frame);
        Log.d("Vision", "FaceLearning update: " + result);
    }

    @Override
    protected void broadcast() {

    }

    @Override
    protected int setupFPS() {
        return 10;
    }
}