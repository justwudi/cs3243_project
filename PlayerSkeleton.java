import java.util.*;

public class PlayerSkeleton {

	private final int SIZE = State.COLS * State.ROWS;

	// Current state properties
	private double currentAverageHeight;
	private int currentMaxHeight;
	private int currentMinHeight;
	private int currentHoles;

	// Next state properties
	private int[][] nextField;
	private int[] heightArray;

	private int rowsCleared = 0;
	private int totalHoles = 0;
	private int totalSizeOfHoles = 0;
	private int totalRowsWithHoles = 0;
	private int totalColumnsWithHoles = 0;
	private int hasLost = 0;
	private int maxWellDepth = 0;
	private int landingHeight = State.COLS;

	private void resetProperties() {
		rowsCleared = 0;
		totalHoles = 0;
		totalSizeOfHoles = 0;
		totalRowsWithHoles = 0;
		totalColumnsWithHoles = 0;
		maxWellDepth = 0;
		landingHeight = State.COLS;
	}

	private void generateNextField(State state, int[] move) {
		resetProperties();
		nextField = deepCopyField(state.getField());
		heightArray = Arrays.copyOf(state.getTop(), state.getTop().length);
		int nextPieceIndex = state.getNextPiece();
		int turn = state.getTurnNumber();

		int orientIndex = move[State.ORIENT];
		int startColumn = move[State.SLOT];

		// piece information
		int[] pTop = state.getpTop()[nextPieceIndex][orientIndex];
		int[] pBottom = state.getpBottom()[nextPieceIndex][orientIndex];
		int pWidth = state.getpWidth()[nextPieceIndex][orientIndex];
		int pHeight = state.getpHeight()[nextPieceIndex][orientIndex];

		// height if the first column makes contact
		int height = heightArray[startColumn] - pBottom[0];
		int nextColumn;
		// for each column beyond the first in the piece
		for (int col = 1; col < pWidth; col++) {
			nextColumn = startColumn + col;
			height = Math.max(height, heightArray[nextColumn] - pBottom[col]);
		}

		if (height + pHeight >= State.ROWS) {
			hasLost = 1;
			return;
		} else {
			hasLost = 0;
		}

		// for each column in the piece - fill in the appropriate blocks
		for (int col = 0; col < pWidth; col++) {
			int startRow = height + pBottom[col];
			landingHeight = Math.min(landingHeight, startRow);
			int endRow = height + pTop[col];

			// from bottom to top of brick
			for (int row = startRow; row < endRow; row++) {
				nextField[row][startColumn + col] = turn;
			}
		}

		// adjust top
		for (int col = 0; col < pWidth; col++) {
			heightArray[startColumn + col] = height + pTop[col];
		}

		// check for full rows - starting at the top
		for (int row = height + pHeight - 1; row >= height; row--) {
			// check all columns in the row
			boolean full = true;
			for (int col = 0; col < State.COLS; col++) {
				if (nextField[row][col] == 0) {
					full = false;
					break;
				}
			}

			// if the row was full - remove it and slide above stuff down
			if (full) {
				rowsCleared++;
				// for each column
				for (int col = 0; col < State.COLS; col++) {

					// slide down all bricks
					for(int r = row; r < heightArray[col]; r++) {
						nextField[r][col] = nextField[r+1][col];
					}
					// lower the top
					heightArray[col]--;
					while (heightArray[col] >= 1 && nextField[heightArray[col] - 1][col] == 0) {
						heightArray[col]--;
					}
				}
			}
		}

		generateHolesHelper();
	}

	private void generateHolesHelper() {
		// int[] is an array of [row, col]
		LinkedList<Integer> emptyPositions = new LinkedList<Integer>();
		HashSet<Integer> rowsWithHoles = new HashSet<Integer>();
		HashSet<Integer> columnsWithHoles = new HashSet<Integer>();
		int height, row, col;

		for (col = 0; col < heightArray.length; col++) {
			height = heightArray[col];

			for (row = 0; row < height - 1; row++) {
				if (nextField[row][col] == 0) {
					emptyPositions.add(row * 100 + col);
					rowsWithHoles.add(row);
					columnsWithHoles.add(col);
				}
			}
		}

		totalSizeOfHoles = emptyPositions.size();
		totalRowsWithHoles = rowsWithHoles.size();
		totalColumnsWithHoles = columnsWithHoles.size();
		totalHoles = getDistinctHoles(emptyPositions);
	}

