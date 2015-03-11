package aios.core.vision.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
		return saveImageWithDir(DIRECTORY_NAME, bitmap);
	}
	public static String saveImageWithDir(String dir_name, Bitmap bitmap){
		OutputStream output;
		SimpleDateFormat df = new SimpleDateFormat(
				"EEE, d MMM yyyy, HH:mm");
		// Find the SD Card path
		File externalPath = Environment.getExternalStorageDirectory();

		// Create a new folder in SD Card
		File dir = new File(externalPath.getAbsolutePath() + "/" + dir_name
				+ "/");
		dir.mkdirs();

		String fileName = String.valueOf(System.currentTimeMillis()) + (int)(Math.random() * 1000) + ".jpg";
		// Create a name for the saved image
		File file = new File(dir, fileName);
		String filePath = file.getAbsolutePath();
		try {
			output = new FileOutputStream(file);
			// Compress into png format image from 0% - 100%
			if (bitmap != null) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
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
	public static void makeToastTimerTask(Context context,
			Handler mHandler, String input, boolean isLong) {
		final String message = input;
		final Context ctx = context;
		final boolean isLongToast = isLong;
		Runnable makeToast = new Runnable() {
			public void run() {
				if(isLongToast){
					Toast.makeText(ctx,
							message,
							Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(ctx,
							message,
							Toast.LENGTH_SHORT).show();
				}

			}
		};
		mHandler.postDelayed(makeToast, 100);
	}
	public static void makeToastTimerTask(Context context, Handler mHandler, String input){
	makeToastTimerTask(context, mHandler, input, false);
	}
	
	public static Vector<String> getResultList(Context ctx, String filePath){
		Vector<String> vec = new Vector<String>();
		
		File textFile = new File(filePath);
		
	    try {
	    	BufferedReader reader = new BufferedReader(new FileReader(textFile));
	    	String line = "";
	    	do {
				line = reader.readLine();
	    		if(line!= null && !line.isEmpty()){
					vec.add(line);	    			
	    		}
			} while (line != null);
	    	reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("MyLog", "Read Result Files. Num Line = " + vec.size());
		return vec;
	}
	public static HashMap<String, Integer> getValList(
			Context applicationContext, String filePath, String rootPath) {
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		File textFile = new File(filePath);
	    try {
	    	BufferedReader reader = new BufferedReader(new FileReader(textFile));
	    	String line = "";
	    	do {
				line = reader.readLine();
				if(line!= null && !line.isEmpty()){
					String[] separated = line.split(" ");
					String path = separated[0];
					int label = Integer.parseInt(separated[1]);
					map.put(rootPath + path, label);
				}
			} while (line != null);
	    	reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("MyLog", "Read Validation Files. Num Val Line = " + map.size());
		return map;
	}
}
