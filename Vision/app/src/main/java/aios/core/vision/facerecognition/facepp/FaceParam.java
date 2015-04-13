package aios.core.vision.facerecognition.facepp;

import com.facepp.http.PostParameters;

import aios.core.vision.facerecognition.OnFaceManagerTaskCompleted;

public class FaceParam {
    private OnFaceppCompleted listener;

    public void setListener(OnFaceppCompleted listener) {
        this.listener = listener;
    }

    public OnFaceppCompleted getListener() {
        return listener;
    }

    public static enum MODE{
        PERSON_CREATE, PERSON_DELETE,
        PERSON_ADD_FACE, PERSON_DELETE_FACE,

        PERSON_ADD_FACE_ID, PERSON_ADD_MANY_FACE, PERSON_GET_INFO ,
        GROUP_CREATE, GROUP_DELETE, GROUP_ADD_PERSON, GROUP_GET_INFO,
        RECOGNITION_IDENTIFY,
        TRAIN_IDENTIFY
    };
    private MODE mode;
    private PostParameters param = null;
    private byte[] data = null;
    private String keyName = "";
    private String personName = "";
    public FaceParam(MODE _m) {
        setMode(_m);
    }
    public FaceParam(MODE _m, PostParameters _p) {
        this(_m);
        setParam(_p);
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
    public void setPersonName(String n) {
        personName = n;
    }

    public String getPersonName() {
        return personName;
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
