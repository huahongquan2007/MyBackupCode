package aios.core.vision.facerecognition.facepp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import aios.core.vision.utility.Utilities;
import aios.core.vision.utils.VisionConfig;


public class FaceppAPI {
	private String NAME_SEPERATOR = VisionConfig.PERSON_NAME_SEPERATOR;
	private String NAME_PREFIX = VisionConfig.USER_PERSON_NAME_PREFIX;
	// Face++
    HttpRequests request = null;
    FaceppAsyncResponse delegate;
    
    private boolean hasPerson = true;
    public boolean hasCandidate(){
    	return hasPerson;
    }
    public FaceppAPI(){
    	if(request == null){
			request = new HttpRequests(VisionConfig.FACE_REG_FACEAPI, VisionConfig.FACE_REG_FACEKEY, false, false);
			if(request != null){
				Log.i("Log", "FReg: request = new HttpRequests SUCCESS");
			}else{
				Log.i("Log", "FReg: request = new HttpRequests FAIL");
			}
		}
    	groupGetInfo(VisionConfig.FACE_REG_GROUP_NAME);
    }
    public FaceppAPI(FaceppAsyncResponse de){
    	this();
    	setDelegate(de);
    	Log.e("Log3", "FaceppAPI");
    }
    public void setDelegate(FaceppAsyncResponse de){
    	delegate = de;
    	Log.e("Log3", "FaceppAPI setDelegate");
    }
    public HttpRequests getRequest(){
    	return request;
    }

