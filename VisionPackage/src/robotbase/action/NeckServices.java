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
	BroadcastReceiver neck_receiver;
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
        this.tilt_joint = new DynamixelMotor(serial_obj, new Joint("MX28"));
        this.tilt_joint.dxl_set_speed_act(2);
      
        
        this.pan_joint = new DynamixelMotor(serial_obj, new Joint("AX12"));
        this.pan_joint.dxl_set_speed_act(10);
        
        this.pan_joint.dxl_set_home_position();
        this.tilt_joint.dxl_set_home_position();
        
        this.neck_receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
            	Bundle bundle = intent.getBundleExtra(NeckInstruction.NECK_PACKAGE);
        		int id = (int)bundle.getInt(NeckInstruction.KEY_ID);
        		float value = (float) bundle.getFloat(NeckInstruction.KEY_VALUE);
        		int ins = (int)bundle.getInt(NeckInstruction.KEY_INSTRUCTION);
        		
        		if (ins == NeckInstruction.INST_SPEED)
        			if (id == NeckInstruction.PAN_JOINT_ID)
        				pan_joint.dxl_set_speed_act(value);
        			else if (id==NeckInstruction.TILT_JOINT_ID)
        				tilt_joint.dxl_set_speed_act(value);
        			else if (id == DynamixelMotor.BROADCAST_ID)
        				{
        					pan_joint.dxl_set_speed(value);
        					tilt_joint.dxl_set_speed( value);
        					pan_joint.dxl_trigger();
        				}
        				
        			
        		if (ins == NeckInstruction.INST_POSITION)
        			if (id == NeckInstruction.PAN_JOINT_ID)
        				pan_joint.dxl_set_position_act(value);
        			else if (id==NeckInstruction.TILT_JOINT_ID)
        				tilt_joint.dxl_set_position_act(value);
        			else if (id == DynamixelMotor.BROADCAST_ID)
        			{
        				pan_joint.dxl_set_position(value);
        				tilt_joint.dxl_set_position(value);
        				pan_joint.dxl_trigger();
        			}
        		Log.i("Neck", "Neck On receive");
            	Log.i("Neck", pan_joint.dxl_tx_data_str());
            	Log.i("Neck", tilt_joint.dxl_tx_data_str());
        		if (id == 5)
        		{
        			pan_joint.dxl_read_word_act(30);
        			Log.i("Neck", pan_joint.dxl_rx_data_str());
        		}
            	
            	
            }
        };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.neck_receiver, theFilter);
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
		unregisterReceiver(this.neck_receiver);
		Log.i("Neck", "Service On destroy");
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
