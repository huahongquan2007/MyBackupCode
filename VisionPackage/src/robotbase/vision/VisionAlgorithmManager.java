package robotbase.vision;

import java.util.ArrayList;
import java.util.List;

public class VisionAlgorithmManager {
	private List<VisionAlgorithm> listAlgo;
	public VisionAlgorithmManager() {
		listAlgo = new ArrayList<VisionAlgorithm>();
	}
	public void addAlgo(VisionAlgorithm algo){
		listAlgo.add(algo);
	}
	public void removeAlgo(String name){
		int index = -1;
		for(VisionAlgorithm algo : listAlgo){
			if(algo.getName().equals(name)){
				index = listAlgo.indexOf(algo);
				break;
			}
		}

		if(index != -1){
			listAlgo.remove(index);
		}
	}
	public void start(){
		for(VisionAlgorithm algo : listAlgo){
			algo.start();
		}
	}
	public void stop(){
		for(VisionAlgorithm algo : listAlgo){
			algo.stop();
		}
	}
	public void update(byte[] frame){
		for(VisionAlgorithm algo : listAlgo){
			algo.update(frame);
		}
	}
	public String getResult(String name){
		for(VisionAlgorithm algo : listAlgo){
			if(algo.getName().equals(name)){
				return algo.getResult();
			}
		}
		return "";
	}
	public VisionAlgorithm getAlgo(String name){
		for(VisionAlgorithm algo : listAlgo){
			if(algo.getName().equals(name)){
				return algo;
			}
		}
		return null;
	}
}