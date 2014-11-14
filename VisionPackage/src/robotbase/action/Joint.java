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
		  if (motor_type.equals("PAN_JOINT"))
		  {
			  
			  this.ID = NeckInstruction.PAN_JOINT_ID;
			  this.SIGN = 1;
			  this.HOME_POS = 180;
			  this.POS_RANGE = 200;
			  this.MIN_SPEED_LIMIT = 10;
			  this.MAX_SPEED_LIMIT = 40 ;
			  this.TYPE = "MX28";
		  }
		  if (motor_type.equals("TILT_JOINT"))
		  {
			  this.ID = NeckInstruction.TILT_JOINT_ID;
			  this.SIGN = -1;
			  this.HOME_POS = 180;
			  this.POS_RANGE = 30;
			  this.MIN_SPEED_LIMIT = 1;
			  this.MAX_SPEED_LIMIT = 2;
			  this.TYPE = "MX28";
		  }
		  
	  }
	  
}