	// Weights to use when average height is less than half
	private Weight featuresWeight1;
	// Weights to use when average height is more than half
	private Weight featuresWeight2;
	public void initWeights(Weight weights) {
		featuresWeight1 = weights;
		featuresWeight2 = weights;
	}
	public void initWeights(Weight weights1, Weight weights2) {
		featuresWeight1 = weights1;
		featuresWeight2 = weights2;
	}

	//Calculate utility
	private double getUtility(State state, int[] move) {
		generateNextField(state, move);
		double utility = 0;
		Weight featuresWeight = 2 * getAverageHeight() < State.ROWS ? featuresWeight1 : featuresWeight2;
		double[] columnWeights = featuresWeight.getColumnWeights();

		utility += featuresWeight.maxHeight() * getMaxHeight();
		utility += featuresWeight.avgHeight() * getAverageHeight();
		utility += featuresWeight.transitions() * getTransitions();
		utility += featuresWeight.holes() * getNumberOfHoles();
		utility += featuresWeight.rowsCleared() * getRowsCleared();
		utility += featuresWeight.hasLost() * hasLost;
		utility += featuresWeight.sumDiffs() * getSumDiffs();
		utility += featuresWeight.totalSizeOfHoles() * getTotalSizeOfHoles();
		utility += featuresWeight.numOfRowsWithHoles() * getNumOfRowsWithHoles();
		utility += featuresWeight.numOfWells() * getNumOfWells();
		utility += featuresWeight.sumOfWellDepths() * getSumOfWellDepths();
		utility += featuresWeight.maxHeightDiff() * getMaxHeightDiff();
		utility += featuresWeight.diffVar() * getDiffVar();
		utility += featuresWeight.maxWellDepth() * getMaxWellDepth();
		utility += featuresWeight.sumOfHoleDepths() * getSumOfHoleDepth();
		utility += featuresWeight.landingHeight() * getLandingHeight();
		utility += featuresWeight.minHeight() * getMinHeight();
		utility += featuresWeight.rowTransitions() * getRowTransitions();
		utility += featuresWeight.averageLessMin() * getAverageLessMin();
		utility += featuresWeight.changeMaxHeight() * getChangeMaxHeight();
		utility += featuresWeight.changeAverageHeight() * getChangeAverageHeight();
		utility += featuresWeight.changeNumHoles() * getChangeNumHoles();

		for (int col = 0; col < heightArray.length; col++) {
			utility += heightArray[col] * columnWeights[col];
		}

		return utility;
	}

	// Array of columns, each index corresponds to the column's height
	private int getMaxHeight() {
		return max(heightArray);
	}

	// Minimum column height
	private int getMinHeight() {
		return min(heightArray);
	}

	private double getAverageHeight() {
		return average(heightArray);
	}

	private double getAverageLessMin() {
		return getAverageHeight() - getMinHeight();
	}

	private int getChangeMaxHeight() {
		return getMaxHeight() - currentMaxHeight;
	}

	private double getChangeAverageHeight() {
		return getAverageHeight() - currentAverageHeight;
	}

	private int getChangeNumHoles() {
		return totalHoles - currentHoles;
	}

	// Array of columns - 1, each index corresponds to the difference between that column
	// and the next column
	private int[] getAbsoluteDifference() {
		int[] differenceArray = new int[State.COLS - 1];

		for (int col = 0; col < differenceArray.length; col++) {
			differenceArray[col] = Math.abs(heightArray[col] - heightArray[col + 1]);
		}

		return differenceArray;
	}

	// Number of transitions
	private int getTransitions() {
		int transitions = 0;

		for (int col = 1; col < heightArray.length; col++) {
			if (Math.abs(heightArray[col] - heightArray[col - 1]) > 0) {
				transitions++;
			}
		}
		return transitions;
	}

	private int getNumberOfHoles() {
		return totalHoles;
	}

	private int getRowsCleared() {
		return rowsCleared;
	}

	private int getSumDiffs() {
		int depth = 0;
		int sumOfDiffs = 0;
		int heightA = 0;
		int heightB = 0;

		for (int col = 0; col < State.COLS - 1; col++) {
			heightA = heightArray[col];
			heightB = heightArray[col+1];
			sumOfDiffs += Math.abs(heightA - heightB);
		}

		return sumOfDiffs;
	}

