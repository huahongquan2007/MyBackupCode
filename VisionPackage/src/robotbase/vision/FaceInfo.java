package robotbase.vision;

import android.os.Parcel;
import android.os.Parcelable;

public class FaceInfo implements Parcelable {
	public float x, y , w , h;
	public long time;

	public FaceInfo(float _x, float _y, float _w, float _h, long _t) {
		x = _x; y = _y; w = _w; h = _h;
		time = _t;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel pc, int flags) {
		pc.writeFloat(x);
		pc.writeFloat(y);
		pc.writeFloat(w);
		pc.writeFloat(h);
		pc.writeLong(time);
	}

	/** Static field used to regenerate object, individually or as arrays */
	public static final Parcelable.Creator<FaceInfo> CREATOR = new Parcelable.Creator<FaceInfo>() {
		public FaceInfo createFromParcel(Parcel pc) {
			return new FaceInfo(pc);
		}

		public FaceInfo[] newArray(int size) {
			return new FaceInfo[size];
		}
	};

	public FaceInfo(){
		x = y = w = y = time = 0;
	}
	public FaceInfo(Parcel pc) {
		x = pc.readFloat();
		y = pc.readFloat();
		w = pc.readFloat();
		h = pc.readFloat();
		time = pc.readLong();
	}
}