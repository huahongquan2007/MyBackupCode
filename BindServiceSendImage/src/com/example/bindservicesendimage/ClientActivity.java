package com.example.bindservicesendimage;

import com.example.bindservicesendimage.Server.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// + Steps for activity
// 1. Create one activity "Client" and create a instance of the "ServiceConnection" Interface
// 2. Implement two methods of this interface onServiceConnected and onServiceDisconnected
// 3. In onServiceConnected method you will get instance of the iBinder so cast it to LocalBinder class which we have created in the service.
// 4. Implement onStart() method and bind the service using bindService() method
// 5. Implement onStop() method and unbind the service using unbindService() method

public class ClientActivity extends Activity {
	boolean mBounded;
	Server mServer;
	TextView text;
	Button button;
	Button buttonImage;
	ImageView imageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);

		text = (TextView) findViewById(R.id.textView1);
		
		//------------- Button Get Text ---------------------
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mBounded) {
					text.setText(mServer.getTime());
				}else{
					text.setText("mBounded = false");
				}
			}
		});

		Intent mIntent = new Intent(this, Server.class);
		bindService(mIntent, mConnection, BIND_AUTO_CREATE);
		
		//------------- Button Get Image ---------------------
		imageView = (ImageView) findViewById(R.id.imageView1);
		buttonImage = (Button) findViewById(R.id.buttonImage);
		buttonImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBounded) {
					text.setText("Set Bitmap Success!");
					imageView.setImageBitmap(mServer.getFrame());
				}else{
					text.setText("mBounded = false");
				}
			}
		});		
	}

	ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(ClientActivity.this, "Service is disconnected", 1000)
					.show();
			mBounded = false;
			mServer = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(ClientActivity.this, "Service is connected", 1000)
					.show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			mServer = mLocalBinder.getServerInstance();
		}
	};

	@Override
	protected void onStart() {
		super.onStart();		
	}

	@Override
	protected void onStop() {
		if (mBounded) {
			unbindService(mConnection);
			mBounded = false;
		}

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
