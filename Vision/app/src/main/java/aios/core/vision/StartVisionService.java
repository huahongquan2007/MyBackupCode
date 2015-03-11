package aios.core.vision;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;

import aios.core.vision.activity.CameraPreviewActivity;
import aios.core.vision.utils.VisionConfig;

public class StartVisionService extends ActionBarActivity {
    static {
        System.loadLibrary("opencv_java");
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
