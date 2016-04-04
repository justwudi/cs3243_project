import java.util.*;

public class Weight implements Comparable<Weight> {
  private final double c1 = 2;
  private final double c2 = 2;
  private final double vMax = 20;
  private int totalFeatures;
  private double[] weightsArray;

  private double[] velocity;
  private double[] pBestWeightsArray;
  private int pBestScore;
  private double[] lBestWeightsArray;
  private int lBestScore;

  int score = 0;

  public Weight(int totalFeatures) {
    this.totalFeatures = totalFeatures;
    int arrayLength = totalFeatures + State.COLS;
    weightsArray = new double[arrayLength];
    velocity = new double[arrayLength];
    for (int i = 0; i < arrayLength; i++)
      weightsArray[i] = Math.random() * 20 - 10;
    for (int i = 0; i < arrayLength; i++)
      velocity[i] = 0;
    setHasLost(-10);
  }

  public Weight(double[] weights) {
    totalFeatures = weights.length - State.COLS;
    weightsArray = weights;
    velocity = new double[weights.length];
    for (int i = 0; i < weights.length; i++)
      velocity[i] = 0;
    setHasLost(-10);
  }

  public double[] getWeights() {
    return Arrays.copyOf(weightsArray, weightsArray.length);
  }

  public void updatePBest() {
    if (pBestWeightsArray == null || score > pBestScore) {
      pBestScore = score;
      pBestWeightsArray = Arrays.copyOf(weightsArray, weightsArray.length);
    }
  }

  public void updateLBest(int lBestScore, double[] lBestWeightsArray) {
    if (this.lBestWeightsArray == null || lBestScore > this.lBestScore) {
      this.lBestScore = lBestScore;
      this.lBestWeightsArray = lBestWeightsArray;
    }
  }

  public void updatePosition() {
    for (int i = 0; i < weightsArray.length; i++) {
      velocity[i] += c1 * Math.random() * (pBestWeightsArray[i] - weightsArray[i]) +
                     c2 * Math.random() * (lBestWeightsArray[i] - weightsArray[i]);
      if (velocity[i] > vMax) {
        velocity[i] = vMax;
      } else if (velocity[i] < -vMax) {
        velocity[i] = -vMax;
      }
      weightsArray[i] += velocity[i];
    }
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

  public double sumOfHoleDepths() {
    return weightsArray[6];
  }

  public double maxWellDepth() {
    return weightsArray[7];
  }

  public void setHasLost(double value) {
    weightsArray[8] = value;
  }

  public double hasLost() {
    return weightsArray[8];
  }

  public double numOfRowsWithHoles() {
    return weightsArray[9];
  }

  public double totalSizeOfHoles() {
    return weightsArray[10];
  }

  public double numOfWells() {
    return weightsArray[11];
  }

  public double sumOfWellDepths() {
    return weightsArray[12];
  }

  public double maxHeightDiff() {
    return weightsArray[13];
  }

  public double diffVar() {
    return weightsArray[14];
  }

  public double heightWeightedCells() {
    return weightsArray[15];
  }

  public double[] getColumnWeights() {
    return Arrays.copyOfRange(weightsArray, totalFeatures, totalFeatures + State.COLS);
  }

  public void mutate() {
    int feature = (int) Math.random() * totalFeatures;
    double percentage = Math.random() * 0.4 - 0.2;
    weightsArray[feature] = weightsArray[feature] + (percentage * weightsArray[feature]);
  }

  @Override
  public int compareTo(Weight s) {
    return s.score - this.score;
  }

  @Override
  public String toString() {
    String output =
      "Max Height                     " + String.format("%8.3f", weightsArray[0])  + "\n" +
      "Average Height                 " + String.format("%8.3f", weightsArray[1])  + "\n" +
      "Transitions                    " + String.format("%8.3f", weightsArray[2])  + "\n" +
      "Number of Holes                " + String.format("%8.3f", weightsArray[3])  + "\n" +
      "Sum of Differences             " + String.format("%8.3f", weightsArray[4])  + "\n" +
      "Rows Cleared                   " + String.format("%8.3f", weightsArray[5])  + "\n" +
      "Sum of Hole Depths             " + String.format("%8.3f", weightsArray[6])  + "\n" +
      "Max Well Depth                 " + String.format("%8.3f", weightsArray[7])  + "\n" +
      "Has Lost                       " + String.format("%8.3f", weightsArray[8])  + "\n" +
      "Number of Rows with Holes      " + String.format("%8.3f", weightsArray[9])  + "\n" +
      "Total Size of Holes            " + String.format("%8.3f", weightsArray[10]) + "\n" +
      "Number of Wells                " + String.format("%8.3f", weightsArray[11]) + "\n" +
      "Sum of Well Depths             " + String.format("%8.3f", weightsArray[12]) + "\n" +
      "Max Height Difference          " + String.format("%8.3f", weightsArray[13]) + "\n" +
      "Difference Variance            " + String.format("%8.3f", weightsArray[14]) + "\n" +
      "Height Weighted Cells          " + String.format("%8.3f", weightsArray[15]) + "\n";

    for (int i = 0; i < State.COLS; i++) {
      output += "Column " + i + "                       " + String.format("%8.3f", weightsArray[totalFeatures + i]) + "\n";
    }

    return output;
  }
}