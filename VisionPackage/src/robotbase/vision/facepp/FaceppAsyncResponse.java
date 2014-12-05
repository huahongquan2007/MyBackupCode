package robotbase.vision.facepp;

public interface FaceppAsyncResponse {
	void processFinish(String input, String output);
	void updateLastPersonAddTime();
	void trainFinish();
}