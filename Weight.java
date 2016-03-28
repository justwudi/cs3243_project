import java.util.*;

public class Weight implements Comparable<Weight> {
  private int totalFeatures;
  private double[] weightsArray;

  int score = 0;

  public Weight(int totalFeatures) {
    this.totalFeatures = totalFeatures;
    weightsArray = new double[totalFeatures];
    for (int i = 0; i < totalFeatures; i++)
      weightsArray[i] = Math.random() * 2 - 1;
  }

  public Weight(double[] weights) {
    totalFeatures = weights.length;
    weightsArray = weights;
  }

  public double[] getWeights() {
    return Arrays.copyOf(weightsArray, totalFeatures);
  }

  public double maxHeight() {
    return weightsArray[0];
  }

  public double avgHeight() {
    return weightsArray[1];
  }

  public double transitions() {
    return weightsArray[2];
  }

  public double holes() {
    return weightsArray[3];
  }

  public double sumDiffs() {
    return weightsArray[4];
  }

  public double rowsCleared() {
    return weightsArray[5];
  }

  public double rowsWithHoles() {
    return weightsArray[6];
  }

  public double maxWellDepth() {
    return weightsArray[7];
  }

  public double hasLost() {
    return weightsArray[8];
  }

  @Override
  public int compareTo(Weight s) {
    return s.score - this.score;
  }
}