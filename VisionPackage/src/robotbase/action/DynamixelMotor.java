package robotbase.action;

import java.io.IOException;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
public class DynamixelMotor  {
	
	/// User definition
		// Serial Port 
		// Offset
		public   UsbSerialDriver dxl_serial;
		// Speed
		public   int   MAX_SPEED_RESGISTER; //1023
		public   float   MIN_SPEED; // 1 rpm
		public   float   MAX_SPEED; // 114 rpm
		public   float MIN_SPEED_LIMIT;
		public	 float MAX_SPEED_LIMIT;
		public   float SPEED_RESOLUTION	; // 1023:113
		public 	 int   DEFAULT_SPEED = 1 ;
		// Position
		// default
		public   int   MAX_POS_RESGISTER; // 1023
		public 	 float   MIN_POS; // 0
		public 	 float   MAX_POS; // 300
		public	 float   MIN_POS_LIMIT;
		public   float   MAX_POS_LIMIT;
		public   float POS_RESOLUTION; // 1023/300
		public   float   HOME_POS = 150;
		public   float   CURRENT_POS;
		// Torque
		public   int   MAX_TORQUE_LIMIT_RESGISTER; // 1023
		public	 float   MAX_TORQUE; // 100%
		public   float   MIN_TORQUE; // 0%
		public	 float TORQUE_RESOLUTION;
		public   float   DEFAULT_TORQUE = 100;
		// Motor type
		public   int   DXL_ID				= 0;
		public   String DXL_TYPE			= "AX12";
	// Control Table  definition////
		int 	   DXL_BAUDRATE				= 1000000;
		final  int MODEL_NUM_L				= 1;
		final  int MODEL_NUM_H				= 2;
		final  int MOTOR_ID					= 3;
		final  int BAUDRATE					= 4;
		final  int RETURN_DELAY_TIME		= 5;
		final  int CW_ANGLE_LIMIT_L			= 6;
		final  int CW_ANGLE_LIMIT_H			= 7;
		final  int CCW_ANGLE_LIMIT_L		= 8;
		final  int CCW_ANGLE_LIMIT_H		= 9;
		final  int HIGHEST_LIMIT_TEM		= 11;
		final  int LOWEST_LIMIT_VOLTAGE		= 12;
		final  int HIGHEST_LIMIT_VOLTAGE	= 13;
		final  int MAX_TORQUE_L				= 14;
		final  int MAX_TORQUE_H				= 15;
		final  int STATUS_RETURN_LEVEL		= 16;
		final  int ALARM_LED				= 17;
		final  int ALARM_SHUTDOWN			= 18;	
		final  int TORQUE_ENABLE			= 24;
		final  int CW_MARGIN				= 26;
		final  int CCW_MARGIN				= 27;
		final  int CW_SLOPE					= 28;
		final  int CCW_SLOPE				= 29;
		final  int GOAL_POS_L				= 30;
		final  int GOAL_POS_H				= 31;
		final  int SPEED_L					= 32;
		final  int SPEED_H					= 33;
		final  int TORQUE_LIMIT_L			= 34;
		final  int TORQUE_LIMIT_H			= 35;
		final  int PRESENT_POS_L			= 36; 
		final  int PRESENT_POS_H			= 37;
		final  int PRESENT_SPEED_L			= 38;
		final  int PRESENT_SPEED_H			= 39;
		final  int PRESENT_LOAD_L			= 40;
		final  int PRESENT_LOAD_H			= 41;
		final  int PRESENT_VOLTAGE			= 42;
		final  int PRESENT_TEM				= 43;
		final  int MOVING					= 46;
		final  int PUNCH_L					= 48;
		final  int PUNCH_H					= 49;
		final  int MAXNUM_TXPARAM 			= 150;
		final  int MAXNUM_RXPARAM 			= 60;  	
		final static  int BROADCAST_ID  	= 254;		
		final  int INST_PING 				= 1; 		
		final  int INST_READ  				= 2;		
		final  int INST_WRITE 				= 3;	
		final  int INST_REG_WRITE 			= 4; 	
		final  int INST_ACTION 				= 5;		
		final  int INST_RESET 				= 6;			
		final  int INST_SYNC_WRITE 			= 131;	
		final  int ERRBIT_VOLTAGE 			= 1; 	
		final  int ERRBIT_ANGLE 			= 2;	
		final  int ERRBIT_OVERHEAT 			= 4; 	

		
		final  int ERRBIT_RANGE 			= 8; 		
		final  int ERRBIT_CHECKSUM 			= 16;	
		final  int ERRBIT_OVERLOAD 			= 32;	
		final  int ERRBIT_INSTRUCTION 		= 64;
		
		
		////// Definition for Transmit data///////
		//------------------------------------------------------------//
		//
		final  int ID 						= 2;					
		final  int LENGTH 					= 3;				
		final  int INSTRUCTION 				= 4;		
		final  int ERRBIT 					= 4;				
		final  int PARAMETER 				= 5;			
		final  int[] gbInstructionPacket 	= new int[MAXNUM_TXPARAM+10]; 
		byte[] gbStatusPacket 				= new byte[MAXNUM_RXPARAM+10]; 
		int checksum 						=0;
		// Constructor
		public  DynamixelMotor(UsbSerialDriver driver, String dynamixel_type, int dynamixel_ID, float home_pos, float range)
		{
			this.dxl_serial = driver;
			this.DXL_ID = dynamixel_ID;
			this.DXL_TYPE = dynamixel_type;
			this.HOME_POS = home_pos;
			this.MAX_POS_LIMIT = this.HOME_POS + range/2;
			this.MIN_POS_LIMIT = this.HOME_POS - range/2;
			
			this.dxl_init();
		}
		private void dxl_init()
		{
			if (this.DXL_TYPE.equals("AX12"))
			{
				this.MAX_POS_RESGISTER = 1023;
				this.MIN_POS = 0;
				this.MAX_POS = 300;
				this.MAX_SPEED_RESGISTER = 1023;
				this.MIN_SPEED = 1;
				this.MAX_SPEED = 114;
				this.MAX_TORQUE_LIMIT_RESGISTER = 1023;
				this.MIN_TORQUE = 0;
				this.MAX_TORQUE = 100;
			}
			else
				if (this.DXL_TYPE.equals("MX28"))
				{
					this.MAX_POS_RESGISTER = 4095;
					this.MIN_POS = 0;
					this.MAX_POS = 360;
					this.MAX_SPEED_RESGISTER = 1023;
					this.MIN_SPEED = 1;
					this.MAX_SPEED = 114;
					this.MAX_TORQUE_LIMIT_RESGISTER = 1023;
					this.MIN_TORQUE = 0;
					this.MAX_TORQUE = 100;
				}
			this.POS_RESOLUTION = (float) (this.MAX_POS_RESGISTER/(MAX_POS-MIN_POS));
			this.SPEED_RESOLUTION = (float)(this.MAX_SPEED_RESGISTER/(MAX_SPEED-MIN_SPEED));
			this.TORQUE_RESOLUTION = (float)(this.MAX_TORQUE_LIMIT_RESGISTER/(this.MAX_TORQUE - this.MIN_TORQUE));
			reset_tx_data();
			reset_rx_data();
			this.dxl_write_byte_act(this.TORQUE_ENABLE, 0);
			this.dxl_write_byte_act(this.STATUS_RETURN_LEVEL, 1);
			this.dxl_write_word_act(this.SPEED_L,this.DEFAULT_SPEED);
			this.dxl_write_byte_act(this.TORQUE_ENABLE, 1);
			this.dxl_write_word_act(this.TORQUE_LIMIT_L, 1023);
		}
		private  void dxl_send()
		{
			int n = this.gbInstructionPacket[this.LENGTH]+4;
			byte[] tx_buffer = new byte[n];
			String s = "";
			for (int i=0; i<n;i++)
			{
				
				tx_buffer[i]=(byte)this.gbInstructionPacket[i];
				s=String.format("%s %X",s,tx_buffer[i]);
			}
			try {
				dxl_serial.write(tx_buffer, 10);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void reset_tx_data()
		{
			/*
			 * Clean TX data
			 * 
			 * */
			gbInstructionPacket[0]=255;
			gbInstructionPacket[1]=255;
			for (int i = ID; i<MAXNUM_TXPARAM+10;i++)
			gbInstructionPacket[i]=0;
		}
		private void reset_rx_data()
		{
			for (int i = 0; i<this.gbStatusPacket.length;i++)
				this.gbStatusPacket[i]=0;
		}
		int dxl_get_low_byte(int word)
		{
			/*
			 * Get low byte of word
			 * */
			byte tem = (byte)word;
			return (int)tem;
		}
		int dxl_get_high_byte(int word)
		{
			/*
			 * Get high byte of word
			 * */
			byte tem = (byte)(word>>8);
			return (int)tem;
		}
		private void dxl_checksum()
		{
			/*
			 * make checksum byte for each data package
			 * */
			checksum=0;
			for( int i=0; i<(gbInstructionPacket[LENGTH]+1); i++ )
			checksum += gbInstructionPacket[i+2];
			byte k = (byte)(checksum);
			k=(byte) ~k;
			gbInstructionPacket[gbInstructionPacket[LENGTH]+3] = k;
			
		}
		void dxl_ping()
		{
			this.gbInstructionPacket[ID] = this.DXL_ID;
			this.gbInstructionPacket[INSTRUCTION] = INST_PING;
			this.gbInstructionPacket[LENGTH] = 2;
			this.dxl_checksum();
			this.dxl_send();
		}
		private float dxl_pos_check(float pos)
		{
			if (pos > this.MAX_POS_LIMIT) pos = this.MAX_POS_LIMIT;
			if (pos < this.MIN_POS_LIMIT) pos = this.MIN_POS_LIMIT;
			return pos;
		}
		private float dxl_speed_check(float speed)
		{
			if (speed> this.MAX_SPEED_LIMIT) speed = this.MAX_SPEED_LIMIT;
			if (speed< this.MIN_SPEED_LIMIT) speed = this.MIN_SPEED_LIMIT;
			return speed;
		}
		private int dxl_torque_check(int torque)
		{
			if(torque>this.MAX_TORQUE_LIMIT_RESGISTER) torque = this.MAX_TORQUE_LIMIT_RESGISTER;
			if (torque<0) torque = 0;
			return torque;
			
		}
		void dxl_write_byte(int table_address, int value)
		{
			/*
			 * Prepare data package for address that have 1 byte parameter
			 * Storage data into control table and wait for trigger signal
			 * */
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] = INST_REG_WRITE;
			gbInstructionPacket[PARAMETER] = table_address;
			gbInstructionPacket[PARAMETER+1] = value;
			gbInstructionPacket[LENGTH] = 4;
			dxl_checksum();
			this.dxl_send();
		}	
		void dxl_write_byte_act(int table_address, int value)
		{
			/*
			 * Prepare data package for address that have 1 byte parameter
			 * Included trigger signal
			 * */
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] = INST_WRITE;
			gbInstructionPacket[PARAMETER] = table_address;
			gbInstructionPacket[PARAMETER+1] = value;
			gbInstructionPacket[LENGTH] = 4;
			dxl_checksum();
			this.dxl_send();
		}
		void dxl_read_byte_act(int table_address)
		{
			/*
			 * Prepare data package for address that have 1 byte parameter
			 * Storage data into control table and wait for trigger signal
			 * */
			
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] = INST_READ;
			gbInstructionPacket[PARAMETER] = (byte)table_address;
			gbInstructionPacket[PARAMETER+1] = 1;
			gbInstructionPacket[LENGTH] = 4; 
			dxl_checksum();
			this.dxl_send();
			try {
				this.reset_rx_data();
				int l = this.dxl_serial.read(this.gbStatusPacket, 1);
				Log.i("Neck", String.format("length = %d", l));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		void dxl_write_word(int table_address, int value)
		{
			/*
			 * Prepare data package for address that have 2 byte parameter
			 * Storage data into control table and wait for trigger signal
			 * */
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] 	= INST_REG_WRITE;
			gbInstructionPacket[PARAMETER] 		= table_address;
			gbInstructionPacket[PARAMETER+1] 	= dxl_get_low_byte(value);
			gbInstructionPacket[PARAMETER+2] 	= dxl_get_high_byte(value);
			gbInstructionPacket[LENGTH] = 5;
			dxl_checksum();
			this.dxl_send();
		}
		void dxl_write_word_act(int table_address, int value)
		{
			/*
			 * Prepare data package for address that have 2 byte parameters
			 * Included trigger signal
			 * */
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] 	= INST_WRITE;
			gbInstructionPacket[PARAMETER] 		= table_address;
			gbInstructionPacket[PARAMETER+1] 	= dxl_get_low_byte(value);
			gbInstructionPacket[PARAMETER+2] 	= dxl_get_high_byte(value);
			gbInstructionPacket[LENGTH] = 5;
			dxl_checksum();
			this.dxl_send();
		}
		private void dxl_get_package()
		{
			// check header file
			 
		}
		void dxl_read_word_act(int table_address)
		{
			/*
			 * Prepare data package for address that have 1 byte parameter
			 * Storage data into control table and wait for trigger signal
			 * */
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] = INST_READ;
			gbInstructionPacket[PARAMETER] = table_address;
			gbInstructionPacket[PARAMETER+1] = 2;
			gbInstructionPacket[LENGTH] = 4;
			dxl_checksum();
			this.dxl_send();
			try {
				this.reset_rx_data();
				int l = this.dxl_serial.read(this.gbStatusPacket, 1);
				Log.i("Neck", String.format("length = %d",100));
				byte x = this.gbStatusPacket[this.PARAMETER];
				byte y = this.gbStatusPacket[this.PARAMETER+1];
				int i = y;
				i = i<<8;
				i = (int)i+x;
				this.CURRENT_POS = (float) (i*this.POS_RESOLUTION);
				Log.i("Neck","current pos  = "+ this.CURRENT_POS);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		void dxl_trigger()
		{
			/*
			 * prepare trigger data
			 * */
			gbInstructionPacket[ID] = BROADCAST_ID;
			gbInstructionPacket[INSTRUCTION] = INST_ACTION;
			gbInstructionPacket[LENGTH] = 2;
			dxl_checksum();
			this.dxl_send();
		}		
		// POSITION
		void dxl_set_position(float deg)
		{
			deg = this.dxl_pos_check(deg);
			int value = (int) (deg*this.POS_RESOLUTION);
			dxl_write_word(GOAL_POS_L,value);
			
		}
		void dxl_set_position_act(float deg)
		{
			deg = this.dxl_pos_check(deg);
			int value = (int) (deg*this.POS_RESOLUTION);
			dxl_write_word_act(GOAL_POS_L,value);
			
		}
		void dxl_set_home_position()
		{
			this.dxl_set_speed_act(this.DEFAULT_SPEED);
			this.dxl_set_position_act(this.HOME_POS);
			this.CURRENT_POS = this.HOME_POS;
		}
		void dxl_get_position()
		{
			dxl_read_word_act(PRESENT_POS_L);
			dxl_read_word_act(PRESENT_POS_L);
			// send data to serial port
			// wait for Return delay time 4us
			// Read data frome comport
		}
		// SPEED
		void dxl_set_speed(float RPM)
		{
			RPM = this.dxl_speed_check(RPM);
			int tem = (int)(RPM*this.SPEED_RESOLUTION);
			dxl_write_word(SPEED_L, tem );
			
		}
		void dxl_set_speed_act(float RPM)
		{
			int tem = (int)(RPM*this.SPEED_RESOLUTION);
			dxl_write_word_act(SPEED_L, tem );
			dxl_send();
			
			
		}
		// TORQUE
		void dxl_set_torque_limit(float value)
		{
			int tem = (int) (value*this.TORQUE_RESOLUTION);
			tem = this.dxl_torque_check(tem);
			dxl_write_word(MAX_TORQUE_L, tem);
			
		}
		void dxl_set_torque_limit_act(float value)
		{
			int tem = (int) (value*this.TORQUE_RESOLUTION);
			tem = this.dxl_torque_check(tem);
			dxl_write_word_act(MAX_TORQUE_L, tem);
			dxl_send();
			
		}
		void dxl_torque_enable(int value)
		{
			if (value >0) value =1;
			if (value<0) value =0;
			dxl_write_byte(TORQUE_ENABLE,value);
		}
		void dxl_torqure_enable_act(int value)
		{
			int tem = (int) (value*this.TORQUE_RESOLUTION);
			tem = this.dxl_torque_check(tem);
			dxl_write_byte_act(TORQUE_ENABLE,value);
		}
		// CW SLOPE
		void dxl_set_CWSlope(int value)
		{
			dxl_write_byte(CW_SLOPE, (byte)value);
			
		}
		void dxl_set_CWSlope_act(int value)
		{
			dxl_write_byte_act(CW_SLOPE, (byte)value);
			
		}
		// CCW SLOPE
		void dxl_set_CCWSlope(int value)
		{
			dxl_write_byte(CCW_SLOPE, (byte)value);
					
		}
		void dxl_set_CCWSlope_act(int value)
		{
			dxl_write_byte_act(CCW_SLOPE, (byte)value);
					
		}
		// CC MARGIN
		void dxl_set_CWMArgin(int value)
		{
			dxl_write_byte(CW_MARGIN, (byte)value);
		}
		void dxl_set_CWMArgin_act(int value)
		{
			dxl_write_byte_act(CW_MARGIN, (byte)value);
		}
		void dxl_set_CCWMArgin(int value)
		{
			dxl_write_byte(CCW_MARGIN, (byte)value);
		}
		void dxl_set_CCWMArgin_act(int value)
		{
			dxl_write_byte_act(CCW_MARGIN, (byte)value);
		}
		// PUNCH
		void dxl_set_punch(int value)
		{
			dxl_write_word(PUNCH_L, value);
		}
		void dxl_set_punch_act(int value)
		{
			dxl_write_word_act(PUNCH_L, value);
		}
		// 
		String dxl_tx_data_str()
		{
			String s = "";
			int n = gbInstructionPacket[LENGTH]+4;
			for (int i = 0; i<n; i++)
				s=String.format("%s %X", s,(byte)gbInstructionPacket[i]);
			return s;
		}
		String dxl_rx_data_str()
		{
			String s = "";
			int n = this.gbStatusPacket[LENGTH]+4;
			for (int i = 0; i<n; i++)
				s=String.format("%s %X", s,(byte)gbStatusPacket[i]);
			return s;
		}
		
}




