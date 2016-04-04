import java.util.*;

public class GALearn extends Learn {
	private final double mutation = 0.05;
	private final double averageChance = 0.1;

	private void generateRandomWeights(int numPermutations) {
		weightPermutations = new Weight[numPermutations];
		for (int i = 0; i < numPermutations; i++) {
			weightPermutations[i] = new Weight(totalFeatures);
		}
	}

	private Weight[] generateOffspringsByPairingParents(int cutOff) {
		// Create half of parents
		Integer[] indexArray = new Integer[cutOff];
		Weight[] offsprings = new Weight[cutOff/2];

		for (int i = 0; i < cutOff; i++) indexArray[i] = i;

		List<Integer> l = Arrays.asList(indexArray);
		Collections.shuffle(l);

		for (int i = 0; i < cutOff/2; i++) {
			double[] weight1 = weightPermutations[l.get(i*2)].getWeights();
			double[] weight2 = weightPermutations[l.get(i*2+1)].getWeights();
			double[] offspring = new double[weight1.length];
			for (int j = 0; j < offspring.length; j++) {
				offspring[j] = (weight1[j] + weight2[j]) / 2;
			}
			offsprings[i] = new Weight(offspring);
		}

		return offsprings;
	}

	private Weight[] generateOffspringsByCombination(int cutOff) {
		int combinations = 0;
		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				combinations++;
			}
		}

		Weight[] offsprings = new Weight[combinations];
		int offspringIndex = 0;

		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				double[] weight1 = weightPermutations[father].getWeights();
				double[] weight2 = weightPermutations[mother].getWeights();

				double[] offspring = new double[weight1.length];

				for (int i = 0; i < offspring.length; i++) {
					if (Math.random() > averageChance) {
						offspring[i] = Math.random() > 0.5 ? weight1[i] : weight2[i];
					} else {
						offspring[i] = (weight1[i] + weight2[i]) / 2;
					}
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

	private void mutate(Weight[] offsprings) {
		for (int i = 0; i < weightPermutations.length - offsprings.length; i++) {
			if (Math.random() < mutation) {
				weightPermutations[i].mutate();
			}
		}
	}

	public static void main(String[] args) {
		GALearn ga = new GALearn();
		ga.generateRandomWeights(500);
		int cutOff = 10;
		int totalIteration = 1000;
		for (int iteration = 0; iteration < totalIteration; iteration++) {
			ga.computeScores();
			Arrays.sort(ga.weightPermutations);
			Weight[] offsprings = ga.generateOffspringsByCombination(cutOff);
			ga.nextGen(offsprings);
			ga.mutate(offsprings);
			System.out.println("Max "+ga.weightPermutations[0].score+" rows.");
			printArray(ga.weightPermutations[0].getWeights());
		}
	}
}