	// Number of full cells in the column above each hole
	private int getSumOfHoleDepth() {
		int total = 0;
		int multiplier;
		boolean newHole = true;

		for (int col = 0; col < heightArray.length; col++) {
			multiplier = 0;
			for (int row = 0; row < heightArray[col]; row++) {
				if (nextField[row][col] == 0) {
					if (newHole) {
						newHole = false;
						multiplier++;
					}
				} else {
					newHole = true;
					total += multiplier;
				}
			}
		}
		return total;
	}

	private int getNumOfWells() {
		int total = 0;
		for (int col = 0; col < heightArray.length; col++) {
			if (col == 0) {
				if (heightArray[col] < heightArray[col + 1]) {
					total += 1;
				}
			} else if (col == heightArray.length - 1) {
				if (heightArray[col] <  heightArray[col - 1]) {
					total += 1;
				}
			} else if (heightArray[col] <  heightArray[col - 1] && heightArray[col] < heightArray[col + 1]) {
				total += 1;
			}
		}

		return total;
	}

	private int getSumOfWellDepths() {
		int total = 0;
		int max = 0;
		int wellDepth;
		for (int col = 0; col < heightArray.length; col++) {
			if (col == 0) {
				if (heightArray[col] < heightArray[col + 1]) {
					wellDepth = heightArray[col + 1];
					total += wellDepth;
					max = Math.max(max, wellDepth);
				}
			} else if (col == heightArray.length - 1) {
				if (heightArray[col] <  heightArray[col - 1]) {
					wellDepth = heightArray[col - 1];
					total += wellDepth;
					max = Math.max(max, wellDepth);
				}
			} else if (heightArray[col] <  heightArray[col - 1] && heightArray[col] < heightArray[col + 1]) {
				wellDepth = Math.min(heightArray[col - 1], heightArray[col + 1]);
				total += wellDepth;
				max = Math.max(max, wellDepth);
			}
		}
		maxWellDepth = max;
		return total;
	}

	// Number of rows having at least one hole
	private int getNumOfRowsWithHoles() {
		return totalRowsWithHoles;
	}

	// Get the maximum depth of a well
	private int getMaxWellDepth() {
		return maxWellDepth;
	}

	private int getTotalSizeOfHoles() {
		return totalSizeOfHoles;
	}

	private int getMaxHeightDiff() {
		return max(heightArray) - min(heightArray);
	}

	// Returns the variance of the difference in the heights of columns
	private	double getDiffVar() {
		double diffVar = 0.0;
		double diffMean = 0.0;
		int[] differenceArray = getAbsoluteDifference();

		for (int column = 0; column < differenceArray.length; column++) {
			diffMean += differenceArray[column];
		}
		diffMean /= differenceArray.length;

		for (int column = 0; column < differenceArray.length; column++) {
			diffVar += Math.pow(differenceArray[column] - diffMean, 2);
		}
		return diffVar;
	}

	private double getLandingHeight() {
		return landingHeight;
	}

	private int getRowTransitions() {
		int sum = 0;

		for (int row = 0; row < max(heightArray); row++) {
			for (int column = 1; column < nextField[row].length; column++) {
				sum += (nextField[row][column] == 0 ^ nextField[row][column-1] == 0) ? 1 : 0;
			}
		}
		return sum;
	}

	private void generateCurrentStateProperties(State state) {
		int[][] currentField = state.getField();
		int[] currentHeightArray = state.getTop();

		currentAverageHeight = average(currentHeightArray);
		currentMaxHeight = max(currentHeightArray);
		currentMinHeight = min(currentHeightArray);

		// Get current number of holes
		LinkedList<Integer> emptyPositions = new LinkedList<Integer>();
		int height, row, col;

		for (col = 0; col < currentHeightArray.length; col++) {
			height = currentHeightArray[col];

			for (row = 0; row < height - 1; row++) {
				if (currentField[row][col] == 0) {
					emptyPositions.add(row*100 + col);
				}
			}
		}

		currentHoles = getDistinctHoles(emptyPositions);
	}

