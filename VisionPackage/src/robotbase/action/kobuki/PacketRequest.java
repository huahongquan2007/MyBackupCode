package robotbase.action.kobuki;

import java.io.IOException;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class PacketRequest {
	private UsbSerialPort port;
	private UsbDeviceConnection connection;
	private PacketBuilder packetBuilder;

	public PacketRequest(UsbSerialPort port, UsbDeviceConnection connection) {
		this.port = port;
		this.connection = connection;
		packetBuilder = new PacketBuilder();

		openPort();
	}

	public void openPort() {
		try {
			if (port != null) {
				port.open(connection);
				port.setParameters(115200, UsbSerialPort.DATABITS_8,
						UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closePort() {
		if (port != null)
			try {
				port.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void baseControl(int velocity, int radius) throws IOException {
		port.write(packetBuilder.buildBaseControlPacket((short) velocity,
				(short) radius), 20);
	}
}
