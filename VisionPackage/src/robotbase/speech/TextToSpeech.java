package robotbase.speech;

import java.util.Locale;

import robotbase.action.RobotIntent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class TextToSpeech extends Service {

	android.speech.tts.TextToSpeech tts_obj;
	TextReceiver receiver;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.e("MyLog","TTS: onCreate ");
		
		tts_obj = new android.speech.tts.TextToSpeech(getApplicationContext(), new android.speech.tts.TextToSpeech.OnInitListener() {
			
			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
				if(status != android.speech.tts.TextToSpeech.ERROR) {
					tts_obj.setLanguage(Locale.US);
				}
			}
		});
		receiver = new TextReceiver();
		IntentFilter intent_filter = new IntentFilter();
		intent_filter.addAction(RobotIntent.TEXT_TO_SPEECH);
		registerReceiver(receiver, intent_filter);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private void talk(String text) {
		int result = tts_obj.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null);
		Log.e("MyLog", "TTS: onTalk + " + String.valueOf(result));
	}

	public class TextReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.e("MyLog","TTS: onReceive :");
			String text = intent.getExtras().getString("text");
			Log.e("MyLog","TTS: onReceive :" + text);
			talk(text);
		}
	}
}