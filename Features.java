public class Features{

	//Does not include the helper functions, just a list of features and their
	//implementations, refer to PlayerSkeleton.java
	public static void main(String[] args) {
		

		//Height Features
		private final String MAX_HEIGHT      = "MAX_HEIGHT";
		private final String AVG_HEIGHT      = "AVG_HEIGHT";
		private final String LANDING_HEIGHT	 = "LANDING_HEIGHT";
		private final String C1				 = "C1_HEIGHT";
		private final String C2				 = "C2_HEIGHT";
		private final String C3				 = "C3_HEIGHT";
		private final String C4				 = "C4_HEIGHT";
		private final String C5				 = "C5_HEIGHT";
		private final String C6				 = "C6_HEIGHT";
		private final String C7				 = "C7_HEIGHT";
		private final String C8				 = "C8_HEIGHT";
		private final String C9				 = "C9_HEIGHT";
		private final String C10			 = "C10_HEIGHT";


		//Smoothness Features
		private final String TRANSITIONS     = "TRANSITIONS";
		private final String SUM_DIFFS 		 = "SUM_DIFFS";
		private final String DIFF_VAR		 = "VARIANCE_OF_HT_DIFFS";
		private final String MAX_HEIGHT_DIFF = "MAX_HEIGHT_DIFF";
		private final String ROW_TRANSITIONS = "ROW_TRANSITIONS";

		//Hole Features
		private final String HOLES           = "HOLES";
		private final String ROWS_WITH_HOLES = "ROWS_WITH_HOLES";
		private	final String SUM_HOLE_DEPTH	 = "SUM_HOLE_DEPTH";


		//Well Features
		private final String MAX_WELL_DEPTH  = "MAX_WELL_DEPTH";
		private final String SUM_WELL_DEPTH  = "SUM_WELL_DEPTH";


		//Scoring Features
		private final String HAS_LOST        = "HAS_LOST";
		private final String ROWS_CLEARED    = "ROWS_CLEARED";
		private final String ERODED_CELLS	 = "ERODED_CELLS";


		//Other Features
		private final String H_WEIGHT_CELLS  = "HEIGHT_WEIGHTED_CELLS";
		private final String OCCUPIED_CELLS	 = "OCCUPIED_CELLS";

		
		//Summarized Optimal Feature Weights
		(HOLES, -190)
		(LANDING_HEIGHT, -51)
		(TRANSITIONS, -46)
		(AVG_HEIGHT, 6)
		(ROW_TRANSITIONS, -38)
		(SUM_WELL_DEPTH, -73)
		(MAX_HEIGHT_DIFF, -60)
		(CHANGE_MAX_HEIGHT, -50)
		(CHANGE_HOLES, -25)
		(CHANGE_MEAN_HEIGHT, -17)
		(MEAN_LESS_MIN, -60)
		(MIN_HEIGHT, -41)
		(SUM_DIFFS, 19)



		//Some Implementations of features


		//Minimum column height
		private int getMinHeight() {
			return min(heightArray);
		}

		private double getMeanLessMin() {
			return getAverageHeight() - getMinHeight();
		}

		private int getRowTransitions() {

			int sum = 0;

			for (int row = 0; row < max(heightArray); row++) {
				for (int column = 1; column < nextField[row].length; column++) {
					sum += ((nextField[row][column] != 0 && nextField[row][column-1] == 0) ||
							(nextField[row][column] == 0 && nextField[row][column-1] != 0)) ?
							1 : 0;
				}
			}
			return sum;
		}

		private int[] getOldHeightArr() {
			int[][] field = state.getField();
			int[] oldHeightArr = new int[field[0].length];

			for (int column = 0; column < field[0].length; column++) {
				int row = field.length - 1;
				while (row >= 0) {
					if (field[row][column] != 0) {
						oldHeightArr[column] = row;
						break;
					}
					if (row == 0) {
						oldHeightArr[column] = row;
					}
				}
			}

			return oldHeightArr;
		}

		private int getChangeMaxHeight() {
			return getMaxHeight() - max(getOldHeightArr);
		}

		private double getOldAverageHeight() {
			int total = 0;
			int[] array = getOldHeightArr();

			for (int col = 0; col < array.length; col++) {
				total += array[col];
			}

			return ((double) total) / ((double) array.length);
		}

		private double getChangeMeanHeight() {
			return getAverageHeight() - getOldAverageHeight();
		}

		private void generateOldHolesHelper() {
			// int[] is an array of [row, col]
			LinkedList<int[]> emptyPositions = new LinkedList<int[]>();
			ArrayDeque<int[]> queue = new ArrayDeque<int[]>();
			HashSet<Integer> rowsWithHoles = new HashSet<Integer>();
			HashSet<Integer> columnsWithHoles = new HashSet<Integer>();
			int height, row, col;
			int[] oldHeightArr = getOldHeightArr();
			int[][] field = state.getField();

			for (col = 0; col < oldHeightArray.length; col++) {
				height = oldHeightArray[col];

				for (row = 0; row < height - 1; row++) {
					if (field[row][col] == 0) {
						emptyPositions.add(new int[] {row, col});
						rowsWithHoles.add(row);
						columnsWithHoles.add(col);
					}
				}
			}

			totalSizeOfHoles = emptyPositions.size();
			totalRowsWithHoles = rowsWithHoles.size();
			totalColumnsWithHoles = columnsWithHoles.size();

			while (!emptyPositions.isEmpty()) {
				boolean foundAdjacentPositions = false;
				queue.addLast(emptyPositions.remove());

				while (!foundAdjacentPositions) {
					int[] position = queue.removeFirst();
					row = position[0];
					col = position[1];
					int emptyNeighbours = 0;
					int[][] neighbours = {
						{row - 1, col},
						{row + 1, col},
						{row, col - 1},
						{row, col + 1}
					};

					for (int[] neighbour : neighbours) {
						if (emptyPositions.contains(neighbour)) {
							emptyNeighbours += 1;
							queue.addLast(neighbour);
							emptyPositions.remove(neighbour);
						}
					}

					if (emptyNeighbours == 0) {
						foundAdjacentPositions = true;
					}
				}

				oldTotalHoles += 1;
			}
		}

		private int getChangeNumHoles() {
			return oldTotalHoles;
		}


		//Older Stuff from 28/3/2016

		//Difference between the tallest and shortest column
		private int getMaxHeightDiff() {
			return max(heightArray) - min(heightArray);
		}

		//Returns the variance of the difference in the heights of columns
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

		//Get the height weighted cells
		private double getHeightWeightedCells() {
			double weightedCells = 0.0;
			int[][] field = state.getField();

			for (int row = 0; row < field.length; row++) {
				for (int column = 0; column < field[row].length; column++) {
					weightedCells += field[row][column] != 0 ? (row + 1) : 0;
				}

			}
			return weightedCells;
		}
	}
}