package aios.core.vision.facerecognition.facepp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import aios.core.vision.facerecognition.FaceManager;
import aios.core.vision.utils.VisionConfig;

public class FaceppAPI {

    // Face++
    String FACE_API = VisionConfig.FACE_REG_FACEAPI;
    String FACE_KEY = VisionConfig.FACE_REG_FACEKEY;
    String GROUP_NAME = VisionConfig.FACE_REG_GROUP_NAME;
    private String NAME_SEPERATOR = VisionConfig.PERSON_NAME_SEPERATOR;
    private String NAME_PREFIX = VisionConfig.USER_PERSON_NAME_PREFIX;

    private Context ctx = null;
    HttpRequests request = null;
    FaceppAsyncResponse delegate;

    public FaceppAPI() {
        if (request == null) {
            request = new HttpRequests(FACE_API, FACE_KEY, false, false);
            if (request != null) {
                Log.i("FaceppAPI", "request = new HttpRequests SUCCESS");
            } else {
                Log.i("FaceppAPI", "request = new HttpRequests FAIL");
            }
        }
        //groupGetInfo(GROUP_NAME);
    }

    public FaceppAPI(FaceppAsyncResponse de) {
        this();
        delegate = de;
    }
    //----------------------------- PERSON -------------------------------------//
    public void personCreate(String personName) {
        PostParameters param = new PostParameters();
        param.addAttribute("person_name", getDatabaseName(personName));
        FaceParam curParam = new FaceParam(
                FaceParam.MODE.PERSON_CREATE, param);
        curParam.setPersonName(personName);
        new FaceppTask().execute(curParam);
    }
    public void personDelete(String personName) {
        PostParameters param = new PostParameters();
        param.addAttribute("person_name", getDatabaseName(personName));
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.PERSON_DELETE, param));
    }
    public void personAddFace(OnFaceppCompleted listener, String personName, byte[] data) {
        PostParameters param = new PostParameters();
        param.addAttribute("person_name", getDatabaseName(personName));
        FaceParam curParam = new FaceParam(
                FaceParam.MODE.PERSON_ADD_FACE, param);
        curParam.setData(data);
        curParam.setPersonName(personName);
        curParam.setListener(listener);

        new FaceppTask().execute(curParam);
    }
    public void personDeleteFace(String personName, String face_id) {
        PostParameters param = new PostParameters();
        param.addAttribute("person_name", getDatabaseName(personName));
        param.addAttribute("face_id", face_id);
        FaceParam curParam = new FaceParam(
                FaceParam.MODE.PERSON_DELETE_FACE, param);

        new FaceppTask().execute(curParam);
    }
    //----------------------------- GROUP -------------------------------------//
    public void groupCreate(String groupName) {
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.GROUP_CREATE, param));
    }

    public void groupDelete(String groupName) {
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.GROUP_DELETE, param));
    }

    public void groupGetInfo(String groupName) {
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.GROUP_GET_INFO, param));
    }

    public void groupAddPerson(String groupName, String personName) {
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        param.addAttribute("person_name", getDatabaseName(personName));
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.GROUP_ADD_PERSON, param));
    }
    //----------------------------- RECOGNITION -------------------------------------//
    public void recognitionIdentify(OnFaceppCompleted listener, String groupName, Bitmap img , String keyName){

        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        FaceParam curParam = new FaceParam(
                FaceParam.MODE.RECOGNITION_IDENTIFY, param);
        curParam.setData(FaceManager.getByteFromBitmap(img));
        curParam.setKeyName(keyName);
        curParam.setListener(listener);

        new FaceppTask().execute(curParam);
    }
    //----------------------------- TRAIN -------------------------------------//
    public void trainIdentify() {
        String groupName = GROUP_NAME;
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        new FaceppTask().execute(new FaceParam(
                FaceParam.MODE.TRAIN_IDENTIFY, param));
    }
    /* ---------------------UTILITIES--------------------- */
    public String getDatabaseName(String name) {
        Log.i("Log", "FReg: GetDatabaseName: " + NAME_PREFIX + NAME_SEPERATOR
                + name);
        return NAME_PREFIX + NAME_SEPERATOR + name;
    }

    public String getRealName(String name) {
        return name.replaceFirst(".+" + NAME_SEPERATOR + ".", "");
    }

    private class FaceppTask extends
            AsyncTask<FaceParam, Void, String> {

        private boolean isRecognition = false; 		// Need to modify for an array
        private String keyName = "";
        private String personName = "";
        @Override
        protected String doInBackground(FaceParam... params) {
            String result = "";
            for (FaceParam curParam : params) {
                JSONObject response = null;
                try {
                    result += curParam.getMode().toString();

                    switch (curParam.getMode()) {
                        case PERSON_CREATE:
                            try{
                                response = request.personCreate(curParam.getParam());
                            } catch(FaceppParseException e){
                                e.printStackTrace();
                            }
                            Log.i("FaceppAPI", "Add person to group");
                            String person_name = curParam.getPersonName();
                            groupAddPerson(GROUP_NAME, getDatabaseName(person_name) );

                            break;
                        case PERSON_DELETE:
                            response = request.personDelete(curParam.getParam());
                            break;
                        case PERSON_ADD_FACE:
                            PostParameters dtParam = curParam.getParam();
                            dtParam.setImg(curParam.getData());
                            JSONObject dtReq = request.detectionDetect(dtParam);

                            String face_id = dtReq.getJSONArray("face")
                                    .getJSONObject(0).getString("face_id");
                            Log.d("FaceppAPI", "ADD_FACE: "
                                            + face_id);

                            PostParameters afParam = curParam.getParam();
                            afParam.addAttribute("face_id", face_id);
                            response = request.personAddFace(afParam);

                            try {
                                curParam.getListener().personAddFace(curParam.getPersonName(), curParam.getData(), face_id);
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                            break;
                        case PERSON_DELETE_FACE:
                            response = request.personRemoveFace(curParam.getParam());
                            break;
                        case RECOGNITION_IDENTIFY:
                            // ---------------------------------- RECOGNITION --------------------
                            Log.i("FaceppAPI", "in case RECOGNITION_IDENTIFY");


                            // Send Image to Server
                            keyName = curParam.getKeyName();
                            PostParameters recogParam = curParam.getParam();
                            recogParam.setImg(curParam.getData());
                            response = request.recognitionIdentify(recogParam);
                            Log.i("FaceppAPI", "in case RECOGNITION_IDENTIFY after response recog identify " + response.toString());
                            isRecognition = true;

                            // Get Recognition Info from JSON
                            JSONArray faceArr = response.getJSONArray("face");
                            int faceLen = faceArr.length();

                            if (faceArr.length() == 0 || faceArr.getJSONObject(0).getJSONArray("candidate").length() == 0) {
                                Log.i("FaceppAPI", "NO CANDIDATE");
                                personName = "unknown " + "no candidate";
                            } else {

                                double confidence = 0;
                                int numOfLargeThan50 = 0;
                                personName = "";

                                // Get best candidate & confidence
                                HashMap<String, Vector<Double>> faceMap = new HashMap<String, Vector<Double>>();
                                for (int iFace = 0; iFace < faceLen; iFace++) {
                                    JSONArray candidateArr = faceArr.getJSONObject(iFace).getJSONArray("candidate");
                                    JSONObject bestCandidate = candidateArr.getJSONObject(0);
                                    String curName = bestCandidate.getString("person_name");
                                    double curConfidence = bestCandidate.getDouble("confidence");

                                    Vector<Double> v = null;
                                    if (faceMap.containsKey(curName)) {
                                        v = faceMap.get(curName);

                                    } else {
                                        v = new Vector<Double>();
                                    }
                                    if (curConfidence > 1)
                                        v.add(curConfidence);
                                    faceMap.put(curName, v);
                                }


                                for (Map.Entry<String, Vector<Double>> entry : faceMap.entrySet()) {
                                    String curName = entry.getKey();
                                    Vector<Double> curValue = entry.getValue();

                                    String temp = curName + " : ";
                                    double curAverage = 0;

                                    int numOver50 = 0;
                                    for (double val : curValue) {
                                        temp += " " + val;
                                        curAverage += val;
                                        if (val > VisionConfig.FACE_REG_THRESHOLD) {
                                            numOver50++;
                                        }
                                    }
                                    curAverage = curAverage / curValue.size();

                                    Log.e("FaceppAPI", "result for face recognition [" + faceLen + "]" + temp + " Average: " + curAverage);
                                    if (curAverage > confidence) {
                                        confidence = curAverage;
                                        personName = curName;
                                        numOfLargeThan50 = numOver50;
                                    }
                                }
                                // End: Get best candidate & confidence

                                // append unknown

                                String realName = getRealName(personName);
                                String realName_face_id = response.getJSONArray("face").getJSONObject(0).getString("face_id");
                                personName = realName;
                                if (confidence < VisionConfig.FACE_REG_THRESHOLD) {
//                                    personName = "unknown " + personName;
                                    if (numOfLargeThan50 > 2) {
                                        personName = personName + " " + VisionConfig.FACE_REG_RETRY_NAME;
                                    }
                                } else {
                                    //add this face to person
                                    try {
                                        curParam.getListener().personAddFace(realName, curParam.getData(), realName_face_id);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    //mFaceManager.addFace(realName, curParam.getData());
                                    // TO DO: Need to send to backup

                                    Log.i("FaceppAPI", "confidence " + confidence + ". Added to person " + realName + " face_id : " + realName_face_id);
                                }
                            }
                            Log.i("FaceppAPI", "Start FacePlusPlusTask RECOG_FACE: " + personName);
                            // ---------------------------------- RECOGNITION --------------------
                            break;
                        case GROUP_CREATE:
                            response = request.groupCreate(curParam.getParam());
                            break;
                        case GROUP_DELETE:
                            response = request.groupDelete(curParam.getParam());
                            break;
                        case GROUP_ADD_PERSON:
                            response = request.groupAddPerson(curParam.getParam());
                            break;
                        case GROUP_GET_INFO:
                            response = request.groupGetInfo(curParam.getParam());
                            int candidateLen = response.getJSONArray("person").length();
                            Log.d("FaceppAPI", "GROUP INFO LENGTH: " + candidateLen);
                            break;

                        case TRAIN_IDENTIFY:
                            response = request.trainIdentify(curParam.getParam());
                            break;
                        default:
                            break;
                    }

                    result += " ";
                    if (response != null) {
                        result += response.toString();
                    }
                }catch(Exception e){
                    Log.d("FaceppAPI", "ERROR[ " + result + " ] : " + e.getMessage() );
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if(isRecognition){
                if (delegate != null){


                    JSONObject json = new JSONObject();
                    try {
                        json.put("face_id", keyName);
                        json.put("face_name", personName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    delegate.processFinish(json.toString());
                } else{
                    Log.d("FaceppAPI", "delegate NULL ");
                }
            }
            Log.d("FaceppAPI", "onPostExecute: " + result);
            super.onPostExecute(result);
        }
    }
}
