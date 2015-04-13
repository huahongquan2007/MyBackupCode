package aios.core.vision.facerecognition.backup;

import android.os.AsyncTask;
import android.util.Log;

import com.facepp.http.PostParameters;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import aios.core.vision.facerecognition.OnFaceManagerTaskCompleted;
import aios.core.vision.facerecognition.facepp.FaceParam;
import aios.core.vision.utility.Utilities;
import aios.core.vision.utils.VisionConfig;

public class BackupAPI {

    private String NAME_SEPERATOR = VisionConfig.PERSON_NAME_SEPERATOR;
    private String NAME_PREFIX = VisionConfig.USER_PERSON_NAME_PREFIX;
    //----------------------------- PERSON -------------------------------------//
    public void personCreate(String personName) {
        BackupParam curParam = new BackupParam(
                BackupParam.MODE.PERSON_CREATE);
        curParam.setPersonName(personName);
        new BackupTask().execute(curParam);
    }
    public void personDelete(String personName) {
        BackupParam curParam = new BackupParam(
                BackupParam.MODE.PERSON_DELETE);
        curParam.setPersonName(personName);
        new BackupTask().execute(curParam);
    }
    public void personAddFace(String personName, byte[] data, String face_id) {
        BackupParam curParam = new BackupParam(
                BackupParam.MODE.PERSON_ADD_FACE);
        curParam.setPersonName(personName);
        curParam.setFaceID(face_id);
        curParam.setData(data);
        new BackupTask().execute(curParam);
    }
    public void personDeleteFace(String personName, String face_id) {
        BackupParam curParam = new BackupParam(
                BackupParam.MODE.PERSON_DELETE_FACE);
        curParam.setPersonName(personName);
        curParam.setFaceID(face_id);

        new BackupTask().execute(curParam);
    }

    public void personGetInfo(OnFaceManagerTaskCompleted listener, String personName){
        BackupParam curParam = new BackupParam(
                BackupParam.MODE.PERSON_GET_INFO);
        curParam.setPersonName(personName);
        curParam.setListener(listener);
        new BackupTask().execute(curParam);
    }

    /* --------------------- BACK UP ROBOTBASE ----------------------*/

    private class BackupTask extends
            AsyncTask<BackupParam, Void, String> {
        String API_URL = "http://robotbase.com/api";
        String API_CREATE ="/vision/person/create";
        String API_DELETE ="/vision/person/delete";
        String API_GET_INFO ="/vision/person/get_info";
        String API_FACE_ADD ="/vision/person/face/add";
        String API_FACE_REMOVE ="/vision/person/face/remove";
        @Override
        protected String doInBackground(BackupParam... params) {
            for (BackupParam curParam : params) {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                switch (curParam.getMode()) {
                    case PERSON_CREATE:
                        list.add(new BasicNameValuePair("person_name", curParam.getPersonName()));
                        Utilities.callAPI(API_URL + API_CREATE, list, "POST");
                        break;
                    case PERSON_DELETE:
                        list.add(new BasicNameValuePair("person_name", curParam.getPersonName()));
                        Utilities.callAPI(API_URL + API_DELETE, list, "POST");
                        break;
                    case PERSON_ADD_FACE:
                        list.add(new BasicNameValuePair("person_name", curParam.getPersonName()));
                        list.add(new BasicNameValuePair("data", curParam.getData()));
                        Utilities.callAPI(API_URL + API_FACE_ADD, list, "POST");
                        break;
                    case PERSON_DELETE_FACE:
                        list.add(new BasicNameValuePair("person_name", curParam.getPersonName()));
                        list.add(new BasicNameValuePair("face_id", curParam.getFaceID()));
                        Utilities.callAPI(API_URL + API_FACE_REMOVE, list, "POST");
                        break;
                    case PERSON_GET_INFO:
                        list.add(new BasicNameValuePair("person_name", curParam.getPersonName()));
                        String personInfo = Utilities.callAPI(API_URL + API_GET_INFO, list, "POST");

                        curParam.getListener().onTaskCompleted(personInfo);
                        break;
                    default:
                        break;
                }
            }
            return null;
        }
    }
}
