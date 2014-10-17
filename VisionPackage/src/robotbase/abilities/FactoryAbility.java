package robotbase.abilities;

import android.content.Context;
import android.content.Intent;

public interface FactoryAbility {
	
	//method listing
	public boolean install();
	
	public boolean getAuthorize();
	
	public boolean addDevices();
	
	public boolean removeDevices();
	
	// register Broadcast
	public boolean registerIntent();
	
	//broadcast to
	
	public boolean broadcastIntent();
	
	public boolean onExcute( Context context, Intent intent);
}
