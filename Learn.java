import java.util.concurrent.TimeUnit;

public abstract class Learn {
  protected final int totalFeatures = 22;
  protected Weight[] weightPermutations;
  private final int attempt = 3;

  protected void computeScores() {
    for (int i = 0; i < weightPermutations.length; i++) {
      Weight w = weightPermutations[i];
      w.score = 0;
      for (int a = 0; a < attempt; a++) {
        State state = new State();
        PlayerSkeleton p = new PlayerSkeleton();
        p.initWeights(w);
        while (!state.hasLost()) {
          state.makeMove(p.pickMove(state, state.legalMoves()));
        }
        w.score += state.getRowsCleared();
      }
      w.score /= attempt;
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

  protected static void printTimeMillis(long millis) {
    System.out.println(String.format("elapsed time %02d min, %02d sec\n",
      TimeUnit.MILLISECONDS.toMinutes(millis),
      TimeUnit.MILLISECONDS.toSeconds(millis) -
      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    ));
  }
}