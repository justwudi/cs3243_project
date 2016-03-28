import java.util.*

public class Weight {
  private int totalFeatures = 9;
  private double[] weightsArray;

  public Weight() {
    weightsArray = new double[totalFeatures];
    for (int i = 0; i < totalFeatures; i++)
      weightsArray[i] = Math.random() * 2 - 1;
  }

  public Weight(double[] weights) {
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
}