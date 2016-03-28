import java.util.*;

public class Learn {
	private final int totalFeatures = 11;
	private final double mutation = 0.05;
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
		// int cominations = 0;
		// for (int father = 0; father < cutOff - 1; father++) {
		// 	for (int mother = father + 1; mother < cutOff; mother++) {
		// 		cominations++;
		// 	}
		// }

		// Weight[] offsprings = new Weight[cominations];
		// int offspringIndex = 0;

		// for (int father = 0; father < cutOff - 1; father++) {
		// 	for (int mother = father + 1; mother < cutOff; mother++) {
		// 		double[] weight1 = weightPermutations[father].getWeights();
		// 		double[] weight2 = weightPermutations[mother].getWeights();

		// 		double[] offspring = new double[weight1.length];

		// 		for (int i = 0; i < offspring.length; i++) {
		// 			// offspring[i] = Math.random() > 0.5 ? weight1[i] : weight2[i];

		// 			offspring[i] = (weight1[i] + weight2[i]) / 2;
		// 		}

		// 		offsprings[offspringIndex] = new Weight(offspring);
		// 		offspringIndex++;
		// 	}
		// }
		Integer[] indexArray = new Integer[cutOff];
		for (int i = 0; i < cutOff; i++) indexArray[i] = i;
		Weight[] offsprings = new Weight[cutOff/2];
		List<Integer> l = Arrays.asList(indexArray);
		Collections.shuffle(l);
		for (int i = 0; i < cutOff/2; i++) {
			double[] weight1 = weightPermutations[l.get(i*2)].getWeights();
			double[] weight2 = weightPermutations[l.get(i*2+1)].getWeights();
			double[] offspring = new double[weight1.length];
			for (int j = 0; j < offspring.length; j++) {
				// offspring[i] = Math.random() > 0.5 ? weight1[i] : weight2[i];
				offspring[j] = (weight1[j] + weight2[j]) / 2;
			}
			offsprings[i] = new Weight(offspring);
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

	private void mutate() {
		for (int i = 0; i < weightPermutations.length; i++) {
			if (Math.random() < mutation) {
				weightPermutations[i].mutate();
			}
		}
	}

	public static void main(String[] args) {
		Learn l = new Learn();
		l.generateRandomWeights(500);
		int cutOff = 50;
		int totalIteration = 1000;
		for (int iteration = 0; iteration < totalIteration; iteration++) {
			l.computeScores();
			Arrays.sort(l.weightPermutations);
			Weight[] offsprings = l.generateOffsprings(cutOff);
			l.nextGen(offsprings);
			l.mutate();
			System.out.println("Max "+l.weightPermutations[0].score+" rows.");
		}
	}
}
