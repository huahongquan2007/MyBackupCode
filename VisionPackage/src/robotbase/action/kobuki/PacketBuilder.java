package robotbase.action.kobuki;

public class PacketBuilder {
	
	private volatile short curSetVelocity; //set by user
	
	public PacketBuilder() {
		curSetVelocity = 0; 
	}
	
	public final byte[] buildBaseControlPacket(short velocity, short radius) { // Units = mm
		curSetVelocity = velocity;
    	byte[] go = new byte[10]; 
    	go[0] = (byte) 0xAA; // Header
    	go[1] = 0x55; // Header
    	go[2] = 0x06; // Length of payload
    	go[3] = 0x01; // Header of payload
    	go[4] = 0x04; // Length sub-payload
    	go[5] = (byte)(velocity & 0xff); // Byte 2 of 2 velocity
    	go[6] = (byte)((velocity >> 8) & 0xff); // Byte 1 of 2 velocity
    	go[7] = (byte)(radius & 0xff); // Byte 2 of 2 radius
    	go[8] = (byte)((radius >> 8) & 0xff); // Byte 1 of 2 radius
    	go[9] = (byte) (go[2]^go[3]^go[4]^go[5]^go[6]^go[7]^go[8]); // Checksum
		return go; // Send back go
	}
	
	/**
	 * @return the curSetVelocity
	 */
	public final short getCurSetVelocity() {
		return curSetVelocity;
	}	
}
