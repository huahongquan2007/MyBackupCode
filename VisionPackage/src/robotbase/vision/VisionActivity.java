package robotbase.vision;

import robotbase.action.NeckServices;
import robotbase.action.kobuki.KobukiService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class VisionActivity extends Activity {
	private FakeNLP fakeNLP;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vision);

		Log.i("MyLog", "Vision Activity onCreate");

		// Service
		VisionConfig.startService(this);

		
		startService(new Intent(this, robotbase.speech.TextToSpeech.class));
//		startService(new Intent(this, FacebookAbility.class));
//		startService(new Intent(this, TwitterAbility.class));
		startService(new Intent(this, NeckServices.class));
		startService(new Intent(this, KobukiService.class));
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
	}

	@Override
	protected void onDestroy() {
		Log.i("MyLog", "Vision Activity onDestroy");

		stopService(new Intent(this, NeckServices.class));
		VisionConfig.stopService(this);

//		stopService(new Intent(this, TwitterAbility.class));
//		stopService(new Intent(this, FacebookAbility.class));
		stopService(new Intent(this, robotbase.speech.TextToSpeech.class));

		
		super.onDestroy();
	}
}