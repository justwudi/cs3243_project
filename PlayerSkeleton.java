import java.util.*;

public class PlayerSkeleton {

	// Array of 10, each index corresponds to the column's height
	private int[] getHeight(State state) {
		return state.getTop();
	}

	// Array of 9, each index corresponds to the difference between that column
	// and the next column
	private int[] getAbsoluteDifference(State state) {
		int[] differenceArray = new int[state.COLS - 1];

		int[] heightArray = getHeight(state);

		for (int col = 0; col < differenceArray.length; col++) {
			differenceArray[col] = Math.abs(heightArray[col] - heightArray[col + 1]);
		}

		return differenceArray;
	}

	// Height of the tallest column
	private int getMaxColumnHeight(State state) {
		int max = 0;
		int[] heightArray = getHeight(state);

		for (int col = 0; col < heightArray.length; col++) {
			int currentHeight = heightArray[col];
			if (currentHeight > max) {
				max = currentHeight;
			}
		}

		return max;
	}

	// Self explanatory
	private int numberOfHoles(State state) {
		int[] heightArray = getHeight(state);
		int[][] field = state.getField();
		int col;
		int row;

		return 0;
	}

	private int numberOfOccupiedCell(State state) {
		int[][] field = state.getField();
		int maxHeight = getMaxColumnHeight(state);
		int fullCells = 0;
		
		for (int row = 0; row < maxHeight; row++) {
			for (int col = 0; col < State.COLS; col++) {
				if (field[row][col] != 0) {
					fullCells++;
				}
			}
		}
		
		return fullCells;
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

}
