package robotbase.abilities.home_security;

import android.content.Context;
import android.content.Intent;

public class HomeSecurityConfig {
	public static void startService(Context c) {
		c.startService(new Intent(c, FaceRecognitionAlarm.class));
	}

	public static void stopService(Context c) {
		c.stopService(new Intent(c, FaceRecognitionAlarm.class));
	}
}
