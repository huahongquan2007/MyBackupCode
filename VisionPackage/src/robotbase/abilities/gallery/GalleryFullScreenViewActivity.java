package robotbase.abilities.gallery;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import robotbase.action.RobotIntent;
import robotbase.utility.Utilities;
import robotbase.vision.FakeNLP;
import robotbase.vision.R;
import robotbase.vision.TakeAPictureService.ListenRecognitionReceiver;
import robotbase.vision.TakeAPictureService.SERVICE_STATE;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class GalleryFullScreenViewActivity extends Activity {
	private ArrayList<String> imagePaths = new ArrayList<String>();

	private GalleryUtils utils;

	private PagerAdapter mPagerAdapter;

	private ViewPager mPager;

	private ListenRecognitionReceiver listenRecognitionReceiver;
	
	private int curPosition = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gallery_full_screen_view);

		// get intent data

		Intent i = getIntent();

		utils = new GalleryUtils(this);

		// loading all image paths from SD card

		imagePaths = utils.getFilePaths();

		// Selected image id

//		int position = i.getExtras().getInt("id");
		curPosition = i.getExtras().getInt("position");
		
		mPagerAdapter = new FullScreenImageAdapter(this, imagePaths);

		mPager = (ViewPager) findViewById(R.id.pager);

		mPager.setAdapter(mPagerAdapter);

		mPager.setCurrentItem(curPosition);

		// ImageView imageView = (ImageView) findViewById(R.id.imgDisplay);
		// imageView.setImageResource(imageAdapter.mThumbIds[position]);
		
		
		// SETUP NLP
		listenRecognitionReceiver = new ListenRecognitionReceiver();
		IntentFilter filterListen  = new IntentFilter(RobotIntent.SPEECH_RECOGNITION_NLP);
		registerReceiver(listenRecognitionReceiver, filterListen);
		
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(listenRecognitionReceiver);
		super.onDestroy();
	}

	public class ListenRecognitionReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MyLog", "GFSView: ListenRecognition: onReceive");
			
			if (RobotIntent.SPEECH_RECOGNITION_NLP.equals(intent
					.getAction().toString())) { 
				String nlp_data = intent.getStringExtra("data"); 
				
				try {
					JSONObject  NLPObject = Utilities.stringToJSON(nlp_data);
					if (NLPObject.getBoolean("success")){
						
						JSONObject nlp= NLPObject.getJSONObject("nlp");
						
						JSONObject  expression = nlp.getJSONObject("expression");
						String keywords = expression.getString("keywords");
						if( ("computer_vision".equals(expression.getString("provider_name")) == false)){
							return;
						}
						if( ("share_this_photo".equals(expression.getString("name")) == false)){
							return;
						}
						String value = nlp.getJSONObject("params").getJSONObject("network").getString("value");
						// Process Command
						Log.i("MyLog", "GFSView: SHARE THIS PHOTO " + value);
						
						Intent intentSP = new Intent();
						intentSP.putExtra("value", value);
						intentSP.putExtra("data", imagePaths.get(curPosition));
						intentSP.setAction(RobotIntent.SHARE_PHOTO);
						sendBroadcast(intentSP);
					} 
					
				} catch (JSONException e) {
					e.printStackTrace();
				} 
				 
			}
		}
		
	}
}