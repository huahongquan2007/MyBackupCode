package robotbase.abilities;

import org.json.JSONException;
import org.json.JSONObject;

import robotbase.action.RobotIntent;
import robotbase.utility.Utilities;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ShowGallery  extends Service{
	private ListenRecognitionReceiver listenRecognitionReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("MyLog", "Show Gallery Service: ListenRecognition: onStart");
		listenRecognitionReceiver = new ListenRecognitionReceiver();
		
		IntentFilter filterListenVision  = new IntentFilter(RobotIntent.SPEECH_RECOGNITION_NLP);
		registerReceiver(listenRecognitionReceiver, filterListenVision);
	}
	@Override
	public void onDestroy() {
		unregisterReceiver(listenRecognitionReceiver);
		super.onDestroy();
	}
	public class ListenRecognitionReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog", "Show Gallery Service: ListenRecognition: onReceive");
			
//			Log.e("MyNLP", "NLP onReceive TAP");
//			String data = intent.getStringExtra("data");
//			if(data.equals("show_gallery")){
//				Intent dialogIntent = new Intent(getBaseContext(), robotbase.abilities.gallery.GalleryActivity.class);
//				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				getApplication().startActivity(dialogIntent);	
//			}else{
//				Log.e("MyLog"," NLP ERROR : " + data);
//			}
//			return;
//			
			
			if (RobotIntent.SPEECH_RECOGNITION_NLP.equals(intent
					.getAction().toString())) { 
				String nlp_data = intent.getStringExtra("data"); 
				Log.i("MyLog", "Show Gallery Service: NLP " + nlp_data);
				try {
					JSONObject  NLPObject = Utilities.stringToJSON(nlp_data);
					
					if (NLPObject.getBoolean("success")){
						
						JSONObject nlp= NLPObject.getJSONObject("nlp");
						
						JSONObject  expression = nlp.getJSONObject("expression");
						if( ("computer_vision".equals(expression.getString("provider_name")) == false)){
							return;
						}
						if( ("show_gallery".equals(expression.getString("name")) == false)){
							return;
						}
						// Process Command
						Intent dialogIntent = new Intent(getBaseContext(), robotbase.abilities.gallery.GalleryActivity.class);
						dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getApplication().startActivity(dialogIntent);
					} 
					
				} catch (JSONException e) {
					e.printStackTrace();
				} 
				 
			}
			
		}
	}
}
