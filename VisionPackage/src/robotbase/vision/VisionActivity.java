package robotbase.vision;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import robotbase.abilities.ShowGallery;
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

		// Button
		Button btnTakePicture = (Button) findViewById(R.id.vision_take_a_picture);
		btnTakePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	new Providers("take a picture").execute();
            }
        });
		Button btnShowGallery = (Button) findViewById(R.id.vision_show_gallery);
		btnShowGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	new Providers("show gallery").execute();
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

		stopService(new Intent(this, robotbase.speech.TextToSpeech.class));
		stopService(new Intent(this, TakeAPictureService.class));
		stopService(new Intent(this, ShowGallery.class));
		super.onDestroy();
	}

	/* Hard code command */
	public String processNLP(String command) {

		Log.i("NLP", command);

		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> response_handler = new BasicResponseHandler();
		try {
			command = URLEncoder.encode(command, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			command = "";
		}
		HttpGet request = new HttpGet(
				"http://robotbasecloud-env-kq6xxhigdt.elasticbeanstalk.com/nlp/analyze?user_id=1&text="
						+ command);
		String result = "";
		try {
			result = client.execute(request, response_handler);
			return result;
			// broadcastNLP(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void broadcastNLP(String nlp_data) {
		Log.i("NLP", "Broadcast");
		Intent broadcast_intent = new Intent();
		broadcast_intent.setAction(RobotIntent.SPEECH_RECOGNITION_NLP);
		broadcast_intent.putExtra("data", nlp_data);
		sendBroadcast(broadcast_intent);
		Log.i("NLP", nlp_data);
	}
	// asyn task
	private class Providers extends AsyncTask<String, String, String> {

		private ProgressDialog dialog;
		private Activity activity;
		private String command;

		public Providers(String command) {
			this.command = command;
			this.activity = VisionActivity.this;
			dialog = new ProgressDialog(this.activity);
			this.dialog.setProgressStyle(this.dialog.STYLE_SPINNER);

		}

		@Override
		protected void onPreExecute() {
			Log.d("debug", "test on onPreExecute");
			this.dialog.setMessage("Loading ...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(String source) {

			if (source != null) {

				broadcastNLP(source);
			} else {
				Toast.makeText(getApplicationContext(), "Source is NULL",
						Toast.LENGTH_LONG).show();
			}

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String source = processNLP(command);
				if (source != null) {
					return source;
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}
	}
	/* End hardcode command */
}
