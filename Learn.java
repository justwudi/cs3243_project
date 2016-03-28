import java.util.*;

public class Learn {
	private final int totalFeatures = 9;
	private Weight[] weightPermutations;


	private void generateRandomWeights(int numPermutations) {
		weightPermutations = new Weight[numPermutations];
		for (int i = 0; i < numPermutations; i++) {
			weightPermutations[i] = new Weight(totalFeatures);
		}
	}

	private void computeScores() {
		for (int i = 0; i < weightPermutations.length; i++) {
			Weight w = weightPermutations[i];
			State state = new State();
			PlayerSkeleton p = new PlayerSkeleton();
			p.initWeights(w);
			while (!state.hasLost()) {
				state.makeMove(p.pickMove(state, state.legalMoves()));
			}
			w.score = state.getRowsCleared();
		}
	}

	private Weight[] generateOffsprings(int cutOff) {
		int cominations = 0;
		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				cominations++;
			}
		}

		Weight[] offsprings = new Weight[cominations];
		int offspringIndex = 0;

		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				double[] weight1 = weightPermutations[father].getWeights();
				double[] weight2 = weightPermutations[mother].getWeights();

				double[] offspring = new double[weight1.length];

				for (int i = 0; i < offspring.length; i++) {
					offspring[i] = (weight1[i] + weight2[i]) / 2;
				}

				offsprings[offspringIndex] = new Weight(offspring);
				offspringIndex++;
			}
		}

		return offsprings;
	}

	private void nextGen(Weight[] offsprings) {
		int offspringIndex = 0;
		for (int i = weightPermutations.length - offsprings.length; i < weightPermutations.length; i++) {
			weightPermutations[i] = offsprings[offspringIndex];
			offspringIndex++;
		}
	}

	public static void main(String[] args) {
		Learn l = new Learn();
		l.generateRandomWeights(500);
		int cutOff = 10;
		int totalIteration = 1000;
		for (int iteration = 0; iteration < totalIteration; iteration++) {
			l.computeScores();
			Arrays.sort(l.weightPermutations);
			Weight[] offsprings = l.generateOffsprings(cutOff);
			l.nextGen(offsprings);
			System.out.println("Max "+l.weightPermutations[0].score+" rows.");
		}
	}
}
