package robotbase.action;
 
public class  RobotIntent{
	
	public static String DISPLAY_SHOW_STATIC_CARD = "robotobase.display.SHOW_STATIC_CARD";
	public static String DISPLAY_SHOW_EMOTION = "robotbase.display.SHOW_EMOTION";
	
	public static String BASE_MOVE = "robotbase.base.MOVE"; 
	// For Neck
	public final static String NECK_SET_POSISTION = "robotbase.neck.SET_POSISTION";
	public final static String NECK_SET_SPEED = "robotbase.neck.SET_SPEED";
	
	
	public final static String NECK_GET_POSISTION = "robotbase.neck.GET_POSISTION";
	public final static String NECK_GET_SPEED = "robotbase.neck.GET_SPEED";
	public final static String NECK_SET_DATA = "robotbase.neck.SET_DATA";
	public final static String NECK_GET_DATA = "robotbase.neck.GET_DATA";
	// For Vision
	public static String CAM_FACE_DETECTION = "robotbase.cam.FACE_DETECTION";
	public static String CAM_TAKE_PICKTURE= "robotbase.cam.TAKE_PICTURE";
	public static String CAM_MOTION = "robotbase.cam.MOTION"; 
  
	//public static String SPEECH_RECOGNITION_TEXT  = "robotbase.speech.SpeechRecognition.speech";  
	//////
	public static String ABILITY_INSTALL  = "robotbase.abilities.install";
	
	public static String RESULT_DATA = "data";
	
	 
	public static String SPEECH_RECOGNITION_TEXT  = "robotbase.speech._TEXT";
	public static String SPEECH_RECOGNITION_NLP  = "robotbase.speech._NLP"; 
	public static String TEXT_TO_SPEECH  = "robotbase.speech.SPEAKER";
	
	public static String ZWAVE_DEVICE  = "robotbase_zwave_";
	// Vision Intent
	public static String SHARE_PHOTO = "robotbase.abilities.SHARE_PHOTO";
	public static String CAM_FACE_TRACKING = "robotbase.abilities.FACE_TRACKING";
	public static String CAM_FACE_RECOGNITION = "robotbase.abilities.FACE_RECOGNITION";
	public static String CAM_FACIAL_LEARNING = "robotbase.abilities.FACIAL_LEARNING";
	
	public static String DB_PULLER = "robotbase.database.DB_PULLER";
}