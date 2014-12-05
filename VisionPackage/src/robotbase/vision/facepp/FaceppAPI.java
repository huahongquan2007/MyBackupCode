package robotbase.vision.facepp;

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

import robotbase.utility.Utilities;
import robotbase.vision.VisionConfig;
import robotbase.vision.VisionUtilities;
import robotbase.vision.facepp.FaceppParam.MODE;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;


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
				Log.i("MyLog", "FReg: request = new HttpRequests SUCCESS");
			}else{
				Log.i("MyLog", "FReg: request = new HttpRequests FAIL");
			}
		}
//    	personDelete("quan");
    	groupGetInfo(VisionConfig.FACE_REG_GROUP_NAME);
    }
    public FaceppAPI(FaceppAsyncResponse de){
    	this();
    	setDelegate(de);
    	Log.e("MyLog3", "FaceppAPI");
    }
    public void setDelegate(FaceppAsyncResponse de){
    	delegate = de;
    	Log.e("MyLog3", "FaceppAPI setDelegate");
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
	public void personAddManyFace(String personName, Vector<Bitmap> vec) {
		Bitmap sendBitmap = VisionUtilities.combineImages(vec, VisionConfig.FACE_REG_IDENTIFY_WIDTH, VisionConfig.FACE_REG_IDENTIFY_HEIGHT, (int)VisionConfig.FACIAL_LEARN_NUM_IMG_WIDTH, (int)VisionConfig.FACIAL_LEARN_NUM_IMG_HEIGHT);
				
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_ADD_MANY_FACE, param, getByteFromBitmap(sendBitmap)));
		
		if(VisionConfig.FACE_REG_BACKUP){
			for(Bitmap img : vec){
				BackUpParam backupParam = new BackUpParam(BackUpParam.MODE.PERSON_ADD_FACE);
				
				backupParam.setData("person_name", getDatabaseName(personName));
				backupParam.setData("data", Base64.encodeToString(getByteFromBitmap(img), Base64.DEFAULT));
				
				new BackUpTask().execute(backupParam);
			}			
		}

		
	}
	public void personGetInfo(String personName) {
		PostParameters param = new PostParameters();
		param.addAttribute("person_name", getDatabaseName(personName));
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.PERSON_GET_INFO, param));
	}
	
	//----------------------------- RECOGNITION -------------------------------------//
	public void recognitionIdentify(String groupName, byte[]data , String keyName){
		
		Log.i("MyLog", "FReg: recognitionIdentify: " + data.length);
		
		PostParameters param = new PostParameters();
		param.addAttribute("group_name", groupName);
//		param.setMode("oneface");
		new FacePlusPlusTask().execute(new FaceppParam(
				FaceppParam.MODE.RECOGNITION_IDENTIFY, param, data, keyName));
		
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
					Log.i("MyLog", "FReg: ___ Start Task: ___ ");
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
							Log.i("MyLog",
									"FReg: Start FacePlusPlusTask ADD_FACE: "
											+ face_id);
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("MyLog",
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
							Log.i("MyLog",
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
							Log.i("MyLog","FReg: Start FacePlusPlusTask PERSON_ADD_MANY_FACE: [" + cur_face_id +"]" + response.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("MyLog",
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
						Log.i("MyLog",
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
						Log.i("MyLog", "FReg: Start FacePlusPlusTask GROUP INFO LENGTH: " + candidateLen);
						break;
					case RECOGNITION_IDENTIFY:
						Log.i("MyLog", "FReg : in case RECOGNITION_IDENTIFY");

						keyName = curParam.getKeyName();
						PostParameters recogParam = curParam.getParam();
						recogParam.setImg(curParam.getData());
						response = request.recognitionIdentify(recogParam);
						isRecognition = true;
						try {
							JSONArray faceArr = response.getJSONArray("face");
							int faceLen = faceArr.length();
							
							if(faceArr.getJSONObject(0).getJSONArray("candidate").length() == 0){
								Log.i("MyLog", "FReg: NO CANDIDATE");
								personName = "unknown " + "no candidate";
							}else{

								double confidence = 0; 
								
								personName = ""; 

								// Get best candidate & confidence
								Log.i("MyLog", "FReg: before HashMap : Length : " + faceLen);
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
									for(double val : curValue){
										temp += " " + val;
										curAverage += val;
									}
									curAverage = curAverage / curValue.size();
									
									Log.e("MyLog", "FReg : result for face recognition: " + temp + " Average: " + curAverage);
									if(curAverage > confidence){
										confidence = curAverage;
										personName = curName;
									}
								}
								// End: Get best candidate & confidence
								
								
								
								
								
								
								
								// append unknown

								String realName = getRealName(personName);
								String realName_face_id = response.getJSONArray("face").getJSONObject(0).getString("face_id");
//								personName = "unknown " + realName +  "_" + confidence;
								personName = realName +  "_" + confidence;
								if(confidence < 50){
									personName = "unknown " + personName + confidence;
								}else if (confidence > 50){
									//add this face to person
									personAddFace(realName, realName_face_id, curParam.getData());
									
									Log.i("MyLog", "FReg: confidence " + confidence + ". Added to person " + realName + " face_id : " + realName_face_id);
								}
							}
							Log.i("MyLog", "FReg: Start FacePlusPlusTask RECOG_FACE: "	+ personName);
						} catch (JSONException e) {
							personName = "NO_FACE";
							e.printStackTrace();
							Log.i("MyLog", "FReg: Start FacePlusPlusTask RECOG_FACE ERROR: " + e.toString());
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
					Log.i("MyLog",
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
				
				if(delegate != null)
					delegate.processFinish(keyName, personName);
				else
					Log.e("MyLog", "FReg: Delegate NULL. Please Initialize First!");
				Log.i("MyLog", "FReg: ___ End Task: (KEY: "+ keyName +")(VALUE: "+ personName +")___ " + result);
			}else{
				Log.i("MyLog", "FReg: ___ End Task: ___ " + result);
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
				Log.i("MyLog", "FReg: ___ Start Task: ___ ");
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
		Log.i("MyLog", "FReg: GetDatabaseName: " + NAME_PREFIX + NAME_SEPERATOR
				+ name);
		return NAME_PREFIX + NAME_SEPERATOR + name;
	}

	public String getRealName(String name) {
		return name.replaceFirst(".+" + NAME_SEPERATOR + ".", "");
	}
	public byte[] getByteFromBitmap(Bitmap bmp){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 20, out);
				
		return out.toByteArray();
	}
}