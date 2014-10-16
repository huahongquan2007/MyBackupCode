package robotbase.action;

import android.content.Intent;
import android.os.Bundle;

public class NeckIntent  {
	// KEY instruction
	public static String KEY_ID 				= "id";
	public static String KEY_INSTRUCTION 		= "instruction";
	public static String KEY_VALUE				= "value";
	public static String NECK_PACKAGE			= "NeckPackage";
	public static int	 INST_POSITION			= 132;
	public static int	 INST_SPEED				= 133;
	public static int	 INST_POS_OVER_TIME		= 134;
	
	public static int	 PAN_JOINT_ID			= 0;
	public static int	 TILT_JOINT_ID			= 1; 
	
	public static float    PAN_HOME_POS			= 100;
	public static float    PAN_MAX_SPEED		= 10;  
	public static float    TILT_HOME_POS		= 180;
	
	
	public static float    PAN_RANGE			= 140;
	public static float    TILT_RANGE		    = 30;
	public Intent pos_intent = new Intent(RobotIntent.NECK_SET_POSISTION);
	public Intent speed_intent = new Intent(RobotIntent.NECK_SET_SPEED);
	public NeckIntent(int id, float value)
	{
		Bundle pos_bundle = new Bundle();
		pos_bundle.putInt(NeckIntent.KEY_ID, id );
		pos_bundle.putInt(NeckIntent.KEY_INSTRUCTION, NeckIntent.INST_POSITION);
		pos_bundle.putFloat(NeckIntent.KEY_VALUE, value);
		this.pos_intent.putExtra(NeckIntent.NECK_PACKAGE, pos_bundle);
		
		Bundle speed_bundle = new Bundle();
		speed_bundle.putInt(NeckIntent.KEY_ID, id );
		speed_bundle.putInt(NeckIntent.KEY_INSTRUCTION, NeckIntent.INST_SPEED);
		speed_bundle.putFloat(NeckIntent.KEY_VALUE, value);
		this.speed_intent.putExtra(NeckIntent.NECK_PACKAGE, speed_bundle);
		
		
	}
}