	private int getDistinctHoles(LinkedList<Integer> emptyPositions) {
		int holes = 0, row, col;
		ArrayDeque<Integer> queue = new ArrayDeque<Integer>();

		while (!emptyPositions.isEmpty()) {
			boolean foundAdjacentPositions = false;
			queue.addLast(emptyPositions.remove());

			while (!foundAdjacentPositions) {
				int position = queue.removeFirst();
				row = position / 100;
				col = position % 100;
				int emptyNeighbours = 0;
				int[] neighbours = {
					Math.max(row - 1, 0)              * 100 + col,
					Math.min(row + 1, State.ROWS - 1) * 100 + col
					// Ignore left and right neighbours since the game does not allow
					// slotting in pieces horizontally.
					// row                               * 100 + Math.max(col - 1, 0),
					// row                               * 100 + Math.min(col + 1, State.COLS - 1)
				};

				for (int neighbour : neighbours) {
					if (emptyPositions.contains(neighbour)) {
						emptyNeighbours += 1;
						queue.addLast(neighbour);
						emptyPositions.removeFirstOccurrence(neighbour);
					}
				}

				if (emptyNeighbours == 0) {
					foundAdjacentPositions = true;
				}
			}

			holes += 1;
		}

		return holes;
	}

	//implement this function to have a working system
	public int pickMove(State state, int[][] legalMoves) {
		int moveIndex = 0;
		Double score = getUtility(state, legalMoves[0]);

		generateCurrentStateProperties(state);

		for (int move = 1; move < legalMoves.length; move++) {
			Double newScore = getUtility(state, legalMoves[move]);
			if (newScore > score) {
				score = newScore;
				moveIndex = move;
			}
		}

		return moveIndex;
	}

	public static void main(String[] args) {
		State state = new State();
		// new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
		double[] weightsArr1 = new double[] {
		/* Max Height                */     0.319,
		/* Average Height            */    38.608,
		/* Transitions               */     8.665,
		/* Number of Holes           */     0.688,
		/* Sum of Differences        */    -2.825,
		/* Rows Cleared              */     0.740,
		/* Sum of Hole Depths        */    -1.589,
		/* Max Well Depth            */     0.146,
		/* Has Lost                  */ -1661.991,
		/* Number of Rows with Holes */    -0.983,
		/* Total Size of Holes       */   -19.004,
		/* Number of Wells           */     0.685,
		/* Sum of Well Depths        */    -1.087,
		/* Max Height Difference     */     0.367,
		/* Difference Variance       */    -1.098,
		/* Landing Height            */    -4.402,
		/* Min Height                */    -0.015,
		/* Row Transitions           */   -10.465,
		/* Average Less Mean         */     1.635,
		/* Change Max Height         */     0.043,
		/* Change Average Height     */    -5.436,
		/* Change Number of Holes    */   -31.775,
		/* Column 0                  */    -0.360,
		/* Column 1                  */    -5.737,
		/* Column 2                  */    -0.552,
		/* Column 3                  */    -5.111,
		/* Column 4                  */    -0.877,
		/* Column 5                  */    -3.750,
		/* Column 6                  */    -3.696,
		/* Column 7                  */    -6.280,
		/* Column 8                  */     0.597,
		/* Column 9                  */    -0.361
		};
		double[] weightsArr2 = new double[] {
		/* Max Height                */     0.319,
		/* Average Height            */    38.608,
		/* Transitions               */     8.665,
		/* Number of Holes           */     0.688,
		/* Sum of Differences        */    -2.825,
		/* Rows Cleared              */     0.740,
		/* Sum of Hole Depths        */    -1.589,
		/* Max Well Depth            */     0.146,
		/* Has Lost                  */ -1661.991,
		/* Number of Rows with Holes */    -0.983,
		/* Total Size of Holes       */   -19.004,
		/* Number of Wells           */     0.685,
		/* Sum of Well Depths        */    -1.087,
		/* Max Height Difference     */     0.367,
		/* Difference Variance       */    -1.098,
		/* Landing Height            */    -4.402,
		/* Min Height                */    -0.015,
		/* Row Transitions           */   -10.465,
		/* Average Less Mean         */     1.635,
		/* Change Max Height         */     0.043,
		/* Change Average Height     */    -5.436,
		/* Change Number of Holes    */   -31.775,
		/* Column 0                  */    -0.360,
		/* Column 1                  */    -5.737,
		/* Column 2                  */    -0.552,
		/* Column 3                  */    -5.111,
		/* Column 4                  */    -0.877,
		/* Column 5                  */    -3.750,
		/* Column 6                  */    -3.696,
		/* Column 7                  */    -6.280,
		/* Column 8                  */     0.597,
		/* Column 9                  */    -0.361
		};
		Weight w1 = new Weight(weightsArr1);
		Weight w2 = new Weight(weightsArr2);
		p.initWeights(w1, w2);
		while(!state.hasLost()) {
			state.makeMove(p.pickMove(state, state.legalMoves()));
			// state.draw();
			// state.drawNext(0, 0);
			// try {
			// 	Thread.sleep(10);
			// } catch (InterruptedException e) {
			// 	e.printStackTrace();
			// }
		}
		System.out.println("You have completed "+state.getRowsCleared()+" rows.");
	}

