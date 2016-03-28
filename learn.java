import java.util.Random;

public class learn {
	
	Random rand = new Random(System.currentTimeMillis());
	
	public void generateRandomWeights(int time){
		double[] weights = new double[5];
		
		for(int i=0; i<weights.length; i++){
			weights[i] = rand.nextDouble();
		}
		
		//return weights;
	}
}
