public abstract class Learn {
  protected final int totalFeatures = 16;
  protected Weight[] weightPermutations;

  protected void computeScores() {
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

  protected static void printArray(double[] anArray) {
    for (int i = 0; i < anArray.length; i++) {
      if (i > 0) {
        System.out.print(", ");
      }
      System.out.print(String.format("%.2f", anArray[i]));
    }
    System.out.println();
  }
}