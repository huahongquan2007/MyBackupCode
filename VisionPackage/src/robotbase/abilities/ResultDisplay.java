package robotbase.abilities;

//import robotbase.face.Emotion;
import android.os.Parcel;
import android.os.Parcelable;

public class ResultDisplay implements Parcelable{
	class Emotion{
		public static final String SHAKE = "";
		public static final String SMILE = "";
	}
	
	//For verify action result
	public String action;
	
	//For request emotion 
	public String emotion;
	
	// for Title dialog show static card
	public String title;
	
	// for content Static card
	public String html;
	
	// int show = 0: display all staticcard and emotion
	// int show = 1: display only emotion
	// int show = 2: display only static card
	public int show;

	public ResultDisplay(int show, String act, String emot, String tit, String htm){
		
		this.show = show;
		this.action = act;
		this.emotion = emot;
		this.title = tit;
		this.html = htm;
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(this.action);
		dest.writeString(this.emotion);
		dest.writeString(title);
		dest.writeString(html ); 
		dest.writeInt(show);
		
	} 
	/** Static field used to regenerate object, individually or as arrays */
	  public static final Parcelable.Creator<ResultDisplay> CREATOR = new Parcelable.Creator<ResultDisplay>() {
	         public ResultDisplay createFromParcel(Parcel pc) {
	             return new ResultDisplay(pc);
	         }
	         public ResultDisplay[] newArray(int size) {
	             return new ResultDisplay[size];
	         }
	   };
	
	   /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
	   public ResultDisplay(Parcel pc){
		   action        = pc.readString();
		   emotion        =  pc.readString();
		   title      = pc.readString();
		   html = pc.readString();
		   show = pc.readInt();
		   
	  }

	public ResultDisplay(int show2, String act, Emotion smile, String tit,
			String htm) {
		// TODO Auto-generated constructor stub 
	}
}
