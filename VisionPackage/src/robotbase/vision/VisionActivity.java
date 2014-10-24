package robotbase.vision;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import robotbase.abilities.FacebookAbility;
import robotbase.abilities.ShowGallery;
import robotbase.abilities.TwitterAbility;
import robotbase.action.NeckServices;
import robotbase.action.RobotIntent;
import robotbase.vision.camera.CameraService;
import robotbase.vision.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class VisionActivity extends Activity {
	private FakeNLP fakeNLP;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vision);

		Log.i("MyLog", "Vision Activity onCreate");

		// Service
		if (VisionConfig.isAndroidCamera)
			startService(new Intent(this, AndroidCameraService.class));
		else
			startService(new Intent(this, CameraService.class));

		startService(new Intent(this, VisionService.class));
		startService(new Intent(this, TakeAPictureService.class));
		startService(new Intent(this, ShowGallery.class));
		startService(new Intent(this, robotbase.speech.TextToSpeech.class));

		startService(new Intent(this, FacebookAbility.class));
		startService(new Intent(this, TwitterAbility.class));
		startService(new Intent(this, NeckServices.class));
		
		// FakeNLP
		fakeNLP = new FakeNLP(this);
		// Button
		Button btnTakePicture = (Button) findViewById(R.id.vision_take_a_picture);
		btnTakePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("take a picture");
            }
        });
		Button btnShowGallery = (Button) findViewById(R.id.vision_show_gallery);
		btnShowGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("show gallery");
            }
        });
		
	}

	@Override
	protected void onDestroy() {
		Log.i("MyLog", "Vision Activity onDestroy");

		stopService(new Intent(this, VisionService.class));

		if (VisionConfig.isAndroidCamera)
			stopService(new Intent(this, AndroidCameraService.class));
		else
			stopService(new Intent(this, CameraService.class));

		stopService(new Intent(this, TwitterAbility.class));
		stopService(new Intent(this, FacebookAbility.class));
		
		stopService(new Intent(this, robotbase.speech.TextToSpeech.class));
		stopService(new Intent(this, TakeAPictureService.class));
		stopService(new Intent(this, ShowGallery.class));
		
		stopService(new Intent(this, NeckServices.class));
		
		super.onDestroy();
	}

}
