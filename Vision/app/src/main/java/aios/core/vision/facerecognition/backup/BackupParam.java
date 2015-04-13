package aios.core.vision.facerecognition.backup;

import android.util.Base64;

import java.util.HashMap;

import aios.core.vision.facerecognition.OnFaceManagerTaskCompleted;

public class BackupParam {
    private OnFaceManagerTaskCompleted listener;

    public BackupParam(MODE m) {
		mode = m;
	}



    public static enum MODE{
		PERSON_CREATE, PERSON_DELETE, PERSON_ADD_FACE, PERSON_DELETE_FACE, PERSON_GET_INFO , PERSON_SET_INFO
		};
	private MODE mode;
    private String personName;
    private String face_id;
    private String data;

	public MODE getMode() {
		return mode;
	}

    public void setPersonName(String n){
        personName = n;
    }
    public String getPersonName(){
        return personName;
    }

    public void setData(byte[] d){
        data = Base64.encodeToString(d, Base64.DEFAULT);
    }
    public String getData(){
        return data;
    }
    public void setFaceID(String id){
        face_id = id;
    }
    public String getFaceID(){
        return face_id;
    }
    public void setListener(OnFaceManagerTaskCompleted listener) {
        this.listener = listener;
    }

    public OnFaceManagerTaskCompleted getListener() {
        return listener;
    }
}