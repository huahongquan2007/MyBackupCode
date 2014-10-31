package robotbase.vision.camera.memory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Server extends Service{


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		NativeServer.setup();
		int data = 1;
		Log.i("MyLog","Server: Write Data" + data);
		NativeServer.writeData(data);
		return super.onStartCommand(intent, flags, startId);
	}
	
}
