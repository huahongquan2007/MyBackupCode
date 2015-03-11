package aios.core.vision.facerecognition.facepp;

import java.util.HashMap;

public class BackUpParam{
	public BackUpParam(MODE m) {
		mode = m;
	}
	public static enum MODE{
		PERSON_CREATE, PERSON_DELETE, PERSON_ADD_FACE, PERSON_GET_INFO , PERSON_SET_INFO
		};
	private MODE mode;
	private HashMap<String, String> data = new HashMap<String, String>();
	
	public void setData(String key, String value) {
		data.put(key, value);
	}

	public MODE getMode() {
		return mode;
	}

	public String getData(String key) {
		if(data.containsKey(key)){
			return data.get(key);
		}
		return "";
	}
}