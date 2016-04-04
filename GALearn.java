import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class GALearn {
	private final int totalFeatures = 16;
	private final double mutation = 0.05;
	private final double averageChance = 0.1;
	private Weight[] weightPermutations;

	private void generateRandomWeights(int numPermutations) {
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
		boolean readFile = false;
		boolean writeFile = true;
		String readFileName = "data1.txt";
		String saveFileName = "data1.txt";
		
//		if(args.length == 0){
//			readFile = false;
//			writeFile = false;
//		}
//		else if(args.length == 1){
//			readFile = false;
//			saveFileName = args[0];
//		}
//		else{
//			saveFileName = args[0];
//			readFileName = args[1];
//		}
		
		GALearn ga = new GALearn();
		int cutOff = 10;
		int totalIteration = 10;
		
		int dataToSave = 500;
		int dataToRead = 25;
		
		ga.weightPermutations = new Weight[500];
		
		if(!readFile){
			ga.generateRandomWeights(500);
		}
		if(readFile){
			try{
				String content;
				String[] array;
				double[] weights = new double[ga.totalFeatures];
				int index;
				
				FileReader fileReader = new FileReader(saveFileName);
				BufferedReader reader = new BufferedReader(fileReader);

				for(int i=0; i<500; i++){
					content = reader.readLine();
					array = content.split(", ");
					for(int j=0; j<ga.totalFeatures; j++){
						weights[j] = Double.parseDouble(array[j]);
					}
					//index = (int)(Math.random() * (500 - dataToRead + 1) + dataToRead - 1);
					ga.weightPermutations[i] = new Weight(weights);
				}
				reader.close();
				
				fileReader = new FileReader(readFileName);
				reader = new BufferedReader(fileReader);

				for(int i=0; i<dataToRead; i++){
					content = reader.readLine();
					array = content.split(", ");
					for(int j=0; j<ga.totalFeatures; j++){
						weights[j] = Double.parseDouble(array[j]);
					}
					index = (int)(Math.random() * (500 - dataToRead + 1) + dataToRead - 1);
					ga.weightPermutations[index] = new Weight(weights);
				}
				reader.close();
			}
			catch(Exception e){
				System.out.print("Error reading file.");
			}
		}
		
		for (int iteration = 0; iteration < totalIteration; iteration++) {
			ga.computeScores();
			Arrays.sort(ga.weightPermutations);
			Weight[] offsprings = ga.generateOffspringsByCombination(cutOff);
			ga.nextGen(offsprings);
			ga.mutate(offsprings);
			System.out.println("Max "+ga.weightPermutations[0].score+" rows.");
			printArray(ga.weightPermutations[0].getWeights());
		}
				
		if(writeFile){
			String content = "";
			double[] weights;
			for(int i=0; i<dataToSave; i++){
				weights = ga.weightPermutations[i].getWeights();
				for(int j=0; j<ga.totalFeatures; j++){
					content += weights[j] + ", ";
				}
				content += "\n";
			}

			try{		
				FileWriter fileWriter = new FileWriter(saveFileName);
				BufferedWriter writer = new BufferedWriter(fileWriter);
				writer.write(content);
				writer.close();
			}
			catch (Exception e){
				System.out.print(e);
				System.out.println("Error saving file.");
			}
		}
	}

	private static void printArray(double[] anArray) {
		for (int i = 0; i < anArray.length; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(String.format("%.2f", anArray[i]));
		}
		System.out.println();
	}
}
