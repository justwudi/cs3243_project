import java.util.*;

public class PlayerSkeleton {

	private int[][] nextField;
	private int[] heightArray;
	private int rowsCleared = 0;

	private boolean generateNextField(State state, int[] move) {
		nextField = deepCopyField(state.getField());
		heightArray = Arrays.copyOf(state.getTop(), state.getTop().length);
		rowsCleared = 0;
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
			return false;
		}

		// for each column in the piece - fill in the appropriate blocks
		for(int col = 0; col < pWidth; col++) {
			int startRow = height + pBottom[col];
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

		return true;
	}

	private final String MAX_HEIGHT = "MAX_HEIGHT";
	private final String AVG_HEIGHT = "AVG_HEIGHT";
	private final String TRANSITIONS = "TRANSITIONS";
	private final String HOLES = "HOLES";
	private final String SUM_DIFFS = "SUM_DIFFS";
	private final String ROWS_CLEARED = "ROWS_CLEARED";

	private String[] features = {
		MAX_HEIGHT,
		AVG_HEIGHT,
		TRANSITIONS,
		HOLES,
		SUM_DIFFS,
		ROWS_CLEARED
	};

	private HashMap<String, Double> featuresWeight = new HashMap<String, Double>();
	private void initWeights() {
		featuresWeight.put(MAX_HEIGHT,     -2.0);
		featuresWeight.put(AVG_HEIGHT,     -1.0);
		featuresWeight.put(TRANSITIONS,    -1.0);
		featuresWeight.put(HOLES,           0.0);
		featuresWeight.put(SUM_DIFFS,       0.0);
		featuresWeight.put(ROWS_CLEARED,   10.0);
	}

	//Calculate utility
	private double getUtility(State state, int[] move) {
		if (!generateNextField(state, move)) {
			return Double.MIN_VALUE;
		}
		double utility = 0;

		utility += featuresWeight.get(MAX_HEIGHT) * getMaxHeight();
		utility += featuresWeight.get(AVG_HEIGHT) * getAverageHeight();
		utility += featuresWeight.get(TRANSITIONS) * getTransitions();
		utility += featuresWeight.get(HOLES) * getNumberOfHoles();
		utility += featuresWeight.get(ROWS_CLEARED) * getRowsCleared();

		return utility;
	}

	// Array of columns, each index corresponds to the column's height
	private int getMaxHeight() {
		int max = 0;

		for (int i = 0; i < heightArray.length; i++) {
			max = Math.max(max, heightArray[i]);
		}

		return max;
	}

	private double getAverageHeight() {
		int total = 0;

		for (int col = 0; col < heightArray.length; col++) {
			total += heightArray[col];
		}

		return ((double) total) / ((double) heightArray.length);
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

	// Height of the tallest column
	private int getMaxColumnHeight() {
		int max = 0;

		for (int col = 0; col < heightArray.length; col++) {
			int currentHeight = heightArray[col];
			if (currentHeight > max) {
				max = currentHeight;
			}
		}

		return max;
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

	// Self explanatory
	private int getNumberOfHoles() {
		return 0;
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

	private int sumOfWellsDepth(State state) {
		int depth = 0;
		int sumOfDepth = 0;
		int heightA = 0;
		int heightB = 0;

		for (int col = 0; col < State.COLS - 1; col++) {
			heightA = heightArray[col];
			heightB = heightArray[col+1];
			depth = Math.abs(heightA - heightB);

			sumOfDepth += depth;
		}

		return sumOfDepth;
	}

	// Number of full cells in the column above each hole
	private int holeDepth(State state, int row, int col) {
		int height = heightArray[col];
		int cellAboveHole = row + 1;
		int holeDepth = 0;

		while (cellAboveHole < height) {
			if (nextField[cellAboveHole][col] != 0) {
				holeDepth++;
			}
		}

		return holeDepth;
	}

	//implement this function to have a working system
	public int pickMove(State state, int[][] legalMoves) {
		int moveIndex = 0;
		Double score = getUtility(state, legalMoves[0]);

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
		new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
		p.initWeights();
		while(!state.hasLost()) {
			state.makeMove(p.pickMove(state, state.legalMoves()));
			state.draw();
			state.drawNext(0, 0);
			try {
				Thread.sleep(300);
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
}
