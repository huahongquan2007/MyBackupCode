package robotbase.vision;

import robotbase.action.NeckInstruction;
import robotbase.action.NeckIntent;
import robotbase.action.RobotIntent;
import robotbase.action.kobuki.KobukiCommand;
import robotbase.action.kobuki.KobukiConstanst;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class VisionHelperCenterOnTargets {
	public static float minX = 30, minY = 30;
	public static float curPan = 0, curTilt = 0;
	public static float STEP = 1f;
	public static boolean run(Context context, float x , float y){
		// Input: Target Center coordinates x(-1000, 1000), y(-1000, 1000)
		// Output: True if mean of targets is in the center. Otherwise, False
		// Purpose: Calculate moments and send to DynamiteService
		
		int frameWidth = VisionConfig.getWidth(), frameHeight = VisionConfig.getHeight(); 
		int curX = frameWidth / 2;
		int curY = frameHeight / 2;
		float eX = 0, eY = 0;
		eX = curX - x;
		eY = curY - y;
		
		if(Math.abs(eX) < minX && Math.abs(eY) < minY) return true;
		
		if(eX < 0){
			Log.i("MyLog", "CenterOnTarget: eX: " + String.valueOf(eX) + " MOVE : LEFT, CurPan " + String.valueOf(curPan));
			curPan -= STEP;
		}
		else{
			Log.i("MyLog", "CenterOnTarget: eX: " + String.valueOf(eX) + " MOVE : RIGHT, CurPan " + String.valueOf(curPan));
			curPan += STEP;
		}
		if(eY > 0) curTilt += STEP; else curTilt -= STEP;
		
		Log.i("MyLog", "CenterOnTarget: eX: " + String.valueOf(eX) + " eY : " + String.valueOf(eY));
		context.sendBroadcast(new NeckIntent(NeckInstruction.PAN_JOINT_ID, curPan, RobotIntent.NECK_SET_POSISTION).data);
		
		if(curTilt > NeckInstruction.TILT_MIN_LIMIT && curTilt < NeckInstruction.TILT_MAX_LIMIT){
			context.sendBroadcast(new NeckIntent(NeckInstruction.TILT_JOINT_ID, curTilt, RobotIntent.NECK_SET_POSISTION).data);
		}else{
			// move back
			int data_control = KobukiCommand.BACKWARD.ordinal();
			Bundle data = new Bundle();
			data.putInt(KobukiConstanst.COMMAND_KEY, data_control);
			Intent intent = new Intent(KobukiConstanst.COMMAND_MOVE);
			intent.putExtra(KobukiConstanst.BUNDLE_KEY, data);
			context.sendBroadcast(intent);
		}

		return false;
	}
}
