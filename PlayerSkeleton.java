import java.util.*;

public class PlayerSkeleton {

	private int[][] nextField;
	private int[] heightArray;

	private void generateNextField(State state, int[] move) {
		nextField = deepCopyField(state.getField());
		heightArray = state.getTop();
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

		int rowsCleared = 0;

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
	}

	private	static final int NUM_F = 4;
	
	//F_WEIGHT = 0, F_VAL = 1
	//Initialise F_VAL to 0
	
	private String[] featureNames = {
		"MAX_HEIGHT",
		"AVERAGE_HEIGHT",
		"SUM_DIFFS",
		"TRANSITIONS",
		"HOLES"
	};

	private double[][] features = {
		{-3, 0},
		{-1, 0},
		{-1.5, 0},
		{-2, 0},
		{-0.75, 0}
	};

	//Calculate utility 
	private double getUtility(State state, int[] move) {
		generateNextField(state, move);
		double utility = 0;
		
		features[0][0] = 0;
		for (int i = 0; i < heightArray.length; i++) {
			features[0][0] = Math.max(features[0][0], heightArray[i]);
		}
		
		features[0][1] = 0;
		for (int i = 0; i < heightArray.length; i++) {
			features[0][1] += heightArray[i];
		}
		features[0][1] = features[0][1] / heightArray.length;

		features[0][2] = 0;
		int[] diffArray = getAbsoluteDifference();
		for (int i = 0; i < diffArray.length; i++) {
			features[0][2] += diffArray[i];
		}
		
		features[0][3] = getTransitions(state);

		//features[0][4] = ;

		for (int i = 0; i < features.length; i++) {
			utility += features[i][0] * features[i][1];
		}

		return utility;
	}

	// Array of columns, each index corresponds to the column's height
	private int[] getHeight() {
		return heightArray;
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
	private int getTransitions(State state) {
		int transitions = 0;

		for (int col = 1; col < heightArray.length; col++) {
			if (Math.abs(heightArray[col] - heightArray[col - 1]) > 0) {
				transitions++;
			}
		}
		return transitions;
	}

	// Self explanatory
	private int numberOfHoles() {
		return 0;
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
		return 0;
	}

	public static void main(String[] args) {
		State state = new State();
		new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
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
