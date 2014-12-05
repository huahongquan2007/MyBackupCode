package robotbase.vision.facepp;

import java.util.Vector;

import android.graphics.Bitmap;

import com.facepp.http.PostParameters;

public class FaceppParam {
	public static enum MODE{
		PERSON_CREATE, PERSON_DELETE, PERSON_ADD_FACE, PERSON_ADD_FACE_ID, PERSON_ADD_MANY_FACE, PERSON_GET_INFO ,
		GROUP_CREATE, GROUP_DELETE, GROUP_ADD_PERSON, GROUP_GET_INFO,
		RECOGNITION_IDENTIFY,
		TRAIN_IDENTIFY};
	private MODE mode;
	private PostParameters param = null;
	private byte[] data = null;
	private String keyName = "";
	
	public FaceppParam(MODE _m) {
		setMode(_m);
	}

	public FaceppParam(MODE _m, PostParameters _p) {
		this(_m);
		setParam(_p);
	}

	public FaceppParam(MODE _m, PostParameters _p, byte[] input) {
		this(_m, _p);
		setData(input);
	}
	
	public FaceppParam(MODE _m, PostParameters _p, byte[] input, String keyName) {
		this(_m, _p);
		setData(input);
		setKeyName(keyName);
	}

	public void setData(byte[] input) {
		data = input.clone();
	}

	public void setParam(PostParameters _p) {
		param = _p;
	}

	public void setMode(MODE _m) {
		mode = _m;
	}
	public void setKeyName(String input){
		keyName = input;
	}
	public String getKeyName(){
		return keyName;
	}
	public MODE getMode() {
		return mode;
	}

	public byte[] getData() {
		return data.clone();
	}

	public PostParameters getParam() {
		return param;
	}
	
}