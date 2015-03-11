package aios.core.vision.facerecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import aios.core.vision.baseclasses.VisionBaseClass;
import aios.core.vision.facerecognition.facepp.FaceppAPI;
import aios.core.vision.facerecognition.facepp.FaceppAsyncResponse;
import aios.core.vision.utils.VisionConfig;
import aios.core.vision.utils.VisionUtilities;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class FaceRecognitionService extends VisionBaseClass implements
        FaceppAsyncResponse {
    static{
        Log.d("Vision", "FaceRec: setup()");
        System.loadLibrary("NativeFaceRecognition");
    }
    private boolean needBroadcast = false;
    private String result = "";
    // ----------- Face tracking
    private FaceTrackingReceiver faceTrackingReceiver;
    private String faceTrackingResult = "";
    private long lastFaceTrackingTime = 0;
    private long faceTrackingTime = 0;
    private ReentrantLock faceTrackingLock = new ReentrantLock();

    // ----------- Facepp API
    private FaceppAPI facepp = new FaceppAPI(this);
    private Vector<String> listResult = new Vector<>();
    private ReentrantLock faceRecognitionLock = new ReentrantLock();

    @Override
    protected void setup() {
        // ---- Face tracking
        faceTrackingReceiver = new FaceTrackingReceiver();
        IntentFilter filterFaceTracking = new IntentFilter(aios.core.vision.Intent.ACTION_FACE_TRACKING);
        registerReceiver(faceTrackingReceiver, filterFaceTracking);
    }
    @Override
    protected void finishProcess() {
        unregisterReceiver(faceTrackingReceiver);
    }
    @Override
    protected void update(byte[] frame) {
        try{
            if(faceTrackingLock.tryLock()){
                needBroadcast = false;
                if(lastFaceTrackingTime != faceTrackingTime){
                    String faceRecResult = "[]";
                    if(faceRecognitionLock.tryLock()){
                        if(listResult.size() > 0){
                            faceRecResult = "[";
                            for(int i = 0 ; i < listResult.size() ; i++){
                                if(i == listResult.size() - 1){
                                    faceRecResult += listResult.get(i);
                                }else{
                                    faceRecResult += listResult.get(i) + ",";
                                }
                            }
                            faceRecResult += "]";
                            listResult.clear();
                            Log.d("Vision", "FaceRec: faceRecResult in update " + faceRecResult);
                        }
                        faceRecognitionLock.unlock();
                    }
                    lastFaceTrackingTime = faceTrackingTime;
                    result = NativeFaceRecognition.update(this, frame, width, height, faceTrackingResult, faceRecResult);
                    needBroadcast = true;
                }
                faceTrackingLock.unlock();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void broadcast() {
        if(!result.isEmpty() && needBroadcast){
            try{
                Log.i("Vision", "FaceRec broadcast " + System.currentTimeMillis() + " " + result);
                Intent intent = new Intent();
                intent.putExtra("data", result);
                intent.setAction(aios.core.vision.Intent.ACTION_FACE_RECOGNITION);

                sendBroadcast(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int setupFPS() {
        return 10;
    }



    @Override
    public void updateLastPersonAddTime() {

    }

    @Override
    public void trainFinish() {

    }

    public class FaceTrackingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String data = intent.getStringExtra("data");
            if (data != null) {
//                Log.d("Vision", "FaceRec: FaceTrackingResult: " + data);
                faceTrackingLock.lock();
                faceTrackingResult = data;
                faceTrackingTime = System.currentTimeMillis();
                faceTrackingLock.unlock();
            } else {
                Log.i("Vision", "FaceRec: FaceTrackingResult: NULL");
            }
        }
    }

    public void sendServer(String face_id, int width, int height, byte[] data){
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, data);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        VisionUtilities.saveImage(bitmap);

        facepp.recognitionIdentify(VisionConfig.FACE_REG_GROUP_NAME, bitmap, face_id);
        Log.e("Vision", "SendServer is here " + data.length);
    }
    @Override
    public void processFinish(String output) {
        for(int i = 0 ; i < 10; i++){
            if(faceRecognitionLock.tryLock()){
                Log.d("Vision", "FaceRec: processFinish output: " + output);
                listResult.add(output);
                faceRecognitionLock.unlock();
                return;
            }else{
                try{
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        Log.e("Vision", "FaceRec: processFinish cannot add result " + output);
    }
}