package com.thanksandroid.example.gcmdemo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {

	private final String TAG = "GcmIntentService";

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	MediaPlayer mediaPlayer = new MediaPlayer();

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		if (!extras.isEmpty()) {

			// read extras as sent from server
			String message = extras.getString("message");
			String serverTime = extras.getString("timestamp");
			sendNotification("Message: " + message + "\n" + "Server Time: "
					+ serverTime);
			Log.i(TAG, "Received: " + extras.toString());
			
			Log.i("HHQ", "Message from server: " + message);
			
			Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

			try {
				 
			      mediaPlayer.setDataSource(getApplicationContext(), defaultRingtoneUri);
			      mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			      mediaPlayer.setLooping(false);
			      mediaPlayer.prepare();
			      mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			         @Override
			         public void onCompletion(MediaPlayer mp)
			         {
			            mp.release();
			         }
			      });
			  mediaPlayer.start();
			  Log.i("HHQ", "Message from server: Notification sound " + message);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}