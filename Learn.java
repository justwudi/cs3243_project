import java.util.Random;

public class Learn {

	private Weight[] weightPermutations;

	private void generateRandomWeights(int numPermutations) {
		weightPermutations = new Weight[numPermutations];
		for (int i = 0; i < numPermutations; i++) {
			weightPermutations[i] = new Weight();
		}
	}

	public static void main(String[] args) {
		generateRandomWeights(500);
		int totalIteration = 100;
		// for (int iteration = 0; iteration < totalIteration; iteration++) {
		// 	computeScores();
		// 	generateOffsprings();
		// 	nextGen();
		// }
	}
}
