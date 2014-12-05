package robotbase.vision;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;

public class VisionUtilities {
	public static final String DIRECTORY_NAME = "Test";
	public static Bitmap combineImages(Vector<Bitmap> vec, int width, int height, int numOfWidth, int numOfHeight){
		Bitmap sendBitmap = Bitmap.createBitmap(width * numOfWidth, height * numOfHeight, Bitmap.Config.ARGB_8888);
	    Canvas comboImage = new Canvas(sendBitmap); 

	    for(int i = 0 ; i < numOfWidth; i++){
	    	for(int j = 0 ; j < numOfHeight ; j++){
	    		comboImage.drawBitmap(vec.elementAt(i + j * numOfWidth), width * i , height * j, null);	
	    	}
	    }
	    return sendBitmap;
	}
	public static String saveImage(Bitmap bitmap) {
		OutputStream output;
		SimpleDateFormat df = new SimpleDateFormat(
				"EEE, d MMM yyyy, HH:mm");
		// Find the SD Card path
		File externalPath = Environment.getExternalStorageDirectory();

		// Create a new folder in SD Card
		File dir = new File(externalPath.getAbsolutePath() + "/" + DIRECTORY_NAME
				+ "/");
		dir.mkdirs();

		String fileName = df.format(Calendar.getInstance().getTime()) + ".png";
		// Create a name for the saved image
		File file = new File(dir, fileName);
		String filePath = file.getAbsolutePath();
		try {
			output = new FileOutputStream(file);
			// Compress into png format image from 0% - 100%
			if (bitmap != null) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			}
			output.flush();
			output.close();
		}

		catch (Exception e) {
			filePath = "SORRY, THERE IS AN ERROR WHILE SAVING!";
			e.printStackTrace();
		}
		return filePath;
	}
}