	public static int[][] deepCopyField(int[][] field) {
		if (field == null) {
				return null;
		}

		final int[][] result = new int[field.length][];
		for (int i = 0; i < field.length; i++) {
				result[i] = Arrays.copyOf(field[i], field[i].length);
		}
		return result;
	}

	public static int max(int[] arr) {
		if (arr.length <= 0) {
			return Integer.MIN_VALUE;
		}

		int max = arr[0];

		for (int i = 1; i < arr.length; i++) {
			max = Math.max(arr[i], max);
		}

		return max;
	}

	public static int min(int[] arr) {
		if (arr.length <= 0) {
			return Integer.MAX_VALUE;
		}

		int min = arr[0];

		for (int i = 1; i < arr.length; i++) {
			min = Math.min(arr[i], min);
		}

		return min;
	}

	public static double average(int[] arr) {
		int total = 0;

		for (int i = 0; i < arr.length; i++) {
			total += arr[i];
		}

		return ((double) total) / ((double) arr.length);
	}

	public static class Weight implements Comparable<Weight> {
		private final double c1 = 2;
		private final double c2 = 2;
		private final double vMax = 20;
		private int totalFeatures;
		private double[] weightsArray;

		private double[] velocity;
		private double[] pBestWeightsArray;
		private int pBestScore;
		private double[] lBestWeightsArray;
		private int lBestScore;

		private Random rand = new Random();

		int score = 0;

		public Weight(int totalFeatures) {
			this.totalFeatures = totalFeatures;
			int arrayLength = totalFeatures + State.COLS;
			weightsArray = new double[arrayLength];
			velocity = new double[arrayLength];
			for (int i = 0; i < arrayLength; i++)
				weightsArray[i] = Math.random() * 20 - 10;
			for (int i = 0; i < arrayLength; i++)
				velocity[i] = 0;
			setHasLost(-10);
		}

		public Weight(double[] weights) {
			totalFeatures = weights.length - State.COLS;
			weightsArray = weights;
			velocity = new double[weights.length];
			for (int i = 0; i < weights.length; i++)
				velocity[i] = 0;
		}

		public double[] getWeights() {
			return Arrays.copyOf(weightsArray, weightsArray.length);
		}

		public void updatePBest() {
			if (pBestWeightsArray == null || score > pBestScore) {
				pBestScore = score;
				pBestWeightsArray = Arrays.copyOf(weightsArray, weightsArray.length);
			}
		}

		public void updateLBest(int lBestScore, double[] lBestWeightsArray) {
			if (this.lBestWeightsArray == null || lBestScore > this.lBestScore) {
				this.lBestScore = lBestScore;
				this.lBestWeightsArray = lBestWeightsArray;
			}
		}

		public void updatePosition() {
			for (int i = 0; i < weightsArray.length; i++) {
				velocity[i] += c1 * Math.random() * (pBestWeightsArray[i] - weightsArray[i]) +
											 c2 * Math.random() * (lBestWeightsArray[i] - weightsArray[i]);
				if (velocity[i] > vMax) {
					velocity[i] = vMax;
				} else if (velocity[i] < -vMax) {
					velocity[i] = -vMax;
				}
				weightsArray[i] += velocity[i];
			}
		}

		public double maxHeight() {
			return weightsArray[0];
		}

		public double avgHeight() {
			return weightsArray[1];
		}

		public double transitions() {
			return weightsArray[2];
		}

