import java.util.Random;

public class Learn {
	
	Random rand = new Random(System.currentTimeMillis());
	
	public void generateRandomWeights(int time){
		double[] weights = new double[5];
		
		for(int i=0; i<weights.length; i++){
			weights[i] = rand.nextDouble();
		}
		
		//return weights;
	}

	public static void main(String[] args) {
		// generateRandomWeights(500);
		// int totalIteration = 100;
		// for (int iteration = 0; iteration < totalIteration; iteration++) {
		// 	computeScores();
		// 	generateOffsprings();
		// 	nextGen();
		// }
	}
}
