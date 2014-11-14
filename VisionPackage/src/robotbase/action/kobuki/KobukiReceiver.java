package robotbase.action.kobuki;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class KobukiReceiver extends BroadcastReceiver {

	private PacketRequest PacketRequest;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle data = intent.getBundleExtra(KobukiConstanst.BUNDLE_KEY);
		KobukiCommand command = (KobukiCommand) KobukiCommand.values()[data.getInt(KobukiConstanst.COMMAND_KEY)];
		Log.d(KobukiConstanst.BASE, command.getCommand());

		int v_linear = 70;
		int v_angular = 75;
		try {
			switch (command) {
			case FORWARD:
				PacketRequest.baseControl(v_linear, 0);
				break;
			case BACKWARD:
				PacketRequest.baseControl(-v_linear, 0);
				break;
			case LEFT:
				PacketRequest.baseControl(v_linear, v_angular);
				break;
			case RIGHT:
				PacketRequest.baseControl(v_linear, -v_angular);
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PacketRequest getPacketRequest() {
		return PacketRequest;
	}

	public void setPacketRequest(PacketRequest packetRequest) {
		PacketRequest = packetRequest;
	}

}
