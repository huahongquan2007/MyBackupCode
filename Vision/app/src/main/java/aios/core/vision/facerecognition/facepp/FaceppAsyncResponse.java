package aios.core.vision.facerecognition.facepp;

public interface FaceppAsyncResponse {
	void processFinish(String output);
	void updateLastPersonAddTime();
	void trainFinish();
}