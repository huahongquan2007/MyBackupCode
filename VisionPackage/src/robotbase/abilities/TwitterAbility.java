package robotbase.abilities;

import java.io.File;

import org.json.JSONException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import robotbase.action.RobotIntent;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAbility extends Service implements FactoryAbility {
	class Emotion{
		public static final String SHAKE = "";
		public static final String SMILE = "";
	}
	static String ConsumerKey = "otcWdCrJA7Gjw5SpSF36qg";
	static String ConsumerSecret = "nHFaiFFQvfKrCHlNOVruSQ7cdtKQNiFlHtWozvd9mY0";

	public ResultDisplay result;

	private Receiver receiver = new Receiver();
	private NLPModel nlp_data = new NLPModel();

	private ShareReceiver shareReceiver = new ShareReceiver();
	private ConfigurationBuilder configBuilder = new ConfigurationBuilder();

	private class ShareReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.hasExtra("value")){
				String value = intent.getStringExtra("value");			
				String path = intent.getStringExtra("data");
				if(value.toLowerCase().equals("all") || value.toLowerCase().equals("twitter")){
					Log.i("MyLog","TW: Share onReceive : " + value);				
					Twitter twitter = new TwitterFactory(configBuilder.build()).getInstance();
					try {
						StatusUpdate status = new StatusUpdate("");
						status.setMedia(new File(path)); // set the image to be uploaded here.
						twitter.updateStatus(status);
					} catch (TwitterException e) {
						e.printStackTrace();
					}					
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
				TwitterAbility.this.nlp_data = new NLPModel(data);
				if (TwitterAbility.this.nlp_data != null
						&& TwitterAbility.this.nlp_data.success
						&& "twitter"
								.equals(TwitterAbility.this.nlp_data.nlp.expression.provider_name
										.toLowerCase())) {
					processData();
				}
			}
		}

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		IntentFilter filter = new IntentFilter(
				RobotIntent.SPEECH_RECOGNITION_NLP);
		registerReceiver(receiver, filter);

		result = new ResultDisplay(0, "twitter", Emotion.SHAKE, "failure", "");
		
		
		configBuilder.setDebugEnabled(true).setOAuthConsumerKey(ConsumerKey)
		  .setOAuthConsumerSecret(ConsumerSecret)
		  .setOAuthAccessToken("911974986-xamAyQ0WnUJGlx8L6wM0gf3QdAKq6uIBAhrdbmWq")
		  .setOAuthAccessTokenSecret("dbgj59f2JoFoCACoEtvbJwiaOx71cG6fwKknASUuGpBQ5");
		
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}

		// SETUP Share Command
		IntentFilter filterShare  = new IntentFilter(RobotIntent.SHARE_PHOTO);
		registerReceiver(shareReceiver, filterShare);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void processData() {
		if (nlp_data != null && nlp_data.success && nlp_data.nlp != null
				&& nlp_data.nlp.user_app != null
				&& !nlp_data.nlp.user_app.token.isEmpty()
				&& !nlp_data.nlp.user_app.secret.isEmpty()
				&& nlp_data.nlp.params.has("message")) {
			try {
				String token = nlp_data.nlp.user_app.token;
				String secret = nlp_data.nlp.user_app.secret;
				String message = nlp_data.nlp.params.getJSONObject("message")
						.getString("value");

				TwitterFactory factory = new TwitterFactory();

				AccessToken accessToken = new AccessToken(token, secret);

				Twitter twitter = factory.getInstance();
				twitter.setOAuthConsumer(ConsumerKey, ConsumerSecret);
				twitter.setOAuthAccessToken(accessToken);

				try {
					Status statusObj = twitter.updateStatus(message);
					result = new ResultDisplay(1, "twitter", Emotion.SMILE, "done", "ok");
					Log.d("debug", "Done");
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					result = new ResultDisplay(0, "twitter", Emotion.SHAKE,
							"failure", e.getErrorMessage());
					Log.d("debug", "failure "+ e.getErrorMessage());
					e.printStackTrace();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = new ResultDisplay(0, "twitter", Emotion.SHAKE,
						"failure", "");
				Log.d("debug", "failure JSONException");
			}
			broadcastIntent();
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
		Intent sentIntent = new Intent(RobotIntent.DISPLAY_SHOW_EMOTION);
		sentIntent.putExtra(RobotIntent.RESULT_DATA, result);
		this.sendBroadcast(sentIntent);

		return true;
	}

	@Override
	public boolean broadcastIntent() {
		// TODO Auto-generated method stub
		Intent sentIntent = new Intent(RobotIntent.DISPLAY_SHOW_EMOTION); 
        sentIntent.putExtra(RobotIntent.RESULT_DATA, result); 
		this.sendBroadcast(sentIntent); 
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