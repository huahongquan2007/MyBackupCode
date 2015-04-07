package aios.core.vision;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import aios.core.vision.activity.CameraPreviewActivity;
import aios.core.vision.facerecognition.FaceManager;
import aios.core.vision.facerecognition.OnFaceManagerTaskCompleted;
import aios.core.vision.utils.VisionConfig;

public class StartVisionService extends ActionBarActivity {
    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("opencv_java");
        System.loadLibrary("jsoncpp");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_vision_service);
    }

    public void onStartServiceClicked(View v){
        Log.d("Vision", "onStartServiceClicked");
        Intent intent = new Intent("aios.core.vision");
        intent.setPackage("aios.core.vision");
        startService(intent);
    }

    public void onBindFaceManagerClicked(View v){
        Log.d("Vision", "onBindFaceManagerClicked");
        bindService(new Intent(getApplicationContext(), FaceManager.class), mConnection, Context.BIND_AUTO_CREATE);
    }
    public void onProcessFaceManagerClicked(View v){
        try{
            Log.d("Vision", "onProcessFaceManagerClicked");
            mFaceManager.addPerson("hong quan");

            mFaceManager.getPerson(listener, "hong quan");

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.taylor_swift);
            mFaceManager.addFace("hong quan", bitmap);
            mFaceManager.removeFace("hong quan", "face_id");

            mFaceManager.removePerson("hong quan");
            mFaceManager.train();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
        }
    }
    public void onUnbindFaceManagerClicked(View v){
        Log.d("Vision", "onUnbindFaceManagerClicked");
        unbindService(mConnection);
    }
    // TaskCompleted

    OnFaceManagerTaskCompleted listener = new OnFaceManagerTaskCompleted() {
        @Override
        public void onTaskCompleted(String result) {
            Log.d("Vision", "onTaskCompleted " + result);
        }
    };
    // Connection to Bind
    boolean mBounded;
    FaceManager mFaceManager;
    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mFaceManager = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            FaceManager.LocalBinder mLocalBinder = (FaceManager.LocalBinder) service;
            mFaceManager = mLocalBinder.getServerInstance();

            Log.d("Vision", "onTestManagerClicked onServiceConnected");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d("Vision", "onStartService Activity onDestroy");
        VisionConfig.stopService(getApplicationContext());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_vision, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, CameraPreviewActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
