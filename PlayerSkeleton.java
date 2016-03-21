
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

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
