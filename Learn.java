import java.util.concurrent.TimeUnit;

public abstract class Learn {
  protected final int totalFeatures = 22;
  protected Weight[] weightPermutations;
  protected final int attempt = 3;
  private int currentIndex = 0;

  private synchronized Weight getWeight() {
    if (currentIndex < weightPermutations.length) {
      return weightPermutations[currentIndex++];
    }
    return null;
  }

  protected void computeScores() {
    currentIndex = 0;
    int processors = Runtime.getRuntime().availableProcessors();
    WeightComputer[] threads = new WeightComputer[processors];
    for (int i = 0; i < processors; i++) {
      threads[i] = new WeightComputer(this);
      threads[i].start();
    }
    try {
      for (int i = 0; i < processors; i++) {
        threads[i].join();
      }
    } catch (Exception e) {
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

  private class TetrisState extends State {}

  public class WeightComputer extends Thread {
    Learn l;

    public WeightComputer(Learn l) {
      this.l = l;
    }

    public void run() {
      Weight w;
      TetrisState state;
      PlayerSkeleton p;

      while (true) {
        synchronized (this) {
          w = l.getWeight();
        }

        if (w == null) {
          break;
        }

        w.score = 0;
        for (int a = 0; a < l.attempt; a++) {
          state = new TetrisState();
          p = new PlayerSkeleton();
          p.initWeights(w);

          while (!state.hasLost()) {
            state.makeMove(p.pickMove(state, state.legalMoves()));
          }
          w.score += state.getRowsCleared();
        }
        w.score /= l.attempt;
      }
    }
  }
}
