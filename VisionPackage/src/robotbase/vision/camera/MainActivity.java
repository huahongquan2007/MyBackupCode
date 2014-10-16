//package robotbase.vision.camera;
//
//import java.util.Timer;
//
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ImageView;
//
//public class MainActivity extends Activity {
//	
//	ImageView view;
//	
//	class CameraReceiver extends BroadcastReceiver {
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Bitmap data;
//			data = (Bitmap)intent.getParcelableExtra(CameraService.CAMERA_DATA);
//			view.setImageBitmap(data);
//		}
//		
//	}
//	
//	private CameraReceiver mCameraReceiver = new CameraReceiver();
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
////		Native.loadlibs();
////		Native.InitCamera();
//		
//		view = (ImageView) findViewById(R.id.image);
//		
//		Intent camera_intent = new Intent(this, CameraService.class);
//		startService(camera_intent);
//		
//		IntentFilter intentFilter = new IntentFilter(CameraService.CAMERA_INTENT);
//		registerReceiver(mCameraReceiver, intentFilter);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	Timer timer = new Timer();
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
////			timer= new Timer();
////			timer.schedule(new GetImageTask(), 0, 40);
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	public void buttonClick(View view) {
//	}
//	
////	class GetImageTask extends TimerTask {
////
////		@Override
////		public void run() {
////			// TODO Auto-generated method stub
//////			Mat data = new Mat(240, 320, CvType.CV_8UC3);
////			Mat data = new Mat();
////			Native.GetImage(data.getNativeObjAddr());
////
////			if (data.empty())
////				return;
////
////			if (data.cols() <= 100 && data.rows() <= 100)
////				return;
////
////			final Bitmap data_bmp = Bitmap.createBitmap(data.width(), data.height(),
////					Bitmap.Config.ARGB_8888);
////			Utils.matToBitmap(data, data_bmp);
////
////			runOnUiThread(new Runnable() {
////				public void run() {
////					ImageView image = (ImageView) findViewById(R.id.image);
////					image.setImageBitmap(data_bmp);
////				}
////			});
////		}
////		
////	}
//}
