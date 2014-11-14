package robotbase.action.kobuki;

import robotbase.action.kobuki.KobukiConstanst;
import robotbase.action.kobuki.KobukiReceiver;
import robotbase.action.kobuki.PacketRequest;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Log;

public class KobukiService extends Service {

	private final KobukiReceiver kobukiReceiver = new KobukiReceiver();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(KobukiConstanst.BASE, "START SERVICE");
		
		Log.d(KobukiConstanst.BASE, "GET SERIAL PORT");
		UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		UsbSerialProber prober = UsbSerialProber.getDefaultProber();
		// List<UsbSerialDriver> availableDrivers = prober
		// .findAllDrivers(usbManager);

		for (UsbDevice device : usbManager.getDeviceList().values()) {
			if ((device.getVendorId() == 0x0403)
					&& (device.getProductId() == 0x6002)) {
				UsbDevice serialDevice = device;
				UsbSerialDriver serialDriver = prober.probeDevice(serialDevice);
				UsbSerialPort port = serialDriver.getPorts().get(0);
				UsbDeviceConnection connection = usbManager.openDevice(port
						.getDriver().getDevice());
				if (connection == null) {
					Log.d("SERIAL", "Open failed");
					stopSelf();
				} else {
					Log.d("SERIAL", "Open Success");
					kobukiReceiver.setPacketRequest(new PacketRequest(port,
							connection));
					break;
				}
				stopSelf();
			}
		}
		IntentFilter intentMove = new IntentFilter(KobukiConstanst.COMMAND_MOVE);
		
		registerReceiver(kobukiReceiver, intentMove);
		
		Log.d(KobukiConstanst.BASE, "REGISTER RECEIVER");
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(kobukiReceiver);
		Log.d(KobukiConstanst.BASE, "DESTROY SERVICE");
	}
}
