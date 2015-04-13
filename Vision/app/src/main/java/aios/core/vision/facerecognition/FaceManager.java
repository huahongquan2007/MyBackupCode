package aios.core.vision.facerecognition;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import aios.core.vision.facerecognition.backup.BackupAPI;
import aios.core.vision.facerecognition.facepp.FaceppAPI;
import aios.core.vision.facerecognition.facepp.FaceppAsyncResponse;
import aios.core.vision.facerecognition.facepp.OnFaceppCompleted;
import aios.core.vision.utils.VisionConfig;

public class FaceManager extends Service implements FaceppAsyncResponse{
    private FaceppAPI facepp = new FaceppAPI(this);
    private BackupAPI backup = new BackupAPI();
    private String GROUPNAME = VisionConfig.FACE_REG_GROUP_NAME;
    public FaceManager() {


    }

    IBinder mBinder = new LocalBinder();

    @Override
    public void processFinish(String output) {

    }

    public class LocalBinder extends Binder {
        public FaceManager getServerInstance() {
            return FaceManager.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addPerson(String person_name){
        Log.d("Vision", "addPerson " + person_name);
        facepp.personCreate(person_name);
        backup.personCreate(person_name);

    }
    public void removePerson(String person_name){
        Log.d("Vision", "removePerson");
        facepp.personDelete(person_name);
        backup.personDelete(person_name);
    }
    public void getPerson(OnFaceManagerTaskCompleted listener, String person_name){
        backup.personGetInfo(listener, person_name);
    }

    OnFaceppCompleted listener = new OnFaceppCompleted() {

        @Override
        public void personAddFace(String person_name, byte[] data, String face_id) {
            backup.personAddFace(person_name, data, face_id);
        }
    };

    public void addFace(String person_name, byte[] data ){
        Log.d("Vision", "addFace");
        facepp.personAddFace( listener, person_name, data );
        // backup.personAddFace is in FaceppCompleted
    }

    public void addFace(String person_name, Bitmap bitmap){
        addFace( person_name, getByteFromBitmap(bitmap) );
    }

    public void removeFace(String person_name, String face_id)
    {
        Log.d("Vision", "removeFace");
        facepp.personDeleteFace(person_name, face_id);
        backup.personDeleteFace(person_name, face_id);
    }

    public void train()
    {
        Log.d("Vision", "train");
        facepp.trainIdentify();
    }

    public static byte[] getByteFromBitmap(Bitmap bmp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
        Log.i("Vision", "FaceManager getByteFromBitmap ");
        return out.toByteArray();
    }
}