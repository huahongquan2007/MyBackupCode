package robotbase.action;

import java.io.IOException;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
public class DynamixelMotor  {
	
	/// User definition
		// Serial Port 
		// Offset
//		public   UsbSerialDriver dxl_serial; 
		public   UsbSerialPort dxl_serial;
		public  float DXL_SIGN = 1;
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
		public   int   DXL_ID;
		public   String DXL_TYPE;
		
		//-------------------------------------------------------------------------//
	// Control Table  definition - Hardware Define////
		int 	   DXL_BAUDRATE				= 1000000;
		public  int MODEL_NUM_L				= 1;
		public  int MODEL_NUM_H				= 2;
		public  int MOTOR_ID					= 3;
		public  int BAUDRATE					= 4;
		public  int RETURN_DELAY_TIME		= 5;
		public  int CW_ANGLE_LIMIT_L			= 6;
		public  int CW_ANGLE_LIMIT_H			= 7;
		public  int CCW_ANGLE_LIMIT_L		= 8;
		public  int CCW_ANGLE_LIMIT_H		= 9;
		public  int HIGHEST_LIMIT_TEM		= 11;
		public  int LOWEST_LIMIT_VOLTAGE		= 12;
		public  int HIGHEST_LIMIT_VOLTAGE	= 13;
		public  int MAX_TORQUE_L				= 14;
		public  int MAX_TORQUE_H				= 15;
		public  int STATUS_RETURN_LEVEL		= 16;
		public  int ALARM_LED				= 17;
		public  int ALARM_SHUTDOWN			= 18;	
		public  int TORQUE_ENABLE			= 24;
		public  int CW_MARGIN				= 26;
		public  int CCW_MARGIN				= 27;
		public  int CW_SLOPE					= 28;
		public  int CCW_SLOPE				= 29;
		public  int GOAL_POS_L				= 30;
		public  int GOAL_POS_H				= 31;
		public  int SPEED_L					= 32;
		public  int SPEED_H					= 33;
		public  int TORQUE_LIMIT_L			= 34;
		public  int TORQUE_LIMIT_H			= 35;
		public  int PRESENT_POS_L			= 36; 
		public  int PRESENT_POS_H			= 37;
		public  int PRESENT_SPEED_L			= 38;
		public  int PRESENT_SPEED_H			= 39;
		public  int PRESENT_LOAD_L			= 40;
		public  int PRESENT_LOAD_H			= 41;
		public  int PRESENT_VOLTAGE			= 42;
		public  int PRESENT_TEM				= 43;
		public  int MOVING					= 46;
		public  int PUNCH_L					= 48;
		public  int PUNCH_H					= 49;
		
		public  int MAXNUM_TXPARAM 			= 150;
		public  int MAXNUM_RXPARAM 			= 60;  	
		public static  int BROADCAST_ID  	= 254;		
		
		// Instruction for communication - Hardware Define
		public  int INST_PING 				= 1; 		
		public  int INST_READ  				= 2;		
		public  int INST_WRITE 				= 3;	
		public  int INST_REG_WRITE 			= 4; 	
		public  int INST_ACTION 				= 5;		
		public  int INST_RESET 				= 6;			
		public  int INST_SYNC_WRITE 			= 131;	
		public  int ERRBIT_VOLTAGE 			= 1; 	
		public  int ERRBIT_ANGLE 			= 2;	
		public  int ERRBIT_OVERHEAT 			= 4; 	

		
		public  int ERRBIT_RANGE 			= 8; 		
		public  int ERRBIT_CHECKSUM 			= 16;	
		public  int ERRBIT_OVERLOAD 			= 32;	
		public  int ERRBIT_INSTRUCTION 		= 64;
		
		
		////// Definition for Transmit data///////
		//------------------------------------------------------------//
		//
		public  int ID 						= 2;					
		public  int LENGTH 					= 3;				
		public  int INSTRUCTION 				= 4;		
		public  int ERRBIT 					= 4;				
		public  int PARAMETER 				= 5;			
		public  int[] gbInstructionPacket 	= new int[MAXNUM_TXPARAM+10]; 
		byte[] gbStatusPacket 				= new byte[MAXNUM_RXPARAM+10]; 
		int rx_data_length					= 0;
		boolean rx_ready 					= false;
		float current_speed					= 0;
		// Constructor

