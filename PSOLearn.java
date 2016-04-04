import java.util.*;

public class PSOLearn extends Learn {
  private final int totalLocalGroups = 10;
  private Weight[][] localGroups = new Weight[totalLocalGroups][];
  private int gBestScore;
  private double[] gBestWeightsArray;

  private void generateRandomWeights(int numPermutations) {
    weightPermutations = new Weight[numPermutations];
    for (int i = 0; i < numPermutations; i++) {
      weightPermutations[i] = new Weight(totalFeatures);
    }

    int groupSize = numPermutations / totalLocalGroups;

    for (int groupNo = 0; groupNo < totalLocalGroups; groupNo++) {
      int startIndex = groupNo * groupSize;
      int endIndex;
      if (groupNo == totalLocalGroups - 1) {
        endIndex = Math.max(startIndex + groupSize, numPermutations);
      } else {
        endIndex = startIndex + groupSize;
      }
      localGroups[groupNo] = new Weight[endIndex - startIndex];

      int index = 0;
      for (int i = startIndex; i < endIndex; i++) {
        localGroups[groupNo][index] = weightPermutations[i];
        index++;
      }
    }
  }

  private void updateParticles() {
    for (int i = 0; i < weightPermutations.length; i++) {
      weightPermutations[i].updatePosition();
    }
  }

  private void updateLBest() {
    for (int groupNo = 0; groupNo < 10; groupNo++) {
      Weight[] group = localGroups[groupNo];
      Arrays.sort(group);
      Weight lBest = group[0];
      int lBestScore = lBest.score;
      double[] lBestWeightsArray = Arrays.copyOf(lBest.getWeights(), totalFeatures + State.COLS);

      for (int i = 0; i < group.length; i++) {
        group[i].updateLBest(lBestScore, lBestWeightsArray);
      }
    }
  }

  public static void main(String[] args) {
    PSOLearn pso = new PSOLearn();
    pso.generateRandomWeights(100);
    int totalIteration = 1000;
    for (int iteration = 0; iteration < totalIteration; iteration++) {
      pso.computeScores();
      for (int i = 0; i < pso.weightPermutations.length; i++) {
        pso.weightPermutations[i].updatePBest();
      }
      Arrays.sort(pso.weightPermutations);
      pso.updateLBest();
      pso.updateParticles();
      System.out.println("Max "+pso.weightPermutations[0].score+" rows.");
      printArray(pso.weightPermutations[0].getWeights());
    }
  }
}
