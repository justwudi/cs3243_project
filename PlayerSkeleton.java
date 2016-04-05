import java.util.*;

public class PlayerSkeleton {

	private final int COLS_SQ = State.COLS * State.COLS;
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

	private final String MAX_HEIGHT      = "MAX_HEIGHT";
	private final String AVG_HEIGHT      = "AVG_HEIGHT";
	private final String TRANSITIONS     = "TRANSITIONS";
	private final String HOLES           = "HOLES";
	private final String SUM_DIFFS       = "SUM_DIFFS";
	private final String ROWS_CLEARED    = "ROWS_CLEARED";
	private final String ROWS_WITH_HOLES = "ROWS_WITH_HOLES";
	private final String MAX_WELL_DEPTH  = "MAX_WELL_DEPTH";
	private final String HAS_LOST        = "HAS_LOST";

	private String[] features = {
		MAX_HEIGHT,
		AVG_HEIGHT,
		TRANSITIONS,
		HOLES,
		SUM_DIFFS,
		ROWS_CLEARED,
		ROWS_WITH_HOLES,
		MAX_WELL_DEPTH,
		HAS_LOST
	};


	private static Weight featuresWeight;
	public void initWeights(Weight weights) {
		featuresWeight = weights;
	}

	//Calculate utility
	private double getUtility(State state, int[] move) {
		generateNextField(state, move);
		double utility = 0;
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
		utility += featuresWeight.diffVar() * getDiffVar() / COLS_SQ;
		utility += featuresWeight.heightWeightedCells() * getHeightWeightedCells() / SIZE;
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

	private int numberOfOccupiedCell(State state) {
		int[][] field = state.getField();
		int fullCells = 0;

		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				if (field[row][col] != 0) {
					fullCells++;
				}
			}
		}

		return fullCells;
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

	// Get the height weighted cells
	private double getHeightWeightedCells() {
		double weightedCells = 0.0;
		for (int row = 0; row < max(heightArray); row++) {
			for (int column = 0; column < nextField[row].length; column++) {
				weightedCells += nextField[row][column] != 0 ? (row + 1) : 0;
			}
		}
		return weightedCells;
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
		//new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
		p.initWeights(featuresWeight);
		while(!state.hasLost()) {
			state.makeMove(p.pickMove(state, state.legalMoves()));
			// state.draw();
			// state.drawNext(0, 0);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
}
