package robotbase.vision;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import robotbase.action.RobotIntent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class FakeNLP {
	Context context;
	public FakeNLP(Context c){
		context = c;
	}
	public void command(String c){
		new Providers(c).execute();
	}
	/* Hard code command */
	public String processNLP(String command) {

		Log.i("NLP", command);

		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> response_handler = new BasicResponseHandler();
		try {
			command = URLEncoder.encode(command, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			command = "";
		}
		HttpGet request = new HttpGet(
				"http://robotbasecloud-env-kq6xxhigdt.elasticbeanstalk.com/nlp/analyze?user_id=1&text="
						+ command);
		String result = "";
		try {
			result = client.execute(request, response_handler);
			return result;
			// broadcastNLP(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void broadcastNLP(String nlp_data) {
		Log.i("NLP", "Broadcast");
		Intent broadcast_intent = new Intent();
		broadcast_intent.setAction(RobotIntent.SPEECH_RECOGNITION_NLP);
		broadcast_intent.putExtra("data", nlp_data);
		context.sendBroadcast(broadcast_intent);
		Log.i("NLP", nlp_data);
	}
	// asyn task
	private class Providers extends AsyncTask<String, String, String> {

		private ProgressDialog dialog;
		private Activity activity;
		private String command;

		public Providers(String command) {
			this.command = command;
			this.activity = (Activity)context;
			dialog = new ProgressDialog(this.activity);
			this.dialog.setProgressStyle(this.dialog.STYLE_SPINNER);

		}

		@Override
		protected void onPreExecute() {
			Log.d("debug", "test on onPreExecute");
			this.dialog.setMessage("Loading ...");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(String source) {

			if (source != null) {

				broadcastNLP(source);
			} else {
				Toast.makeText(context, "Source is NULL",
						Toast.LENGTH_LONG).show();
			}

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String source = processNLP(command);
				if (source != null) {
					return source;
				}
				return null;
			} catch (Exception e) {
				return null;
			}
		}
	}
	/* End hardcode command */
}