		public  DynamixelMotor(UsbSerialPort driver,Joint joint )
		{
			this.dxl_serial = driver;
			this.DXL_ID = joint.ID;
			Log.i("Neck", String.format("ID init = %d", this.DXL_ID));
			this.DXL_TYPE = joint.TYPE;
			this.HOME_POS = joint.HOME_POS;
			this.MAX_POS_LIMIT = this.HOME_POS + joint.POS_RANGE/2;
			this.MIN_POS_LIMIT = this.HOME_POS - joint.POS_RANGE/2;
			this.MAX_SPEED_LIMIT = joint.MAX_SPEED_LIMIT;
			this.MIN_SPEED_LIMIT = joint.MIN_SPEED_LIMIT;
			this.DXL_SIGN = joint.SIGN;
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
			if (this.dxl_serial != null)
			{
				try 
				{
					this.dxl_serial.write(tx_buffer, 10);
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else
				Log.i("Neck", "USB2Dynamixel device not found!");
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
		private void dxl_tx_checksum()
		{
			/*
			 * make checksum byte for each data package
			 * */
			int tx_checksum=0;
			for( int i=0; i<(gbInstructionPacket[LENGTH]+1); i++ )
			tx_checksum += gbInstructionPacket[i+2];
			byte k = (byte)(tx_checksum);
			k=(byte) ~k;
			gbInstructionPacket[gbInstructionPacket[LENGTH]+3] = k;
			
		}
		private byte dxl_rx_checksum()
		{
			/*
			 * make checksum byte for each rx package
			 * */
			int rx_checksum = 0;
			for( int i = 0;  i< (this.gbStatusPacket[LENGTH] + 1); i++ )
			rx_checksum  += this.gbStatusPacket[i + 2];
			byte k = (byte)(rx_checksum);
			k=(byte) ~k;
			return k;
		}
		void dxl_ping()
		{
			this.gbInstructionPacket[ID] = this.DXL_ID;
			this.gbInstructionPacket[INSTRUCTION] = INST_PING;
			this.gbInstructionPacket[LENGTH] = 2;
			this.dxl_tx_checksum();
			this.dxl_send();
		}
		private float dxl_pos_check(float pos)
		{
			pos = this.HOME_POS + this.DXL_SIGN*pos;
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
			dxl_tx_checksum();
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
			dxl_tx_checksum();
			this.dxl_send();
		}
		void dxl_read_byte_act(int table_address) throws InterruptedException
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
			dxl_tx_checksum();
			this.dxl_send();
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
			dxl_tx_checksum();
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
			dxl_tx_checksum();
			this.dxl_send();
		}
		private void dxl_get_package()
		{
			// check header file
			 
		}
		void dxl_read_word_act(int table_address)
		{
			/*
			 * Prepare data package for address that have 2 byte parameter
			 * Storage data into control table and wait for trigger signal
			 * */
			// Prepare read command
			gbInstructionPacket[ID] = this.DXL_ID;
			gbInstructionPacket[INSTRUCTION] = INST_READ;
			gbInstructionPacket[PARAMETER] = table_address;
			gbInstructionPacket[PARAMETER+1] = 2;
			gbInstructionPacket[LENGTH] = 4;
			dxl_tx_checksum();
			// send to motor
			this.dxl_send();
		}		
		void dxl_trigger()
		{
			/*
			 * prepare trigger data
			 * */
			gbInstructionPacket[ID] = BROADCAST_ID;
			gbInstructionPacket[INSTRUCTION] = INST_ACTION;
			gbInstructionPacket[LENGTH] = 2;
			dxl_tx_checksum();
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
			this.dxl_set_position_act(0);
			this.CURRENT_POS = 0;
		}
		void dxl_get_position()
		{
			// Prepare instruction data to send
			
			rx_ready = true;
			reset_rx_data();
			reset_tx_data();
			while (this.check_rx_package()==false)
			{
				if (rx_ready == true)
				{
					reset_rx_data();
					dxl_read_word_act(PRESENT_POS_L);
					rx_ready = false;
				}
			}
				int word = this.dxl_make_word(this.gbStatusPacket[this.PARAMETER], this.gbStatusPacket[this.PARAMETER+1]);
				float tam = (float) word;
				tam = tam/this.POS_RESOLUTION;
				tam = tam - this.HOME_POS;
				tam = tam/this.DXL_SIGN;
				this.CURRENT_POS = tam;
			
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
			RPM = this.dxl_speed_check(RPM);
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
		boolean check_rx_package()
		
		{
			
			// checking ID
			if (this.DXL_ID != this.gbStatusPacket[this.ID] )
			{
//				Log.i("Neck", "wrong ID:"+this.gbStatusPacket[this.ID]+"/"+this.DXL_ID);
				return false;
			}
			// Checking package Length
			if (this.rx_data_length != this.gbStatusPacket[this.LENGTH]+4)
			{
//				Log.i("Neck", "wrong length:" + (this.gbStatusPacket[this.LENGTH]+4) +"/" +this.rx_data_length);
				return false;
			}
			// Checking checksum
			if (this.gbStatusPacket[this.rx_data_length-1] != this.dxl_rx_checksum())
			{
//				Log.i("Neck",this.gbStatusPacket[this.rx_data_length-1]+"/"+ this.dxl_rx_checksum());
				return false;
			}
			// checking error byte
			if (this.gbStatusPacket[this.ERRBIT] != 0)
			{
//				Log.i("Neck", "Error Code:" + this.gbStatusPacket[this.ERRBIT]);
				
				return false;
			}
			Log.i("Neck", "RX OK");
			return true;
		}
		int dxl_make_word(byte low_byte, byte high_byte)
		{
			int word = (int)high_byte;
			word = word<<8;
			word = word+(int)low_byte;
			return word;
		}
}




