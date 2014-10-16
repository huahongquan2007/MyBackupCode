package robotbase.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;





import android.util.DisplayMetrics;
import android.util.Log;

public class Utilities {

	

	
	public static int dpToPx(int dp, DisplayMetrics displayMetrics) {
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	public static int pxToDp(int px, DisplayMetrics displayMetrics) {
	    int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	    return dp;
	}

	public static String capitalizeString(String inputString) {
		return String.valueOf(inputString.charAt(0)).toUpperCase()
				+ inputString.substring(1, inputString.length());
	}

	public static JSONObject stringToJSON(String s) throws JSONException {
		return (new JSONObject(s));
	}

	public static String streamToString(InputStream is) throws IOException {
		String str = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			str = sb.toString();
		}

		return str;
	}

	public static String callAPI(String urlString, String method) {
		try {
			// //LOG.info(urlString);

			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			urlConnection.setRequestMethod(method);
			urlConnection.setDoInput(true);
			// urlConnection.setDoOutput(true);

			urlConnection.connect();

			String res = Utilities.streamToString(urlConnection
					.getInputStream());

			return res;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static int callApi(String urlString, String method, String payloadData){
		 
	        URL url;
			try {
				url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod(method);
		        connection.setDoOutput(true);
		        connection.setRequestProperty("Content-Type", "application/json");
		        connection.setRequestProperty("Accept", "application/json");
		        connection.setInstanceFollowRedirects(true); 
		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36");
		        
		        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
		        osw.write(String.format( payloadData ));
		        osw.flush();
		        osw.close();
		        System.err.println(connection.getResponseCode());  
		        
		        int code= connection.getResponseCode();
		        		if(code == 307){
		        			String newUrl = connection.getHeaderField("Location");
		        			return callApi(newUrl,"PUT",payloadData);
		        		}
		        
		        return code;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
	        
	        
	}
	
	public static String callAPI(String urlString, List<NameValuePair> params,
			String method) {
		String res = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(urlString);

		try {
			if (params.size() > 0) {
				httpPost.setEntity(new UrlEncodedFormEntity(params));
			}

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = Utilities.streamToString(entity.getContent());
			}

		} catch (ClientProtocolException e) {
			Log.d("exception", e.getMessage());
		} catch (IOException e) {
			Log.d("exception", e.getMessage());
		}

		return res;
	}

	/**
	 * load user info
	 * 
	 * @param userId
	 * @return
	 */
	public static UserShortcut loadUser(String userId) {

		String url = "https://morning-garden-5976.herokuapp.com/api/v1/users/"
				+ userId + "/command";

		// url = "https://morning-garden-5976.herokuapp.com/api/v1/users/" +
		// userId;

		String userSource = Utilities.callAPI(url, "GET");

		return Utilities.loadUserFromSource(userId, userSource);

	}

	public static UserShortcut loadUserFromSource(String userId,
			String userSource) {

		UserShortcut user = new UserShortcut();
		user.setUserID(userId);

		JSONTokener jstk = new JSONTokener(userSource);

		JSONObject jsonObj = null;

		try {
			jsonObj = (JSONObject) jstk.nextValue();
		} catch (JSONException e) {
			Log.d("debug ", e.getMessage());
		}

		if (jsonObj != null) {

			Log.d("debug ", "Getting Google auth info");
			try {
				JSONObject google = jsonObj.getJSONObject("google");
				// Email
				user.setEmail(google.getString("email"));

				user.setAccessToken(google.getString("token"));
				user.setRefreshToken(google.getString("refresh_token"));
			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Foursquare auth info");

			try {
				JSONObject foursquare = jsonObj.getJSONObject("foursquare");

				user.setFoursquareAccessToken(foursquare.getString("token"));

			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Facebook auth info");

			try {
				JSONObject facebook = jsonObj.getJSONObject("facebook");
				user.setFacebookAccessToken(facebook.getString("token"));

				Map<String, String> facebook_hm = new HashMap<String, String>();

				facebook_hm.put("id", facebook.getString("id"));
				facebook_hm.put("token", facebook.getString("token"));

				user.setFacebook(facebook_hm);

			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Coinbase auth info");

			try {
				JSONObject coinbase = jsonObj.getJSONObject("coinbase");

				user.setCoinbaseAccessToken(coinbase.getString("token"));
				Map<String, String> coinbase_hm = new HashMap<String, String>();
				coinbase_hm.put("id", coinbase.getString("id"));
				coinbase_hm.put("token", coinbase.getString("token"));

			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Dwolla auth info");

			try {
				JSONObject dwollaObj = jsonObj.getJSONObject("dwolla");
				Map<String, String> dwolla = new HashMap<String, String>();

				user.setDwollaAccessToken(dwollaObj.getString("token"));
				user.setDwollaPIN(dwollaObj.getString("refresh_token"));

				dwolla.put("id", dwollaObj.getString("id"));
				dwolla.put("token", dwollaObj.getString("token"));
				dwolla.put("pin", dwollaObj.getString("refresh_token"));
				user.setDwolla(dwolla);

			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Twitter auth info");

			try {

				JSONObject twitter = jsonObj.getJSONObject("twitter");
				user.setTwitterToken(twitter.getString("token"));
				user.setTwitterTokenSecret(twitter.getString("secret"));
			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting Fitbit auth info");

			try {
				JSONObject fitbitojb = jsonObj.getJSONObject("fitbit");
				Map<String, String> fitbit = new HashMap<String, String>();
				fitbit.put("id", fitbitojb.getString("id"));
				fitbit.put("user_id", fitbitojb.getString("user_id"));
				fitbit.put("token", fitbitojb.getString("token"));
				fitbit.put("tokenSecret", fitbitojb.getString("secret"));
				user.setFitbit(fitbit);

			} catch (JSONException e) {
				Log.d("debug ", e.getMessage());
			}

			Log.d("debug ", "Getting lockiron  auth info");

			try {

				JSONObject lockitron = jsonObj.getJSONObject("lockitron");
				user.setLockitronAccessToken(lockitron.getString("token"));

			} catch (JSONException e) {
				Log.d("debug", "Error API: " + e.getMessage());
			}

			Log.d("debug ", "Getting SmartThing auth info");
			try {

				JSONObject smartthings = jsonObj.getJSONObject("smartthings");
				user.setSmartThingAccessToken(smartthings.getString("token"));

				Map<String, String> smartthingsObject = new HashMap<String, String>();
				smartthingsObject.put("id", smartthings.getString("id"));
				smartthingsObject.put("token", smartthings.getString("token"));
				user.setNest(smartthingsObject);

			} catch (JSONException e) {
				Log.d("debug", "Error API SmartThing: " + e.getMessage());
			}
			Log.d("debug ", "Getting Nest auth info");
			try {

				JSONObject nesttojb = jsonObj.getJSONObject("nest");
				Map<String, String> nest = new HashMap<String, String>();
				nest.put("id", nesttojb.getString("id"));
				nest.put("token", nesttojb.getString("token"));
				user.setNest(nest);

			} catch (JSONException e) {
				Log.d("debug", "Error API Nest: " + e.getMessage());
			}
			Log.d("debug ", "Getting MeetHue auth info");
			try {
				JSONObject meethueObj = jsonObj.getJSONObject("meethue");
				Map<String, String> meetHue = new HashMap<String, String>();

				meetHue.put("id", meethueObj.getString("id"));
				meetHue.put("token", meethueObj.getString("token"));
				// meetHue.put("data", meethueObj.getString("data"));

				user.setMeetHue(meetHue);
			} catch (JSONException e) {
				Log.d("debug", "Error API MeetHue: " + e.getMessage());
			}
			Log.d("debug ", "Getting dropcam auth info");
			try {
				JSONObject meethueObj = jsonObj.getJSONObject("dropcam");
				Map<String, String> meetHue = new HashMap<String, String>();

				meetHue.put("id", meethueObj.getString("id"));
				meetHue.put("token", meethueObj.getString("token"));
				// meetHue.put("data", meethueObj.getString("data"));

				user.setDropcam(meetHue);
			} catch (JSONException e) {
				Log.d("debug", "Error API dropcam: " + e.getMessage());
			}
			Log.d("debug ", "Getting tesla auth info");
			try {
				JSONObject teslaObj = jsonObj.getJSONObject("tesla");
				Map<String, String> tesla = new HashMap<String, String>();

				tesla.put("id", teslaObj.getString("id"));
				tesla.put("token", teslaObj.getString("token"));

				user.setTesla(tesla);
			} catch (JSONException e) {
				Log.d("debug", "Error API tesla: " + e.getMessage());
			}

			Log.d("debug ", "Getting foursquare auth info");
			try {
				JSONObject foursquareObj = jsonObj.getJSONObject("foursquare");
				Map<String, String> foursquare = new HashMap<String, String>();

				foursquare.put("id", foursquareObj.getString("id"));
				foursquare.put("token", foursquareObj.getString("token"));

				user.setFoursquare(foursquare);
			} catch (JSONException e) {
				Log.d("debug", "Error API foursquare: " + e.getMessage());
			}

			Log.d("debug ", "Getting twitter auth info");
			try {
				JSONObject twitterObj = jsonObj.getJSONObject("twitter");
				Map<String, String> twitter = new HashMap<String, String>();

				twitter.put("id", twitterObj.getString("id"));
				twitter.put("token", twitterObj.getString("token"));

				user.setTwitter(twitter);
			} catch (JSONException e) {
				Log.d("debug", "Error API twitter: " + e.getMessage());
			}

			Log.d("debug ", "Getting lockitron auth info");
			try {
				JSONObject lockitronObj = jsonObj.getJSONObject("lockitron");
				Map<String, String> lockitron = new HashMap<String, String>();

				lockitron.put("id", lockitronObj.getString("id"));
				lockitron.put("token", lockitronObj.getString("token"));

				user.setLockitron(lockitron);
			} catch (JSONException e) {
				Log.d("debug", "Error API lockitron: " + e.getMessage());
			}

			Log.d("debug ", "Getting coinbase auth info");
			try {
				JSONObject coinbaseObj = jsonObj.getJSONObject("coinbase");
				Map<String, String> coinbase = new HashMap<String, String>();

				coinbase.put("id", coinbaseObj.getString("id"));
				coinbase.put("token", coinbaseObj.getString("token"));

				user.setCoinbase(coinbase);
			} catch (JSONException e) {
				Log.d("debug", "Error API coinbase: " + e.getMessage());
			}

			Log.d("debug ", "Getting ordrin auth info");
			try {
				JSONObject ordrinObj = jsonObj.getJSONObject("ordrin");
				Map<String, String> ordrin = new HashMap<String, String>();

				ordrin.put("id", ordrinObj.getString("id"));
				ordrin.put("token", ordrinObj.getString("token"));

				user.setOrdrin(ordrin);
			} catch (JSONException e) {
				Log.d("debug", "Error API ordrin: " + e.getMessage());
			}

			Log.d("debug ", "Getting google auth info");
			try {
				JSONObject googleObj = jsonObj.getJSONObject("google");
				Map<String, String> google = new HashMap<String, String>();

				google.put("id", googleObj.getString("id"));
				google.put("token", googleObj.getString("token"));
				google.put("refresh_token",
						googleObj.getString("refresh_token"));
				google.put("expires_at", googleObj.getString("expires_at"));

				user.setGoogle(google);
			} catch (JSONException e) {
				Log.d("debug", "Error API ordrin: " + e.getMessage());
			}

			Log.d("debug ", "Getting smartthings auth info");
			try {
				JSONObject smartthingsObj = jsonObj
						.getJSONObject("smartthings");
				Map<String, String> smartthings = new HashMap<String, String>();

				smartthings.put("id", smartthingsObj.getString("id"));
				smartthings.put("token", smartthingsObj.getString("token"));
				smartthings.put("refresh_token",
						smartthingsObj.getString("refresh_token"));
				smartthings.put("expires_at",
						smartthingsObj.getString("expires_at"));

				user.setSmartthings(smartthings);
			} catch (JSONException e) {
				Log.d("debug", "Error API ordrin: " + e.getMessage());
			}

		}

		// List commands
		user.setListCommand(Utilities.readListCommand(userSource));
		user.tag = userSource;
		Log.d("source", "done source");
		return user;
	}

	public static String timeMilisToString(long milis) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(milis);

		return sd.format(calendar.getTime());
	}

	public static String getDateTime(String date_s, String time_s,
			String formatDateTime) {

		Date date = new Date();
		String year = new SimpleDateFormat("yyyy").format(date);
		String day = new SimpleDateFormat("dd").format(date);
		String month = new SimpleDateFormat("MMMM").format(date);

		String hours = new SimpleDateFormat("HH").format(date);
		String min = new SimpleDateFormat("mm").format(date);
		String a = new SimpleDateFormat("a").format(date);

		// Lay thang tu input:
		Pattern p = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(date_s);
		while (m.find()) {
			day = m.group();
			break;
		}
		// Lay thang tu chuoi:
		String lstMonth[] = date_s.split(day);
		if (lstMonth.length >= 1) {
			month = lstMonth[0].trim();
		}

		// Gio phut giay tu chuoi
		// Time start
		String lstTime[] = time_s.split(":");
		if (lstTime.length >= 2) {
			m = p.matcher(lstTime[0]);
			while (m.find()) {
				hours = m.group();
				break;
			}
			m = p.matcher(lstTime[1]);
			while (m.find()) {
				min = m.group();
				break;
			}
		} else {
			p = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
			m = p.matcher(time_s);
			while (m.find()) {
				hours = m.group();
				break;
			}
			min = "0";
		}

		boolean flag_a = false;
		p = Pattern.compile("a.m", Pattern.CASE_INSENSITIVE);
		m = p.matcher(time_s);
		while (m.find()) {
			a = "AM";
			flag_a = true;
			break;
		}
		p = Pattern.compile("am", Pattern.CASE_INSENSITIVE);
		m = p.matcher(time_s);
		while (m.find()) {
			a = "AM";
			flag_a = true;
			break;
		}
		p = Pattern.compile("p.m", Pattern.CASE_INSENSITIVE);
		m = p.matcher(time_s);
		while (m.find()) {
			a = "PM";
			flag_a = true;
			break;
		}
		p = Pattern.compile("pm", Pattern.CASE_INSENSITIVE);
		m = p.matcher(time_s);
		while (m.find()) {
			a = "PM";
			flag_a = true;
			break;
		}

		if (!flag_a) {
			if (Integer.parseInt(hours) >= 8 && Integer.parseInt(hours) <= 11) {
				a = "AM";
			}
		}
		String strStartTime = month + " " + day + ", " + year + " " + hours
				+ ":" + min + " " + a;

		if (formatDateTime == null
				|| (formatDateTime != null && formatDateTime == "")) {
			formatDateTime = "yyyy/MM/dd h:m a";
		}
		try {
			date = new SimpleDateFormat("MMMM d, yyyy h:m a")
					.parse(strStartTime);
		} catch (ParseException e) {
			date = new Date();
		}
		strStartTime = new SimpleDateFormat(formatDateTime).format(date);

		return strStartTime;
	}

	public static float parseFloat(String str) {
		float money = 0;
		Pattern p = Pattern.compile("(\\d+(.\\d+)?)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		Boolean f = false;
		while (m.find()) {
			money = Float.parseFloat(m.group());
			f = true;
			break;
		}
		if (!f)
			try {
				return Float.parseFloat(String.valueOf(ConvertWordToNumber
						.parse(str.toLowerCase())));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		return money;
	}

	public static List<Command> readListCommand(String source) {

		List<Command> commandList = new ArrayList<Command>();

		// read from source..
		try {
			JSONObject object = new JSONObject(source);

			JSONArray cmArray = object.getJSONArray("commands");
			for (int i = 0; i < cmArray.length(); i++) {

				Command cmd = new Command();
				try {
					JSONObject job = cmArray.getJSONObject(i);

					cmd.setId(job.getString("id"));
					cmd.setText(job.getString("text"));
					cmd.setEditable(Boolean.parseBoolean(job
							.getString("editable")));
					cmd.setDefault_text(job.getString("default_text"));

					// parrams
					try {
						JSONArray params = job.getJSONArray("params");
						cmd.setParams(params);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// children command
					List<String> cmchilds = new ArrayList<String>();
					try {
						JSONArray childs = job.getJSONArray("childrens");

						for (int j = 0; j < childs.length(); j++) {
							cmchilds.add(childs.getJSONObject(j).getString(
									"command_id"));
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					cmd.setSubcommands(cmchilds);
					// done..
					commandList.add(cmd);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return commandList;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return commandList;

	}

	/**
	 * abc at gmail.com -> abc@gmail.com ex
	 * 
	 * @param string
	 * @return email
	 */
	public static String parseEmail(String str) {
		// Replace "at" -> "@' neu co
		str = str.trim().replaceAll(" at ", "@");
		str = str.replaceAll("\\s+", "");
		Pattern p = Pattern.compile(
				"\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		while (m.find()) {
			return m.group();
		}
		return "";
	}

	/**
	 * Xem them trong OrderFoodAction ["Error 1", "Error 2", "Error 3"] ->
	 * "Error 1, error 2, error 3"
	 * 
	 * @param JSONArray
	 * @return string
	 */
	public static String getErrorStringFromArrayJson(JSONArray jsArrayErrors) {
		String strErrors = "";
		for (int i = 0; i < jsArrayErrors.length(); i++) {
			String err;
			try {
				err = jsArrayErrors.getString(i);
			} catch (JSONException e) {
				err = "";
			}
			if (i > 0 && err != "") {
				err = ", " + err.toLowerCase();
			}
			strErrors += err;
		}
		return strErrors;
	}

	/**
	 * Xem them trong OrderFoodAction "order food pizza to word" ex:
	 * getValueOfKeyword("order food ", "order food pizza to word")->pizza
	 * 
	 * @param string
	 *            keyStart, string keyEnd, string command
	 * @return string
	 */
	public static String getValueOfKeyword(String keyStart, String keyEnd,
			String command) {
		command = command.trim().toLowerCase();
		String outPut = "";

		int indexStart = command.indexOf(keyStart);
		int indexEnd = command.indexOf(keyEnd);
		try {
			if (indexStart != -1 && indexEnd != -1) {
				outPut = command.substring(indexStart + keyStart.length(),
						indexEnd).trim();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return outPut;
	}

	/**
	 * Xem them trong OrderFoodAction "order food pizza to word" ex:
	 * getValueOfKeyword(" to ", "order food pizza to word")->word
	 * 
	 * @param string
	 *            keyStart, string keyEnd, string command
	 * @return string
	 */
	public static String getValueOfKeyword(String keyStart, String command) {
		command = command.trim().toLowerCase();
		String outPut = "";
		int indexStart = command.indexOf(keyStart);
		try {
			if (indexStart != -1) {
				outPut = command.substring(indexStart + keyStart.length())
						.trim();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return outPut;
	}

	/**
	 * 123123123.31 -> 123,123,123.31 ex
	 * 
	 * @param number
	 * @return
	 */
	public static String formatNumber(String number) {

		if (number == "") {
			return "";
		}

		DecimalFormat myFormatter = new DecimalFormat("###,###.##");
		String output = myFormatter.format(Double.parseDouble(number));
		System.out.println(output);
		return output;
	}


	public static String cap1stChar(String userIdea) {
		char[] stringArray = userIdea.toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		return userIdea = new String(stringArray);
	}

	/*
	 * POST : https://morning-garden-5976.herokuapp.com/api/v1/log/tracking ----
	 * user_id (user_id login) status ( status ) message ( message reponse ket
	 * qua ) action_name (voicecommand) device_name ( glass, android, ios )
	 * user_id = params[:user_id].blank? ? "" : params[:user_id]
	 * 
	 * command = params[:command].blank? ? "" : params[:command] status_command
	 * = params[:status].blank? ? "" : params[:status] message =
	 * params[:message].blank? ? "" : params[:message] action_name =
	 * params[:action_name].blank? ? "" : params[:action_name] device_name =
	 * params[:device_name].blank? ? "" : params[:device_name]
	 */
	public static boolean logserver(String user_id, String useremail, String status, String command,
			String message, String action_name, String step) {

		// List<NameValuePair> params = new ArrayList<NameValuePair>();

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
		nameValuePairs.add(new BasicNameValuePair("email", useremail));
		nameValuePairs.add(new BasicNameValuePair("status", status));
		nameValuePairs.add(new BasicNameValuePair("message", message));
		nameValuePairs.add(new BasicNameValuePair("command", command));
		nameValuePairs.add(new BasicNameValuePair("action_name", action_name));
		nameValuePairs.add(new BasicNameValuePair("step", step));
		nameValuePairs.add(new BasicNameValuePair("device_name", "ANDROID"));

		String urlString = "https://morning-garden-5976.herokuapp.com/api/v1/log/tracking";
		 
		Utilities.callAPI(urlString, nameValuePairs, "POST");

		return true;
	}
	
	public static String arrayToString(ArrayList<String> list, String split) {

		String listString = "";

		for (String s : list) {
			listString += s + split;
		}
		return listString;

	}

	// validating email id
	public static boolean isValidEmail(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	// find string in string 
	
	 public static boolean findMe(String subString, String mainString) {
	        boolean foundme = false;
	        
	        int max = mainString.length() - subString.length();
	 
	        // Java's Default "contains()" Method
	        System.out.println(mainString.contains(subString) ? "mainString.contains(subString) Check Passed.."
	                : "mainString.contains(subString) Check Failed..");
	 
	        // Implement your own Contains Method with Recursion
	        checkrecusion: for (int i = 0; i <= max; i++) {
	            int n = subString.length();
	 
	            int j = i;
	            int k = 0;
	 
	            while (n-- != 0) {
	                if (mainString.charAt(j++) != subString.charAt(k++)) {
	                    continue checkrecusion;
	                }
	            }
	            foundme = true;
	            break checkrecusion;
	        }
	        System.out
	        .println(foundme ? "\nImplement your own Contains() Method - Result: Yes, Match Found.."
	                : "\nImplement your own Contains() Method - Result:  Nope - No Match Found..");
	        
	        return foundme;
	    }
	 
}
