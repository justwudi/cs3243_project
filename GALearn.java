import java.util.*;

public class GALearn extends Learn {
	Random rand = new Random();
	private final int mutation = 10;
	private final int averageChance = 10;

	private void generateRandomWeights(int numPermutations) {
		weightPermutations = new PlayerSkeleton.Weight[numPermutations];
		for (int i = 0; i < numPermutations; i++) {
			weightPermutations[i] = new PlayerSkeleton.Weight(totalFeatures);
		}
	}

	private PlayerSkeleton.Weight[] generateOffspringsByPairingParents(int cutOff) {
		// Create half of parents
		Integer[] indexArray = new Integer[cutOff];
		PlayerSkeleton.Weight[] offsprings = new PlayerSkeleton.Weight[cutOff/2];

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
			offsprings[i] = new PlayerSkeleton.Weight(offspring);
		}

		return offsprings;
	}

	private PlayerSkeleton.Weight[] generateOffspringsByCombination(int cutOff) {
		int combinations = 0;
		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				combinations++;
			}
		}

		PlayerSkeleton.Weight[] offsprings = new PlayerSkeleton.Weight[combinations];
		int offspringIndex = 0;

		for (int father = 0; father < cutOff - 1; father++) {
			for (int mother = father + 1; mother < cutOff; mother++) {
				double[] weight1 = weightPermutations[father].getWeights();
				double[] weight2 = weightPermutations[mother].getWeights();

				double[] offspring = new double[weight1.length];

				for (int i = 0; i < offspring.length; i++) {
					if (rand.nextInt(100) > averageChance) {
						offspring[i] = rand.nextInt(100) > 50 ? weight1[i] : weight2[i];
					} else {
						offspring[i] = (weight1[i] + weight2[i]) / 2;
					}
				}

				offsprings[offspringIndex] = new PlayerSkeleton.Weight(offspring);
				offspringIndex++;
			}
		}

		return offsprings;
	}

	private void nextGen(PlayerSkeleton.Weight[] offsprings) {
		int offspringIndex = 0;
		for (int i = weightPermutations.length - offsprings.length; i < weightPermutations.length; i++) {
			weightPermutations[i] = offsprings[offspringIndex];
			offspringIndex++;
		}
	}

	private void mutate(PlayerSkeleton.Weight[] offsprings, int cutOff) {
		for (int i = cutOff; i < weightPermutations.length - offsprings.length; i++) {
			weightPermutations[i].mutate(mutation);
		}
	}

	public static void main(String[] args) {
		long startTime, endTime;
		GALearn ga = new GALearn();
		ga.generateRandomWeights(100);
		int cutOff = 5;
		int totalIteration = 1000;
		for (int iteration = 0; iteration < totalIteration; iteration++) {
			startTime = System.currentTimeMillis();
			ga.computeScores();
			Arrays.sort(ga.weightPermutations);
			PlayerSkeleton.Weight[] offsprings = ga.generateOffspringsByCombination(cutOff);
			ga.nextGen(offsprings);
			ga.mutate(offsprings, cutOff);
			System.out.println("Iteration " + (iteration + 1) + ": Max "+ga.weightPermutations[0].score+" rows.");
			System.out.println(ga.weightPermutations[0]);
			endTime = System.currentTimeMillis();
			ga.printTimeMillis(endTime - startTime);
		}
	}
}
