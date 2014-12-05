package robotbase.utility;

import robotbase.action.RobotIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Notification {
//	public static void dialogNotification(Context context, String title, String content) {
//		Intent dialog = new Intent(context, DialogNotificationActivity.class);
//		if(title != null) {
//			dialog.putExtra("title", title);
//		}
//		dialog.putExtra("content_html", content);
//		context.startActivity(dialog);
//	}
	
	public static void voiceNotification(Context context, String content) {
    	Intent broadcast_intent = new Intent();
    	broadcast_intent.setAction(RobotIntent.TEXT_TO_SPEECH);
    	broadcast_intent.putExtra("text", content);
    	context.sendBroadcast(broadcast_intent);
	}
	public static void voiceNotification(Context context, String[] content) {
    	Intent broadcast_intent = new Intent();
    	broadcast_intent.setAction(RobotIntent.TEXT_TO_SPEECH);
    	broadcast_intent.putExtra("text", content[(int)(Math.random() * content.length + Math.random() * content.length)/2]);
    	context.sendBroadcast(broadcast_intent);
	}	
//	public static void cloudNotification(Context context, String title, String message) {
//		SharedPreferences sp = context.getSharedPreferences("Robotbase", Context.MODE_PRIVATE);
//		String user_id = sp.getString("robotbase_user_id", "");
//		String user_hash = sp.getString("robotbase_user_hash", "");
//		if(title == null) {
//			title = "";
//		}
//		if(!message.isEmpty() && !user_id.isEmpty() && !user_hash.isEmpty()) {
//			RobotBaseApi api = new RobotBaseApi();
//			api.sendCloudNotification(user_id, user_hash, title, message);
//		}
//	}
}
