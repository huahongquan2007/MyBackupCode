//package robotbase.utility;
//
//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import android.graphics.Bitmap;
//import android.util.Log;
//
//public class UtilitiesBackup {
//	static String crlf = "\r\n";
//	static String twoHyphens = "--";
//	static String boundary =  "*****";
//
//	public static void sendImage(){
//		Bitmap bitmap = null;
//		// Send image data to server
//		ByteArrayOutputStream byteStream = null;
//		try {		
//		    byteStream = new ByteArrayOutputStream();
//		    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
//
//		    HttpURLConnection httpUrlConnection = null;
//		    URL url = new URL("http://example.com/server.cgi");
//		    httpUrlConnection = (HttpURLConnection) url.openConnection();
//		    httpUrlConnection.setUseCaches(false);
//		    httpUrlConnection.setDoOutput(true);
//		    httpUrlConnection.setDoInput(true);
//		    httpUrlConnection.setRequestMethod("POST");
//		    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
//		    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
//		    httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//		    DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
//
////		    request.wr
//		} catch (Exception e) {
//		    e.printStackTrace();
//		} finally {
//		    try {
//		        if (byteStream != null) byteStream.close();
//		    } catch (Exception e) {}
//		}
//	}
//}
