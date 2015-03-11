package aios.core.vision.utility;

import java.util.List;
import java.util.Map;

public class UserShortcut {
	private String userID;
	private String email;
	private String glassAccessToken;
	private String glassRefreshToken;
	private String foursquareAccessToken;
	private String facebookAccessToken;

	private String twitterToken;
	private String twitterTokenSecret;
	
	private String coinbaseAccessToken;
	private String dwollaAccessToken;
	private String dwollaPIN;
	
	private String smartThingAccessToken;
	
	private String lockitronAccessToken;
	private String googleAccessToken;
	private Map<String, String> meetHue;
	
	private Map<String, String> fitbit;
	private Map<String, String> nest;
	private Map<String, String> dropcam;
	private Map<String, String> tesla;
	private Map<String, String> facebook;
	private Map<String, String> smartthings;
	private Map<String, String> lockitron;
	private Map<String, String> foursquare;
	private Map<String, String> twitter;
	private Map<String, String> dwolla;
	private Map<String, String> ordrin;
	private Map<String, String> coinbase;
	private Map<String, String> google;
	
	public Object tag;
	
	private double lng;
	private double lat;
	
	private List<Command> listCommand ;
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getAccessToken() {
		return glassAccessToken;
	}
	public void setAccessToken(String accessToken) {
		this.glassAccessToken = accessToken;
	}
	public String getRefreshToken() {
		return glassRefreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.glassRefreshToken = refreshToken;
	}
	public String getFoursquareAccessToken() {
		return foursquareAccessToken;
	}
	public void setFoursquareAccessToken(String foursquareAccessToken) {
		this.foursquareAccessToken = foursquareAccessToken;
	}
	
	public String getFacebookAccessToken() {
		return facebookAccessToken;
	}
	public void setFacebookAccessToken(String googleAccessToken) {
		this.googleAccessToken = googleAccessToken;
	}
	
	public String getGoogleAccessToken() {
		return googleAccessToken;
	}
	public void setGoogleAccessToken(String facebookAccessToken) {
		this.facebookAccessToken = facebookAccessToken;
	}
	
	public String getTwitterToken() {
		return twitterToken;
	}
	public void setTwitterToken(String twitterToken) {
		this.twitterToken = twitterToken;
	}
	public String getTwitterTokenSecret() {
		return twitterTokenSecret;
	}
	public void setTwitterTokenSecret(String twitterTokenSecret) {
		this.twitterTokenSecret = twitterTokenSecret;
	}
	
	
	public void setMeetHue(Map<String, String> meetHue){
		this.meetHue  = meetHue;
	}
	public Map<String, String> getMeetHue(){
		return this.meetHue;
	}
	
	public void setFitbit(Map<String, String> fitbit){
		this.fitbit  = fitbit;
	}
	
	public String getCoinbaseAccessToken() {
		return coinbaseAccessToken;
	}
	public void setCoinbaseAccessToken(String coinbaseAccessToken) {
		this.coinbaseAccessToken = coinbaseAccessToken;
	}
	
	
	public String getDwollaAccessToken() {
		return dwollaAccessToken;
	}
	public void setDwollaAccessToken(String dwollaAccessToken) {
		this.dwollaAccessToken = dwollaAccessToken;
	}
	public String getDwollaPIN() {
		return this.dwollaPIN;
	}
	public void setDwollaPIN(String dwollaPIN) {
		this.dwollaPIN = dwollaPIN;
	}
	
	public Map<String, String> getFitbit(){
		return this.fitbit;
	}
	/**
	 * @return the lockitronAccessToken
	 */
	public String getLockitronAccessToken() {
		return lockitronAccessToken;
	}
	/**
	 * @param lockitronAccessToken the lockitronAccessToken to set
	 */
	public void setLockitronAccessToken(String lockitronAccessToken) {
		this.lockitronAccessToken = lockitronAccessToken;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public List<Command> getListCommand() {
		return listCommand;
	}
	public void setListCommand(List<Command> listCommand) {
		this.listCommand = listCommand;
	}
	public String getSmartThingAccessToken() {
		return smartThingAccessToken;
	}
	public void setSmartThingAccessToken(String smartThingAccessToken) {
		this.smartThingAccessToken = smartThingAccessToken;
	}
	public Map<String, String> getNest() {
		return nest;
	}
	public void setNest(Map<String, String> nest) {
		this.nest = nest;
	}
	public Map<String, String> getDropcam() {
		return dropcam;
	}
	public void setDropcam(Map<String, String> dropcam) {
		this.dropcam = dropcam;
	}
	public Map<String, String> getTesla() {
		return tesla;
	}
	public void setTesla(Map<String, String> tesla) {
		this.tesla = tesla;
	}
	public Map<String, String> getFacebook() {
		return facebook;
	}
	public void setFacebook(Map<String, String> facebook) {
		this.facebook = facebook;
	}
	public Map<String, String> getSmartthings() {
		return smartthings;
	}
	public void setSmartthings(Map<String, String> smartthings) {
		this.smartthings = smartthings;
	}
	public Map<String, String> getLockitron() {
		return lockitron;
	}
	public void setLockitron(Map<String, String> lockitron) {
		this.lockitron = lockitron;
	}
	public Map<String, String> getCoinbase() {
		return coinbase;
	}
	public void setCoinbase(Map<String, String> coinbase) {
		this.coinbase = coinbase;
	}
	public Map<String, String> getTwitter() {
		return twitter;
	}
	public void setTwitter(Map<String, String> twitter) {
		this.twitter = twitter;
	}
	public Map<String, String> getFoursquare() {
		return foursquare;
	}
	public void setFoursquare(Map<String, String> foursquare) {
		this.foursquare = foursquare;
	}
	public Map<String, String> getDwolla() {
		return dwolla;
	}
	public void setDwolla(Map<String, String> dwolla) {
		this.dwolla = dwolla;
	}
	public Map<String, String> getOrdrin() {
		return ordrin;
	}
	public void setOrdrin(Map<String, String> ordrin) {
		this.ordrin = ordrin;
	}
	public Map<String, String> getGoogle() {
		return google;
	}
	public void setGoogle(Map<String, String> google) {
		this.google = google;
	}
	
}
