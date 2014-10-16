package robotbase.vision;

import robotbase.action.NeckIntent;
import android.content.Context;
import android.util.Log;

public class VisionHelperCenterOnTargets {
	public static float minX = 30, minY = 30;
	
	public static float curPan = 150, curTilt = 180;
	public static float STEP = 1;
	public static boolean run(Context context, float x , float y){
		// Input: Target Center coordinates x(-1000, 1000), y(-1000, 1000)
		// Output: True if mean of targets is in the center. Otherwise, False
		// Purpose: Calculate moments and send to DynamiteService
		
		
		int frameWidth = 320, frameHeight = 240; 
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
		if(eY < 0) curTilt -= STEP; else curTilt += STEP;
		
		Log.i("MyLog", "CenterOnTarget: eX: " + String.valueOf(eX) + " eY : " + String.valueOf(eY));
		context.sendBroadcast(new NeckIntent(NeckIntent.PAN_JOINT_ID, curPan).pos_intent);
		
		context.sendBroadcast(new NeckIntent(NeckIntent.TILT_JOINT_ID, curTilt).pos_intent);
		
		return false;
	}
}
