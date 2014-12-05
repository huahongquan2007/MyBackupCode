package robotbase.action;

import android.content.Intent;
import android.os.Bundle;

public class NeckIntent  {
	// KEY instruction
//	public Intent pos_intent = new Intent(RobotIntent.NECK_SET_POSISTION);
//	public Intent speed_intent = new Intent(RobotIntent.NECK_SET_SPEED);
	
	public Intent data;
	Bundle bundle = new Bundle();
	public NeckIntent(int id, float value, String str)
	{
		switch (str)
		{
		case RobotIntent.NECK_SET_POSISTION:
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_POSITION);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_POSISTION);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			break;
		case RobotIntent.NECK_SET_SPEED:
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_SPEED);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_SPEED);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			break;
		case RobotIntent.NECK_SET_DATA:
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SET_DATA);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_SET_DATA);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			break;
		case RobotIntent.NECK_GET_POSISTION:
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_GET_POSITION);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_GET_POSISTION);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			break;
		case RobotIntent.NECK_GET_SPEED:
			bundle.putInt(NeckInstruction.KEY_ID, id );
			bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_GET_SPEED);
			bundle.putFloat(NeckInstruction.KEY_VALUE, value);
			this.data = new Intent(RobotIntent.NECK_GET_SPEED);
			this.data.putExtra(NeckInstruction.NECK_PACKAGE, bundle);
			break;
		}
		
	}
}






















