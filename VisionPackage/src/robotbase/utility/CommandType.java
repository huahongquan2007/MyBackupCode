package robotbase.utility;

import java.util.List;

public class CommandType {
	private String keyToStartGetDevice;
	private String keyToEndGetDevice;
	private String keyToDetect;
	private List<String> controllerClasses;
	private String deviceName;
	private String action;
	
	public CommandType(String keyToStartGetDevice, String keyToEndGetDevice, String keyToDetect, String action, List<String> controller){
		this.setKeyToStartGetDevice(keyToStartGetDevice);
		this.setKeyToEndGetDevice(keyToEndGetDevice);
		this.setKeyToDetect(keyToDetect);
		this.setAction(action);
		this.setControllerClasses(controller);
	}

	public String getKeyToStartGetDevice() {
		return keyToStartGetDevice;
	}

	public void setKeyToStartGetDevice(String keyToStartGetDevice) {
		this.keyToStartGetDevice = keyToStartGetDevice;
	}

	public String getKeyToDetect() {
		return keyToDetect;
	}

	public void setKeyToDetect(String keyToDetect) {
		this.keyToDetect = keyToDetect;
	}

	public String getKeyToEndGetDevice() {
		return keyToEndGetDevice;
	}

	public void setKeyToEndGetDevice(String keyToEndGetDevice) {
		this.keyToEndGetDevice = keyToEndGetDevice;
	}

	public List<String> getControllerClasses() {
		return controllerClasses;
	}

	public void setControllerClasses(List<String> controllerClass) {
		this.controllerClasses = controllerClass;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
