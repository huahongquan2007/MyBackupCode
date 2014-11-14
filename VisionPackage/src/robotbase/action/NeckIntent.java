package robotbase.action;

import android.content.Intent;
import android.os.Bundle;

public class NeckIntent  {
	// KEY instruction
//	public Intent pos_intent = new Intent(RobotIntent.NECK_SET_POSISTION);
//	public Intent speed_intent = new Intent(RobotIntent.NECK_SET_SPEED);
	
	public Intent data;
//	public NeckIntent(int id, float value)
	public NeckIntent(int id, float value, String str)
	{
//		Bundle pos_bundle = new Bundle();
//		pos_bundle.putInt(NeckInstruction.KEY_ID, id );
//		pos_bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_POSITION);
//		pos_bundle.putFloat(NeckInstruction.KEY_VALUE, value);
//		this.pos_intent.putExtra(NeckInstruction.NECK_PACKAGE, pos_bundle);
		
//		Bundle speed_bundle = new Bundle();
//		speed_bundle.putInt(NeckInstruction.KEY_ID, id );
//		speed_bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_SPEED);
//		speed_bundle.putFloat(NeckInstruction.KEY_VALUE, value);
//		this.speed_intent.putExtra(NeckInstruction.NECK_PACKAGE, speed_bundle);
		
		if (str.equals(RobotIntent.NECK_SET_POSISTION))
		{
			Bundle bundle = new Bundle();
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_POSITION);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_POSISTION);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			
		}
		else if (str.equals(RobotIntent.NECK_SET_SPEED))
		{
			Bundle bundle = new Bundle();
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_SPEED);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_SPEED);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
		}
		else if(str.equals(RobotIntent.NECK_GET_POSISTION))
		{
			Bundle bundle = new Bundle();
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_GET_POSITION);
//			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_GET_POSISTION);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
		}
		else if (str.equals(RobotIntent.NECK_GET_SPEED))
		{
			Bundle bundle = new Bundle();
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_GET_SPEED);
//			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_GET_SPEED);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
		}
		else if (RobotIntent.NECK_SET_DATA.equals(str))
		{
			Bundle bundle = new Bundle();
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_DATA);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_DATA);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
		}
		
	}
}






















