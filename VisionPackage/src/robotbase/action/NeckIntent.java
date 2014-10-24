package robotbase.action;

import android.content.Intent;
import android.os.Bundle;

public class NeckIntent  {
	// KEY instruction
	public Intent pos_intent = new Intent(RobotIntent.NECK_SET_POSISTION);
	public Intent speed_intent = new Intent(RobotIntent.NECK_SET_SPEED);
	public NeckIntent(int id, float value)
	{
		Bundle pos_bundle = new Bundle();
		pos_bundle.putInt(NeckInstruction.KEY_ID, id );
		pos_bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_POSITION);
		pos_bundle.putFloat(NeckInstruction.KEY_VALUE, value);
		this.pos_intent.putExtra(NeckInstruction.NECK_PACKAGE, pos_bundle);
		
		Bundle speed_bundle = new Bundle();
		speed_bundle.putInt(NeckInstruction.KEY_ID, id );
		speed_bundle.putInt(NeckInstruction.KEY_INSTRUCTION, NeckInstruction.INST_SPEED);
		speed_bundle.putFloat(NeckInstruction.KEY_VALUE, value);
		this.speed_intent.putExtra(NeckInstruction.NECK_PACKAGE, speed_bundle);
		
		
	}
}