    //----------------------------- PERSON -------------------------------------//
	public void personCreate(String personName) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_CREATE, param));
		
		BackUpParam backupParam = new BackUpParam(BackUpParam.MODE.PERSON_CREATE);
		backupParam.setData("person_name", getDatabaseName(personName));
		new BackUpTask().execute(backupParam);
	}

	public void personDelete(String personName) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_DELETE, param));
	}

	public void personAddFace(String personName, byte[] data) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_ADD_FACE, param, data));
		
		if(VisionConfig.FACE_REG_BACKUP){
			BackUpParam backupParam = new BackUpParam(BackUpParam.MODE.PERSON_ADD_FACE);
			
			backupParam.setData("person_name", getDatabaseName(personName));
			backupParam.setData("data", Base64.encodeToString(data, Base64.DEFAULT));
			
			new BackUpTask().execute(backupParam);			
		}

	}
	public void personAddFace(String personName, String face_id, byte[] data) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		param.addAttribute("face_id", face_id);
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_ADD_FACE_ID, param));
		
		if(VisionConfig.FACE_REG_BACKUP){
			BackUpParam backupParam = new BackUpParam(BackUpParam.MODE.PERSON_ADD_FACE);
			
			backupParam.setData("person_name", getDatabaseName(personName));
			backupParam.setData("data", Base64.encodeToString(data, Base64.DEFAULT));
			
			new BackUpTask().execute(backupParam);			
		}
	}
	public void personGetInfo(String personName) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_GET_INFO, param));
	}
	
	
	//----------------------------- RECOGNITION -------------------------------------//
	public void recognitionIdentify(String groupName, Bitmap data , String keyName){
        PostParameters param = new PostParameters();
        param.addAttribute("group_name", groupName);
        new FacePlusPlusTask().execute(new FaceppParam(
                FaceppParam.MODE.RECOGNITION_IDENTIFY, param, getByteFromBitmap(data), keyName));
	}
	
    //----------------------------- GROUP -------------------------------------//
	public void groupCreate(String groupName) {
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.GROUP_CREATE, param));
	}

	public void groupDelete(String groupName) {
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.GROUP_DELETE, param));
	}
	public void groupGetInfo(String groupName) {
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.GROUP_GET_INFO, param));
	}
	public void groupAddPerson(String groupName, String personName) {
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.GROUP_ADD_PERSON, param));
	}
    //----------------------------- TRAIN -------------------------------------//
	public void trainIdentify(String groupName) {
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.TRAIN_IDENTIFY, param));
	}	
	private class FacePlusPlusTask extends
			AsyncTask<FaceppParam, Void, String> {
		private String keyName = ""; 		// Need to modify for an array
		private String personName = ""; 		// Need to modify for an array
		private boolean isRecognition = false; 		// Need to modify for an array
//		public FaceppAsyncResponse delegate = null;
		
		@Override
		protected String doInBackground(FaceppParam... facepp) {
			String result = "";
			for (FaceppParam curParam : facepp) {
				JSONObject response = null;
				try {
					Log.i("Log", "FReg: ___ Start Task: ___ ");
					switch (curParam.getMode()) {
					case PERSON_CREATE:
						response = request.personCreate(curParam.getParam());
						break;
					case PERSON_DELETE:
						response = request.personDelete(curParam.getParam());
						break;
					case PERSON_ADD_FACE:
						PostParameters dtParam = curParam.getParam();
						dtParam.setImg(curParam.getData());
						JSONObject dtReq = request.detectionDetect(dtParam);

						String face_id = "";
						try {
							face_id = dtReq.getJSONArray("face")
									.getJSONObject(0).getString("face_id");
							Log.i("Log",
									"FReg: Start FacePlusPlusTask ADD_FACE: "
											+ face_id);
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("Log",
									"FReg: Start FacePlusPlusTask ADD_FACE ERROR: "
											+ e.toString());
						}

						PostParameters afParam = curParam.getParam();
						afParam.addAttribute("face_id", face_id);
						response = request.personAddFace(afParam);
						break;
					case PERSON_ADD_FACE_ID:
						request.personAddFace(curParam.getParam());
						delegate.updateLastPersonAddTime();

						break;
					case PERSON_ADD_MANY_FACE:
						PostParameters dtManyParam = curParam.getParam();
						dtManyParam.setImg(curParam.getData());
						JSONObject dtManyReq = request.detectionDetect(dtManyParam);
						
						try {
							JSONArray dtManyfaceArr = dtManyReq.getJSONArray("face");
							int dtManyfaceArrLen = dtManyfaceArr.length();
							Log.i("Log",
									"FReg: Start FacePlusPlusTask PERSON_ADD_MANY_FACE: LENGTH_" + dtManyfaceArrLen + " _ "
											+ dtManyReq.toString());
							String cur_face_id = "";
							for(int iManyFace = 0 ; iManyFace < dtManyfaceArrLen; iManyFace++){
								cur_face_id += dtManyReq.getJSONArray("face").getJSONObject(iManyFace).getString("face_id");
								if(iManyFace != dtManyfaceArrLen - 1)
									cur_face_id += ",";
					
							}
							PostParameters afManyParam = curParam.getParam();
							afManyParam.addAttribute("face_id", cur_face_id);
							response = request.personAddFace(afManyParam);
							Log.i("Log","FReg: Start FacePlusPlusTask PERSON_ADD_MANY_FACE: [" + cur_face_id +"]" + response.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("Log",
									"FReg: Start FacePlusPlusTask PERSON_ADD_MANY_FACE ERROR: "
											+ e.toString());
						}
						
						
						break;
					case PERSON_GET_INFO:
						response = request.personGetInfo(curParam.getParam());
						int len = 0;
						try {
							len = response.getJSONArray("face").length();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Log.i("Log",
									"FReg: Start FacePlusPlusTask PERSON INFO LENGTH: " + len);
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
						int candidateLen = 0;
						try {
							candidateLen = response.getJSONArray("person").length();
							if(candidateLen > 0){
								hasPerson = true;
							}else{
								hasPerson = false;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Log.i("Log", "FReg: Start FacePlusPlusTask GROUP INFO LENGTH: " + candidateLen);
						break;
					case RECOGNITION_IDENTIFY:
						Log.i("Log", "FReg : in case RECOGNITION_IDENTIFY");

						keyName = curParam.getKeyName();
						PostParameters recogParam = curParam.getParam();
						recogParam.setImg(curParam.getData());
						response = request.recognitionIdentify(recogParam);
						Log.i("Log", "FReg : in case RECOGNITION_IDENTIFY after response recog identify " + response.toString());
						isRecognition = true;
						try {
							JSONArray faceArr = response.getJSONArray("face");
							int faceLen = faceArr.length();
							
							if(faceArr.length() == 0 || faceArr.getJSONObject(0).getJSONArray("candidate").length() == 0){
								Log.i("Log", "FReg: NO CANDIDATE");
								personName = "unknown " + "no candidate";
							}else{

								double confidence = 0; 
								int numOfLargeThan50 = 0;
								personName = ""; 

								// Get best candidate & confidence
								HashMap<String, Vector<Double>> faceMap = new HashMap<String, Vector<Double>>();
								for(int iFace = 0 ; iFace < faceLen ; iFace++ ){
									JSONArray candidateArr = faceArr.getJSONObject(iFace).getJSONArray("candidate");
									JSONObject bestCandidate = candidateArr.getJSONObject(0); 
									String curName = bestCandidate.getString("person_name");
									double curConfidence = bestCandidate.getDouble("confidence");
									
									Vector<Double> v = null;
									if(faceMap.containsKey(curName)){
										v = faceMap.get(curName);
									
									}else{
										v = new Vector<Double>();
									}
									if(curConfidence > 1)
										v.add(curConfidence);
									faceMap.put(curName, v);
								}
								
								
								
								for(Map.Entry<String, Vector<Double>> entry : faceMap.entrySet()){
									String curName = entry.getKey();
									Vector<Double> curValue = entry.getValue();
									
									String temp = curName + " : ";
									double curAverage = 0;
									
									int numOver50 = 0;
									for(double val : curValue){
										temp += " " + val;
										curAverage += val;
										if(val > VisionConfig.FACE_REG_THRESHOLD)
										{
											numOver50++;
										}
									}
									curAverage = curAverage / curValue.size();
									
									Log.e("Log", "FReg : result for face recognition [" + faceLen + "]" + temp + " Average: " + curAverage);
									if(curAverage > confidence){
										confidence = curAverage;
										personName = curName;
										numOfLargeThan50 = numOver50;
									}
								}
								// End: Get best candidate & confidence
								
								// append unknown

								String realName = getRealName(personName);
//								String realName = personName;
								String realName_face_id = response.getJSONArray("face").getJSONObject(0).getString("face_id");
//								personName = "unknown " + realName +  "_" + confidence;
//								personName = realName +  "_" + confidence;
								personName = realName;
								if(confidence < VisionConfig.FACE_REG_THRESHOLD){
									personName = "unknown " + personName;
									if(numOfLargeThan50 > 2){
										personName = personName + " " + VisionConfig.FACE_REG_RETRY_NAME;
									}
								}else{
									//add this face to person
									personAddFace(realName, realName_face_id, curParam.getData());
									
									Log.i("Log", "FReg: confidence " + confidence + ". Added to person " + realName + " face_id : " + realName_face_id);
								}
							}
							Log.i("Log", "FReg: Start FacePlusPlusTask RECOG_FACE: "	+ personName);
						} catch (JSONException e) {
							personName = "NO_FACE";
							e.printStackTrace();
							Log.i("Log", "FReg: Start FacePlusPlusTask RECOG_FACE ERROR: " + e.toString());
						}
						break;
					case TRAIN_IDENTIFY:
						response = request.trainIdentify(curParam.getParam());
						
						if(delegate != null)
							delegate.trainFinish();
						
						break;
					default:
						break;
					}
				} catch (FaceppParseException e) {
					Log.i("Log",
							"FReg: FacePlusPlusTask Exception " + e.toString());
				}
				if (response != null) {
					result += curParam.getMode().toString();
					result += response.toString();
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			
			if(isRecognition){
				
				if(delegate != null){
                    try {
                        JSONObject object = new JSONObject();
                        object.put("face_id", keyName);
                        object.put("face_name", personName);
                        delegate.processFinish( object.toString() );
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
				else
					Log.e("Log", "FReg: Delegate NULL. Please Initialize First!");
				Log.i("Log", "FReg: ___ End Task: (KEY: "+ keyName +")(VALUE: "+ personName +")___ " + result);
			}else{
				Log.i("Log", "FReg: ___ End Task: ___ " + result);
			}
			super.onPostExecute(result);
			
		}

	}
	/* --------------------- BACK UP ROBOTBASE ----------------------*/

	private class BackUpTask extends
	AsyncTask<BackUpParam, Void, String> {
		String API_URL = "http://192.168.1.127:5000/api";
		String API_CREATE ="/vision/person/create";
		String API_DELETE ="/vision/person/delete";
		String API_SET_INFO ="/vision/person/set_info";
		String API_FACE_ADD ="/vision/person/face/add";
		String API_FACE_REMOVE ="/vision/person/face/remove";
		@Override
		protected String doInBackground(BackUpParam... params) {		
			for (BackUpParam curParam : params) {		
				Log.i("Log", "FReg: ___ Start Task: ___ ");
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				switch (curParam.getMode()) {
				case PERSON_CREATE:
					list.add(new BasicNameValuePair("person_name", curParam.getData("person_name")));
					Utilities.callAPI(API_URL + API_CREATE, list, "POST");
					break;
				case PERSON_DELETE:					
					list.add(new BasicNameValuePair("person_name", curParam.getData("person_name")));
					Utilities.callAPI(API_URL + API_DELETE, list, "POST");			
					break;
				case PERSON_ADD_FACE:
					list.add(new BasicNameValuePair("person_name", curParam.getData("person_name")));
					list.add(new BasicNameValuePair("data", curParam.getData("data")));
					Utilities.callAPI(API_URL + API_FACE_ADD, list, "POST");			
					break;
				default:
					break;
				}
			}
			return null;
		}	
	}
	/* ---------------------UTILITIES--------------------- */
	public String getDatabaseName(String name) {
//		Log.i("Log", "FReg: GetDatabaseName: " + NAME_PREFIX + NAME_SEPERATOR
//				+ name);
//		return NAME_PREFIX + NAME_SEPERATOR + name;
		return name;
	}

	public String getRealName(String name) {
		return name;
//		return name.replaceFirst(".+" + NAME_SEPERATOR + ".", "");
	}
	public byte[] getByteFromBitmap(Bitmap bmp){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
		Log.i("Log", "FReg: getByteFromBitmap ");
		return out.toByteArray();
	}
}