package robotbase.abilities;

import org.json.JSONException;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import robotbase.abilities.gallery.GalleryFullScreenViewActivity.ListenRecognitionReceiver;
import robotbase.action.RobotIntent;
//import robotbase.face.Emotion;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;


// Xoa Emotion o facebook ability + result Display
// Chinh ham broadcast Intent the hien thi Toast thay vi public intent

public class FacebookAbility extends Service implements FactoryAbility {
	
	class Emotion{
		public static final String SHAKE = "";
		public static final String SMILE = "";
	}
	//private String api_key = "476548449112697";
	//private String secret = "74f4c08650b4451937c973f24fb6d550";
	
	private String api_key = "285857431513811";
	private String secret = "ff76a833adeae4bdaa0e244152d879da";

	public ResultDisplay result;

	private Receiver receiver = new Receiver();
	private ShareReceiver shareReceiver = new ShareReceiver();
	
	private NLPModel nlp_data = new NLPModel();
	
	private class ShareReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog","FB: Share onReceive");
			if(intent.hasExtra("value")){
				String value = intent.getStringExtra("value");			
				if(value.toLowerCase().equals("all") || value.toLowerCase().equals("facebook")){
					Log.i("MyLog","FB: Share onReceive : " + value);
				}
			}
		}
	}
	private class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.hasExtra("data")) {
				String data = intent.getStringExtra("data");
				FacebookAbility.this.nlp_data = new NLPModel(data);
				if (FacebookAbility.this.nlp_data != null
						&& FacebookAbility.this.nlp_data.success
						&& "facebook"
								.equals(FacebookAbility.this.nlp_data.nlp.expression.provider_name
										.toLowerCase())) {
					processData();
				}
			}
		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("MyLog","FB: Share onCreate");
		
		IntentFilter filter = new IntentFilter(
				RobotIntent.SPEECH_RECOGNITION_NLP);
		registerReceiver(receiver, filter);

		// SETUP Share Command
		IntentFilter filterShare  = new IntentFilter(RobotIntent.SHARE_PHOTO);
		registerReceiver(shareReceiver, filterShare);
		
		
		result = new ResultDisplay(0, "facebook", Emotion.SHAKE, "", "");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void processData() {
		if (nlp_data != null && nlp_data.success && nlp_data.nlp != null && nlp_data.nlp.user_app != null
				&& nlp_data.nlp.user_app.token != null
				&& nlp_data.nlp.params.has("message")) {
			try {
				String token = nlp_data.nlp.user_app.token;
				String message = nlp_data.nlp.params.getJSONObject("message")
						.getString("value");

				Facebook facebook = new FacebookFactory().getInstance();
				facebook.setOAuthAppId(api_key, secret);
				facebook.setOAuthAccessToken(new AccessToken(token, null));
				try {
					facebook.postStatusMessage(message);
					result = new ResultDisplay(1, "facebook", Emotion.SMILE, "done", "Your status is posted on your timeline");
				} catch (FacebookException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = new ResultDisplay(0, "facebook", Emotion.SHAKE, "failure", e.getErrorMessage());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = new ResultDisplay(0, "facebook", Emotion.SHAKE, "failure", "");
			}
			this.broadcastIntent();
		}
	}

	@Override
	public boolean install() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAuthorize() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDevices() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeDevices() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerIntent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean broadcastIntent() {
		
//		Intent sentIntent = new Intent(RobotIntent.DISPLAY_SHOW_EMOTION);
//		sentIntent.putExtra(RobotIntent.RESULT_DATA, result);
//		this.sendBroadcast(sentIntent);

		return true;
	}

	@Override
	public boolean onExcute(Context context, Intent intent) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}