		public double holes() {
			return weightsArray[3];
		}

		public double sumDiffs() {
			return weightsArray[4];
		}

		public double rowsCleared() {
			return weightsArray[5];
		}

		public double sumOfHoleDepths() {
			return weightsArray[6];
		}

		public double maxWellDepth() {
			return weightsArray[7];
		}

		public void setHasLost(double value) {
			weightsArray[8] = value;
		}

		public double hasLost() {
			return weightsArray[8];
		}

		public double numOfRowsWithHoles() {
			return weightsArray[9];
		}

		public double totalSizeOfHoles() {
			return weightsArray[10];
		}

		public double numOfWells() {
			return weightsArray[11];
		}

		public double sumOfWellDepths() {
			return weightsArray[12];
		}

		public double maxHeightDiff() {
			return weightsArray[13];
		}

		public double diffVar() {
			return weightsArray[14];
		}

		public double landingHeight() {
			return weightsArray[15];
		}

		public double minHeight() {
			return weightsArray[16];
		}

		public double rowTransitions() {
			return weightsArray[17];
		}

		public double averageLessMin() {
			return weightsArray[18];
		}

		public double changeMaxHeight() {
			return weightsArray[19];
		}

		public double changeAverageHeight() {
			return weightsArray[20];
		}

		public double changeNumHoles() {
			return weightsArray[21];
		}

		public double[] getColumnWeights() {
			return Arrays.copyOfRange(weightsArray, totalFeatures, totalFeatures + State.COLS);
		}

		public void mutate(int mutation) {
			for (int feature = 0; feature < weightsArray.length; feature++ ) {
				if (rand.nextInt(100) < mutation) {
					double percentage = 0.01 * (rand.nextInt(40) - 20);
					weightsArray[feature] = weightsArray[feature] + (percentage * weightsArray[feature]);
				}
			}
		}

		@Override
		public int compareTo(Weight s) {
			return s.score - this.score;
		}

		@Override
		public String toString() {
			String output =
				"Max Height                     " + String.format("%8.3f", weightsArray[0])  + "\n" +
				"Average Height                 " + String.format("%8.3f", weightsArray[1])  + "\n" +
				"Transitions                    " + String.format("%8.3f", weightsArray[2])  + "\n" +
				"Number of Holes                " + String.format("%8.3f", weightsArray[3])  + "\n" +
				"Sum of Differences             " + String.format("%8.3f", weightsArray[4])  + "\n" +
				"Rows Cleared                   " + String.format("%8.3f", weightsArray[5])  + "\n" +
				"Sum of Hole Depths             " + String.format("%8.3f", weightsArray[6])  + "\n" +
				"Max Well Depth                 " + String.format("%8.3f", weightsArray[7])  + "\n" +
				"Has Lost                       " + String.format("%8.3f", weightsArray[8])  + "\n" +
				"Number of Rows with Holes      " + String.format("%8.3f", weightsArray[9])  + "\n" +
				"Total Size of Holes            " + String.format("%8.3f", weightsArray[10]) + "\n" +
				"Number of Wells                " + String.format("%8.3f", weightsArray[11]) + "\n" +
				"Sum of Well Depths             " + String.format("%8.3f", weightsArray[12]) + "\n" +
				"Max Height Difference          " + String.format("%8.3f", weightsArray[13]) + "\n" +
				"Difference Variance            " + String.format("%8.3f", weightsArray[14]) + "\n" +
				"Landing Height                 " + String.format("%8.3f", weightsArray[15]) + "\n" +
				"Min Height                     " + String.format("%8.3f", weightsArray[16]) + "\n" +
				"Row Transitions                " + String.format("%8.3f", weightsArray[17]) + "\n" +
				"Average Less Min               " + String.format("%8.3f", weightsArray[18]) + "\n" +
				"Change Max Height              " + String.format("%8.3f", weightsArray[19]) + "\n" +
				"Change Average Height          " + String.format("%8.3f", weightsArray[20]) + "\n" +
				"Change Number of Holes         " + String.format("%8.3f", weightsArray[21]) + "\n";

			for (int i = 0; i < State.COLS; i++) {
				output += "Column " + i + "                       " +
					String.format("%8.3f", weightsArray[totalFeatures + i]) + "\n";
			}

			return output;
		}
	}
}
