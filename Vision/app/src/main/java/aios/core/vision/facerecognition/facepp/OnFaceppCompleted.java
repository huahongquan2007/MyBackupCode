package aios.core.vision.facerecognition.facepp;

public interface OnFaceppCompleted {
    public void personAddFace(String person_name, byte[] data, String face_id);
}
