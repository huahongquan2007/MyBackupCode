package robotbase.action;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import android.hardware.usb.UsbManager;
import robotbase.action.DynamixelMotor;

public class NeckServices extends Service {
	
	UsbManager manager;
	public static UsbSerialDriver serial_obj;
	DynamixelMotor pan_joint, tilt_joint;
	BroadcastReceiver yourReceiver;
	boolean i;
	@Override
	  public void onCreate() {
        super.onCreate();
        i=true;
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(RobotIntent.NECK_SET_POSISTION);
        theFilter.addAction(RobotIntent.NECK_SET_SPEED);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.i("Neck", manager.toString());
        serial_obj = (UsbSerialDriver)UsbSerialProber.acquire(manager);
        Log.i("Neck", serial_obj.toString());
        try {
        	serial_obj.open();
			//serial_obj.setBaudRate(1000000);
			serial_obj.setParameters(1000000, UsbSerialDriver.DATABITS_8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
			Log.i("Neck", "Serial Port Opened");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.tilt_joint = new DynamixelMotor(serial_obj, "MX28", NeckIntent.TILT_JOINT_ID, NeckIntent.TILT_HOME_POS, NeckIntent.TILT_RANGE );
        this.pan_joint = new DynamixelMotor(serial_obj, "AX12", NeckIntent.PAN_JOINT_ID,NeckIntent.PAN_HOME_POS , NeckIntent.PAN_RANGE);
        this.pan_joint.dxl_set_home_position();
        this.tilt_joint.dxl_set_home_position();
        this.yourReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Do whatever you need it to do when it receives the broadcast
                // Example show a Toast message...
            	Bundle bundle = intent.getBundleExtra(NeckIntent.NECK_PACKAGE);
        		int ID = (int)bundle.getInt(NeckIntent.KEY_ID);
        		float Value = (float) bundle.getFloat(NeckIntent.KEY_VALUE);
        		int ins = (int)bundle.getInt(NeckIntent.KEY_INSTRUCTION);
        		
        		if (ins == NeckIntent.INST_SPEED)
        			if (ID == NeckIntent.PAN_JOINT_ID)
        				pan_joint.dxl_set_speed_act(Value);
        			else if (ID==NeckIntent.TILT_JOINT_ID)
        				tilt_joint.dxl_set_speed_act(Value);
        			else if (ID == DynamixelMotor.BROADCAST_ID)
        				{
        					pan_joint.dxl_set_speed(Value);
        					tilt_joint.dxl_set_speed( Value);
        					pan_joint.dxl_trigger();
        				}
        				
        			
        		if (ins == NeckIntent.INST_POSITION)
        			if (ID == NeckIntent.PAN_JOINT_ID)
        				pan_joint.dxl_set_position_act(Value);
        			else if (ID==NeckIntent.TILT_JOINT_ID)
        				tilt_joint.dxl_set_position_act(Value);
        			else if (ID == DynamixelMotor.BROADCAST_ID)
        			{
        				pan_joint.dxl_set_position(Value);
        				tilt_joint.dxl_set_position(Value);
        				pan_joint.dxl_trigger();
        			}
        		Log.i("Neck", "Neck On receive");
            	Log.i("Neck", pan_joint.dxl_tx_data_str());
        		if (ID == 5)
        		{
        			pan_joint.dxl_read_word_act(30);
        			Log.i("Neck", pan_joint.dxl_rx_data_str());
        		}
            	
            	
            }
        };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.yourReceiver, theFilter);
        Log.i("Neck", "Service On Create");

    }
	@Override
	public void onStart(Intent intent, int startId)
	{
		pan_joint.dxl_set_speed_act(10);
		Log.i("Neck", "Service On Start");
		//serial_obj = (UsbSerialDriver)UsbSerialProber.acquire(manager);
        //Log.i("Neck", String.format("%s", serial_obj));
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(this.yourReceiver);
		Log.i("Neck", "Service On destroy");
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
