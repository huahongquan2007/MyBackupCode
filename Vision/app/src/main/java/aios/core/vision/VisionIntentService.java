package aios.core.vision;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import aios.core.vision.utils.VisionConfig;

public class VisionIntentService extends IntentService {

    public VisionIntentService() {
        super("VisionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("Vision", "onHandleIntent VisionIntentService");
            VisionConfig.startService(getApplicationContext());
        }
    }
}
