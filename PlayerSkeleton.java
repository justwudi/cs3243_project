
public class PlayerSkeleton {

	// Array of 10, each index corresponds to the column's height
	private int[] getHeight(State state) {
		return int[];
	}

	// Array of 9, each index corresponds to the difference between that column
	// and the next column
	private int[] getAbsoluteDifference(State state) {
		return int[];
	}

	// Height of the tallest column
	private int getMaxColumnHeight(State state) {
		return 0;
	}

	// Self explanatory
	private int numberOfHoles(State state) {
		return 0;
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
