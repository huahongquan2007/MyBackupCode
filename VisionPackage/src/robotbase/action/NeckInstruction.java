package robotbase.action;

public final class NeckInstruction {
	public final static String KEY_ID 				= "id";
	public final static String KEY_INSTRUCTION 		= "instruction";
	public final static String KEY_VALUE				= "value";
	public final static String NECK_PACKAGE			= "NeckPackage";
	public final static int	 INST_SET_POSITION			= 132;
	public final static int	 INST_GET_POSITION			= 133;
	public final static int	 INST_GET_SPEED				= 134;
	public final static int	 INST_SET_SPEED				= 135;
	public final static int	 INST_SET_DATA				= 136;
	public final static int	 INST_GET_DATA				= 137;
	
	
	public final static int	 INST_POS_OVER_TIME		= 1361;
	public final static int	 PAN_JOINT_ID			= 0;
	public final static int	 TILT_JOINT_ID			= 1;
	public final static float  PAN_MAX_LIMIT			= 100;
	public final static float  PAN_MIN_LIMIT			= -100;
	
	public final static float TILT_MAX_LIMIT			= 15;
	public final static float TILT_MIN_LIMIT			= -15;

}
 