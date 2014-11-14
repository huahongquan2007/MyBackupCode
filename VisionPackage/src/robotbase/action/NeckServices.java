package robotbase.action;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import robotbase.action.DynamixelMotor;
import robotbase.action.NeckInstruction;

public class NeckServices extends Service 
{
	
//	UsbManager manager = null;
//	public  UsbSerialDriver serial_obj = null;
	BroadcastReceiver neck_receiver;
	DynamixelMotor pan_joint, tilt_joint;
	IntentFilter neck_filter = new IntentFilter();
	//--------------
	public BroadcastReceiver PosReceiver = null;
	public UsbManager usbManager;
	public UsbSerialDriver serialDriver;
	public UsbSerialPort serialPort;
	public UsbDevice serialDevice;
	UsbSerialProber prober;
	public SerialInputOutputManager mSerialIoManager;
    boolean S_Init_done = false;
    public Handler mHandler = new Handler();
	//------------------------//
    public final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    public static final String ACTION_USB_PERMISSION ="com.android.example.USB_PERMISSION";
	public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
	    	        
			String action = intent.getAction();
	    	        
			if (ACTION_USB_PERMISSION.equals(action)) 
			{
	    	      synchronized (this) 
	    	      {
	    	    	  UsbDevice device = (UsbDevice)intent.getParcelableExtra(usbManager.EXTRA_DEVICE);
	    	    	  if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
	    	    	  {
	    	              if(device != null)
	    	              {
	    	                      //call method to set up device communication
	    	                    if(S_Init_done==false)OpenserialPort();
	    	              }
	    	    	  } 
	    	    	  else
	    	    	  {
//	    	                    Log.d(TAG, "permission denied for device " + device);
	    	                
	    	    	  }
	    	      }
	    	 }
		  }
	 };
	private final SerialInputOutputManager.Listener mListener =
	            	new SerialInputOutputManager.Listener() {
	            		@Override
	            		public void onRunError(Exception e) {
	            				
	            		}
	            		@Override
	            		public void onNewData(final byte[] data) {
	            			NeckServices.this.updateReceivedData(data);
	            		}
	        };
	
	@Override
	public void onCreate() {
        super.onCreate();
        neck_filter.addAction(RobotIntent.NECK_SET_POSISTION);
        neck_filter.addAction(RobotIntent.NECK_SET_SPEED);
        neck_filter.addAction(RobotIntent.NECK_GET_SPEED);
        neck_filter.addAction(RobotIntent.NECK_GET_POSISTION);
        neck_filter.addAction(RobotIntent.NECK_GET_DATA);
        neck_filter.addAction(RobotIntent.NECK_SET_DATA);
    	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        
        
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.i("Neck", "usbManager: "+usbManager.toString());
        prober = UsbSerialProber.getDefaultProber();
        Log.i("Neck", "Propber: "+ prober.toString());
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(usbManager);
        Log.i("Neck", "List: "+availableDrivers.toString());
        if (!availableDrivers.isEmpty()) {
			   // check for existing devices
		    for (UsbDevice device :  usbManager.getDeviceList().values()) 
		    {
		        if((device.getVendorId() == 0x0403) && (device.getProductId() == 0x6001))
		        {
		        	serialDevice = device;
		        	serialDriver = prober.probeDevice(serialDevice);
		        	Log.i("Neck", "Serial Driver: "+serialDriver.toString());
//		        	PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

//		        	registerReceiver(mUsbReceiver, filter);
//					usbManager.requestPermission(device, pendingIntent);
		        	OpenserialPort();
					Log.i("Neck"," request permission");
//					return;
		        }
		     }
		}
        this.neck_receiver = new BroadcastReceiver() {
 	        @Override
 	        public void onReceive(Context context, Intent intent) 
 	            {
 	            	Bundle bundle = intent.getBundleExtra(NeckInstruction.NECK_PACKAGE); 
 	        		int id = (int)bundle.getInt(NeckInstruction.KEY_ID);
 	        		float value = (float) bundle.getFloat(NeckInstruction.KEY_VALUE);
 	        		int ins = (int)bundle.getInt(NeckInstruction.KEY_INSTRUCTION);
 	        		
 	        		if (ins == NeckInstruction.INST_SET_SPEED)
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
 	        				
 	        			
 	        		if (ins == NeckInstruction.INST_SET_POSITION)
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
 	        	
 	        		if (ins == NeckInstruction.INST_SET_DATA){
 	        			if (id == NeckInstruction.PAN_JOINT_ID)
							try {
								Log.i("Neck", "value = "+ value);
								pan_joint.dxl_read_byte_act((int)value);
								Log.i("Neck", "Neck on RX Mode");
         	        			Log.i("Neck", "PAN TX Data"+pan_joint.dxl_tx_data_str());
         	        			Log.i("Neck", "RX Data"+pan_joint.dxl_rx_data_str());
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						else if (id==NeckInstruction.TILT_JOINT_ID)
							try {
								tilt_joint.dxl_read_byte_act((int)value);
								Log.i("Neck", "Neck on RX Mode");
         	        			Log.i("Neck", "TILT TX Data"+tilt_joint.dxl_tx_data_str());
         	        			Log.i("Neck", "RX Data"+tilt_joint.dxl_rx_data_str());
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
 	        		}
 	        		Log.i("Neck", "Neck On receive");
 	            	Log.i("Neck", "PAN TX Data: "+pan_joint.dxl_tx_data_str());
 	            	Log.i("Neck", "TILT TX Data"+tilt_joint.dxl_tx_data_str());
 	        		if (id == 5) 
 	        		{
 	        			try {
 							tilt_joint.dxl_read_byte_act(tilt_joint.PRESENT_TEM);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 	        			Log.i("Neck", "Neck on RX Mode");
 	        			Log.i("Neck", "TILT TX Data"+tilt_joint.dxl_tx_data_str());
 	        			Log.i("Neck", "RX Data"+tilt_joint.dxl_rx_data_str());
 	        		}
 	            }
 	        };
 	    registerReceiver(this.neck_receiver, neck_filter);
        Log.i("Neck", "Neck Service On Create");
        
    }
	@Override
	public void onStart(Intent intent, int startId)
	{

		if (S_Init_done)
		{
			Toast.makeText(this, "Neck Serial  Ok ", Toast.LENGTH_LONG).show();
		}
		else
		{
			OpenserialPort();
		}
		Log.i("Neck", "Neck Service On Start");
//		if (this.serial_obj == null)
//		{
//			Log.i("Neck", "No USB2Dynamixel device connected!");
//			serial_obj = (UsbSerialDriver)UsbSerialProber.acquire(manager);
//			if (serial_obj != null)serialInit();
//		}
//		else
//			Log.i("Neck", serial_obj.toString());
		
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i("Neck", "Neck Service On destroy");
		unregisterReceiver(this.neck_receiver);
		stopSelf();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	private void OpenserialPort()
	{
		UsbDeviceConnection connection = usbManager.openDevice(serialDevice);
		if (connection != null) {
			// get first port and open it
			serialPort = serialDriver.getPorts().get(0);
			Log.i("Neck", serialPort.toString());
			try {				
				serialPort.open(connection);
				serialPort.setParameters(1000000, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
				Log.i("Neck", "Serial Port: " + serialPort.toString());
				Toast.makeText(this, "Neck Serial  Ok ", Toast.LENGTH_LONG).show();
				S_Init_done = true;
				this.tilt_joint = new DynamixelMotor(serialPort, new Joint("TILT_JOINT"));
		        this.tilt_joint.dxl_set_speed_act(2);
		        this.tilt_joint.dxl_write_byte_act(this.tilt_joint.TORQUE_ENABLE, 1);
		        this.tilt_joint.dxl_write_byte_act(this.tilt_joint.CW_SLOPE, 50);
		        this.tilt_joint.dxl_write_byte_act(this.tilt_joint.CCW_SLOPE, 50);
		        this.tilt_joint.dxl_write_byte_act(this.tilt_joint.PUNCH_L, 1023);
		        
		        // init Pan joint
		        this.pan_joint = new DynamixelMotor(serialPort, new Joint("PAN_JOINT"));
		        this.pan_joint.dxl_set_speed_act(10);
		        this.pan_joint.dxl_write_byte_act(this.pan_joint.TORQUE_ENABLE,1);
		        this.pan_joint.dxl_write_byte_act(this.pan_joint.CW_SLOPE, 50);
		        this.pan_joint.dxl_write_byte_act(this.pan_joint.CCW_SLOPE, 50);
		        this.pan_joint.dxl_write_byte_act(this.pan_joint.PUNCH_L, 1023);
		        
		        this.pan_joint.dxl_set_home_position();  
		        this.tilt_joint.dxl_set_home_position();      
				
//				if (setDTR) port.setDTR(true);
			}
			catch (IOException e) {
				S_Init_done = false;
			// deal with error
				Log.i("Neck", "Serial open fail");
			}
			onDeviceStateChange();
		}	
	}
	private void stopIoManager() {
		if (mSerialIoManager != null) {
		mSerialIoManager.stop();
		mSerialIoManager = null;
		}
	}
	/**
	* Observe serial connection
	*/
	private void startIoManager() {
		if (serialDriver != null) {
		mSerialIoManager = new SerialInputOutputManager(serialPort, mListener);
		mExecutor.submit(mSerialIoManager);
		}
	}
	/**
	* Restart the observation of the serial connection
	*/
	private void onDeviceStateChange() {
		stopIoManager();
		startIoManager();
	}
	private void updateReceivedData(byte[] data) {
    	
//    	final String message = "Read " + data.length + " bytes: \n"
//                + HexDump.dumpHexString(data) + "\n\n";
//    	Log.i("Neck", "read data " + message);  	  
	}
}
