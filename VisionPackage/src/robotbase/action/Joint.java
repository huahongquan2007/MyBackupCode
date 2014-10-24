package robotbase.action;


public class Joint {
	  int   	ID;
	  float     SIGN;
	  float 	HOME_POS;
	  float 	POS_RANGE;
	  float 	MIN_SPEED_LIMIT;
	  float 	MAX_SPEED_LIMIT;
	  String 	TYPE;
	  // Constructor
	  public Joint(String motor_type)
	  {
		  if (motor_type.equals("AX12"))
		  {
			  
			  this.ID = NeckInstruction.PAN_JOINT_ID;
			  this.SIGN = 1;
			  this.HOME_POS = 100;
			  this.POS_RANGE = 200;
			  this.MIN_SPEED_LIMIT = 1;
			  this.MAX_SPEED_LIMIT = 10;
			  this.TYPE = "AX12";
		  }
		  if (motor_type.equals("MX28"))
		  {
			  this.ID = NeckInstruction.TILT_JOINT_ID;
			  this.SIGN = 1;
			  this.HOME_POS = 180;
			  this.POS_RANGE = 30;
			  this.MIN_SPEED_LIMIT = 1;
			  this.MAX_SPEED_LIMIT = 2;
			  this.TYPE = "MX28";
		  }
		  
	  }
	  
}
