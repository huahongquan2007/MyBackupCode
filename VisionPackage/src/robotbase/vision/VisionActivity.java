package robotbase.vision;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import robotbase.abilities.home_security.HomeSecurityConfig;
import robotbase.action.NeckServices;
import robotbase.action.kobuki.KobukiService;
import robotbase.speech.TextToSpeech;
import robotbase.utility.Utilities;
import robotbase.vision.facepp.FaceppParam;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class VisionActivity extends Activity {
	private FakeNLP fakeNLP;
	private boolean isSerial = VisionConfig.USE_SERIAL;
	private EditText editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vision);

		Log.i("MyLog", "Vision Activity onCreate");

		// Service
		VisionConfig.startService(this);
		
//		HomeSecurityConfig.startService(this);
		
		startService(new Intent(this, TextToSpeech.class));
//		startService(new Intent(this, FacebookAbility.class));
//		startService(new Intent(this, TwitterAbility.class));
		
		if(isSerial){
			startService(new Intent(this, NeckServices.class));
			startService(new Intent(this, KobukiService.class));			
		}
		
		// FakeNLP
		fakeNLP = new FakeNLP(this);
		// Button
		Button btnTakePicture = (Button) findViewById(R.id.vision_take_a_picture);
		btnTakePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("take picture");
            }
        });
		Button btnShowGallery = (Button) findViewById(R.id.vision_show_gallery);
		btnShowGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("show gallery");
            }
        });	
		Button btnRemember = (Button) findViewById(R.id.vision_remember_me);
		btnRemember.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	fakeNLP.command("remember me");
            }
        });			
		
		 editText = (EditText) findViewById(R.id.vision_create_person_name);
		
		Button btnSendBitmap = (Button) findViewById(R.id.vision_send_bitmap);
		btnSendBitmap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InputStream is = getResources().openRawResource(R.raw.test);

				Bitmap bitmap= BitmapFactory.decodeStream(is);
				Log.e("MyLog", "WIDTH RAW: " + bitmap.getWidth());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] imageBytes = baos.toByteArray();
				String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
				
				new SendBitmapTask().execute(encodedImage);

			}
		});
		Button btnCreatePerson = (Button) findViewById(R.id.vision_create_person);
		btnCreatePerson.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				new CreatePersonTask().execute(editText.getText().toString());

			}
		});
	}
	private class CreatePersonTask extends
	AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try{
				String personName = params[0];
				String API_URL = "http://192.168.1.127:5000/api/vision/person/create";
				String dataSend = API_URL;
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				list.add(new BasicNameValuePair("person_name", personName));
				list.add(new BasicNameValuePair("user_id", "1"));
				Utilities.callAPI(dataSend, list, "POST");			
				Log.e("MyLog", "dataSend: " + dataSend);				
			}catch(Exception e){
				
			}

			
			return null;
		}
		
	}
	private class SendBitmapTask extends
	AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String encodedImage = params[0];
			String personName = "User-01[RB]quan";
			
			String API_URL = "http://192.168.1.127:5000/api/vision/person/face/add";
			String dataSend = API_URL;
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("person_name", personName));
			list.add(new BasicNameValuePair("data", encodedImage));
			
			Utilities.callAPI(dataSend, list, "POST");			
			Log.e("MyLog", "dataSend: " + dataSend);
			return null;
		}
		
	}
	@Override
	protected void onDestroy() {
		Log.i("MyLog", "Vision Activity onDestroy");
		VisionConfig.stopService(this);
		HomeSecurityConfig.stopService(this);
		
//		stopService(new Intent(this, TwitterAbility.class));
//		stopService(new Intent(this, FacebookAbility.class));
		stopService(new Intent(this, TextToSpeech.class));
		
		if(isSerial){
			stopService(new Intent(this, NeckServices.class));
			stopService(new Intent(this, KobukiService.class));			
		}

		super.onDestroy();
	}
}