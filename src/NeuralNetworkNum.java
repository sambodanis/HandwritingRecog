/**
 * This class is a neural network classifier.
 * It is able to distinguish between an indefinite number
 * of object classes by using one vs all classification
 */


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class NeuralNetworkNum {

    // A larger hidden layer enables the network to model more complex relationships.
    // This data isn't particularly complicated though so 25 should suffice.
    private static final int hiddenLayerSize = 25;
    // This is used in calculating initial gaussian noise in theta matrices.
    private static final double epsilonInit = 0.12;
    // This increases/decreases regularisation
    private static final double lambda = 1;
    // This effects how quickly the network is trained.
    // It is basically the step size across the error surface during an iteration of gradient descent.
    // A larger value will train faster but can get stuck zigzagging back and forth across the sides
    // of a minimum and not converging.
    private static final double alpha = 0.3;

    // Creates a neural network object, on construction it is trained for
    // a specified number of iterations. Two debug matrices can be submitted as arguments
    // to check that the computation at each step is correct, note EXTREMELY TIME CONSUMING.
    // A single debug session doing this took almost 2 hours although it solved the problem.
    public NeuralNetworkNum(Matrix trainingData, Matrix trainingLabels, int maxIters, Matrix debugTheta1, Matrix debugTheta2) {
        inputLayerSize = trainingData.getColumns();
        this.maxIters = maxIters;
        rGen = new Random();
        numLabels = determineLabels(trainingLabels);
        if (debugTheta1 != null && debugTheta2 != null) {
            Theta1 = debugTheta1;
            Theta2 = debugTheta2;
        } else {
            Theta1 = randInitializeWeights(inputLayerSize, hiddenLayerSize);
            Theta2 = randInitializeWeights(hiddenLayerSize, numLabels);
        }
        Theta1Gradient = new Matrix(new double[Theta1.getRows()][Theta1.getColumns()]);
        Theta2Gradient = new Matrix(new double[Theta2.getRows()][Theta2.getColumns()]);
        Xdata = trainingData;
        Ydata = trainingLabels;
        feedForwardMap = new HashMap<String, Matrix>();
        binaryLabels = labelsToBinary();
        train();
    }

    // Iterates through the training label matrix to determine how many unique values there
    // are and thus how many labels there are.
    private int determineLabels(Matrix trainingLabels) {
        ArrayList<Double> differentLabels = new ArrayList<Double>();
        for (int i = 0; i < trainingLabels.getRows(); i++) {
            for (int j = 0; j < trainingLabels.getColumns(); j++) {
                if (!differentLabels.contains(trainingLabels.objectAtPoint(i, j))) {
                    differentLabels.add(trainingLabels.objectAtPoint(i, j));
                }
            }
        }
        return differentLabels.size();
    }

    // Runs gradient descent and then calculates accuracy.
    private void train() {
        gradientDescent();
        predictBackend(Xdata, true);
    }

    // Runs gradient descent.
    // A guess is ade by computing a feed forward using the theta matrices.
    // The theta matrix weights are then nudged in the correct direction by
    // iteratively computing gradients of every element and taking a small step in
    // the direction which minimises the error.
    private void gradientDescent() {
        int iteration = 0;
        double currentCost = computeCost();
        while (iteration < maxIters) {
            recomputeGradientsMatrices();
            currentCost = computeCost();
            Theta1 = Theta1.matrixAddition(Theta1Gradient.scalarMultiplication(-1 * alpha));
            Theta2 = Theta2.matrixAddition(Theta2Gradient.scalarMultiplication(-1 * alpha));
            iteration++;
            System.out.println("Iteration    " + iteration + " | Cost: " + currentCost);
        }
    }

    // Given a list of points, performs all pre-processing on the points.
    // Then runs the points through the network to compute a guess of their symbol.
    public int predict(ArrayList<Point> pointsForDrawing) {
        numberExtractor scaler = new numberExtractor();
        ArrayList<Point> scaledPoints = scaler.scalePoints(pointsForDrawing);
        Point[] boundingBox = scaler.boundingBoxForSingleShape();
        Matrix pointsMatrix = new Matrix(null);
        pointsMatrix.makeMatrixFromPointsArrayList(scaledPoints, boundingBox);
        pointsMatrix.emulateWriting();
        pointsMatrix = pointsMatrix.scaleDown();
        return predictBackend(pointsMatrix.matrixToRowVector(), false);
    }

    // Predicts the value of a matrix by running it though the network.
    // This outputs a probability distribution over all possible labels the
    // data could have. The highest probability label is then chosen.
    // To determine accuracy, check how many guessed labels are the same
    // as the corresponding label in the input label matrix.
    private int predictBackend(Matrix dataToPredict, boolean displayAccuracy) {
        ArrayList<Double> result = new ArrayList<Double>();
        dataToPredict = dataToPredict.prependColumnOfValue(1);
        Matrix h1 = sigmoidFunction(dataToPredict.matrixMultiplication(Theta1.transpose()));
        h1 = h1.prependColumnOfValue(1);
        Matrix h2 = sigmoidFunction(h1.matrixMultiplication(Theta2.transpose()));
        int numCorrect = 0;
        for (int i = 0; i < h2.getRows(); i++) {
            double rowMax = 0;
            int maxIndex = 0;
            for (int j = 0; j < h2.getColumns(); j++) {
                if (h2.objectAtPoint(i, j) > rowMax) {
                    rowMax = h2.objectAtPoint(i, j);
                    maxIndex = j;
                }
            }
//            maxIndex += 1;  Only required for 1 indexed mnist data
            result.add((double) maxIndex);
            if (Ydata.objectAtPoint(i, 0) == (double)maxIndex) numCorrect++;
        }
        if (displayAccuracy) printSimp("Accuracy: " + (((double)numCorrect / Ydata.getRows()) * 100.0));
        int res = (int)(result.get(0) + .5);
        return res == 10 ? 0 : res;
    }

    // Computes a guess of which label a set of points belong to.
    // Measures how far off a guess is
    // This code is all vectorised to take advantage of optimised
    // linear algebra libraries. With my library it makes no difference
    // apart from succinctness.
    // Computes: J = sum(1/m * sum(-newY.*log(a3) - (1 - newY).*log(1-a3)))
    private double computeCost() {
        double cost = 0;
        double m = Xdata.getRows();
        feedForward();
        Matrix a3 = feedForwardMap.get("a3");
        Matrix negativeY = binaryLabels.scalarMultiplication(-1);
        Matrix negYLogA3 = negativeY.hadamardProduct(a3.elementwiseLog());
        Matrix oneMinusY = binaryLabels.nMinusMatrix(1);
        Matrix oneMinusA3 = a3.nMinusMatrix(1);
        Matrix oneMinYLogA3 = oneMinusY.hadamardProduct(oneMinusA3.elementwiseLog());
        Matrix firstSumInternal = negYLogA3.matrixAddition(oneMinYLogA3.scalarMultiplication(-1));
        Matrix firstSum = firstSumInternal.sigmaSumColumnsToRowVector();
        Matrix secondSumInternal = firstSum.scalarMultiplication(1/m);
        for (int j = 0; j < secondSumInternal.getColumns(); j++) {
            cost += secondSumInternal.objectAtPoint(0, j);
        }
        cost = regularizeCost(cost);
        return cost;
    }

    // Computes a guess of which label a set of points belong to.
    // Columns of 1 are prepended to take into account bias.
    private void feedForward() {
        Matrix a1 = Xdata.prependColumnOfValue(1);
        Matrix z2 = a1.matrixMultiplication(Theta1.transpose());
        Matrix a2 = sigmoidFunction(z2);
        a2 = a2.prependColumnOfValue(1);
        Matrix z3 = a2.matrixMultiplication(Theta2.transpose());
        Matrix a3 = sigmoidFunction(z3);
        feedForwardMap.put("a1", a1);
        feedForwardMap.put("z2", z2);
        feedForwardMap.put("a2", a2);
        feedForwardMap.put("z3", z3);
        feedForwardMap.put("a3", a3);
    }

    // Adds regularisation to suppress the impact of too many higher order terms.
    // ie smooths out a 7th order polynomial fitted to 8 points.
    // This enables the network to generalise better.
    // Computes: J = J + (lambda / (2 * m)) * sum(sum(sum(Theta1(:, 2:end).^2)) +
    //                                            sum(sum(Theta2(:, 2:end).^2)))
    private double regularizeCost(double cost) {
        double theta1Sum = 0;
        double theta2Sum = 0;
        double m = Xdata.getRows();
        Matrix theta1Squared = Theta1.hadamardProduct(Theta1);
        Matrix theta2Squared = Theta2.hadamardProduct(Theta2);
        Matrix sumTheta1Squared = theta1Squared.sigmaSumColumnsToRowVector();
        Matrix sumTheta2Squared = theta2Squared.sigmaSumColumnsToRowVector();
        for (int j = 0; j < sumTheta1Squared.getColumns(); j++) {
            theta1Sum += sumTheta1Squared.objectAtPoint(0, j);
        }
        for (int j = 0; j < sumTheta2Squared.getColumns(); j++) {
            theta2Sum += sumTheta2Squared.objectAtPoint(0, j);
        }
        double result = theta1Sum + theta2Sum;
        result = result * (lambda / (2 * m));
        return cost + result;
    }

    // Adds regularisation to gradient matrices.
    private void regulariseGradients() {
        Matrix theta1Grad1stCol = Theta1Gradient.extractColumn(0);
        Matrix theta1Grad2toN = Theta1Gradient.cutColumnN(0);
        Matrix regularizeTheta1 = Theta1.cutColumnN(0).scalarMultiplication(lambda / Xdata.getColumns());
        theta1Grad2toN = theta1Grad2toN.matrixAddition(regularizeTheta1);
        Theta1Gradient = theta1Grad1stCol.stickToMatrix(theta1Grad2toN);

        Matrix theta2Grad1stCol = Theta2Gradient.extractColumn(0);
        Matrix theta2Grad2toN = Theta2Gradient.cutColumnN(0);
        Matrix regularizeTheta2 = Theta2.cutColumnN(0).scalarMultiplication(lambda / Xdata.getColumns());
        theta2Grad2toN = theta2Grad2toN.matrixAddition(regularizeTheta2);
        Theta2Gradient = theta2Grad1stCol.stickToMatrix(theta2Grad2toN);
    }

    // Computes the derivative of elements by using back-propagation.
    // Once again, code is vectorised to take advantage of improving
    // the linear algebra library.
    private void recomputeGradientsMatrices() {
        feedForward();
        Matrix delta3 = feedForwardMap.get("a3").matrixAddition(binaryLabels.scalarMultiplication(-1));
        Matrix delta3Temp = delta3.matrixMultiplication(Theta2);
        Matrix delta3NoFirstColumn = delta3Temp.cutColumnN(0);
        Matrix derivativeOfz2 = sigmoidGradient(feedForwardMap.get("z2"));
        Matrix delta2 = delta3NoFirstColumn.hadamardProduct(derivativeOfz2);
        delta2 = delta2.transpose();
        Matrix delta2TtimesA1 = delta2.matrixMultiplication(feedForwardMap.get("a1"));
        Matrix sumTheta1GradDelta2A1 = Theta1Gradient.matrixAddition(delta2TtimesA1);
        Theta1Gradient = sumTheta1GradDelta2A1.scalarMultiplication(1.0 / Xdata.getRows());
        delta3 = delta3.transpose();
        Matrix delta3TtimesA2 = delta3.matrixMultiplication(feedForwardMap.get("a2"));
        Matrix sumTheta2GradDelta3A2 = Theta2Gradient.matrixAddition(delta3TtimesA2);
        Theta2Gradient = sumTheta2GradDelta3A2.scalarMultiplication(1.0/Xdata.getRows());
        regulariseGradients();
    }

    // Returns the numerical result of the derivative of the sigmoid function.
    // result = g = sigmoid(z) .* (1 - sigmoid(z))
    private Matrix sigmoidGradient(Matrix mat) {
        return sigmoidFunction(mat).hadamardProduct(sigmoidFunction(mat).nMinusMatrix(1));
    }

    // Performs the sigmoid function on every element of a matrix.
    private Matrix sigmoidFunction(Matrix mat) {
        Matrix result = new Matrix(new double[mat.getRows()][mat.getColumns()]);
        for (int i = 0; i < result.getRows(); i++) {
            for (int j = 0; j < result.getColumns(); j++) {
                double valueToBeSet = 1 / (1 + Math.exp(-1 * mat.objectAtPoint(i, j)));
                result.setObjectAtPoint(i, j, valueToBeSet);
            }
        }
        return result;
    }

    // labels are converted to binary vectors because logistic units
    // are derived from the bernoulli distribution and so can only
    // have values in the range (0, 1).
    // eg: 3, 4, 0:
    // 0 0 1
    // 0 0 0
    // 0 0 0
    // 1 0 0
    // 0 1 0
    private Matrix labelsToBinary() {
        Matrix newY = new Matrix(new double[Ydata.getRows()][numLabels]);
        for (int i = 0; i < newY.getRows(); i++) {
            int YbinaryIndex = (int)Ydata.objectAtPoint(i, 0);// - 1;
            newY.setObjectAtPoint(i, YbinaryIndex, 1);
        }
        return newY;
    }

    // Randomly initialises all connections to break symmetry
    private Matrix randInitializeWeights(int layersIn, int layersOut) {
        Matrix result = new Matrix(new double[layersOut][layersIn + 1]);
        for (int i = 0; i < result.getRows(); i++) {
            for (int j = 0; j < result.getColumns(); j++) {
                double randomValue = rGen.nextDouble() * 2 * epsilonInit - epsilonInit;
                result.setObjectAtPoint(i, j, randomValue);
            }
        }
        return result;
    }

    // Shorthand printing for debugging.
    private void printSimp(String str) {
        System.out.println(str);
    }

    private int inputLayerSize;
    private int numLabels;
    private Matrix Theta1;
    private Matrix Theta2;
    private Matrix Theta1Gradient;
    private Matrix Theta2Gradient;
    private Matrix Xdata;
    private Matrix Ydata;
    private Matrix binaryLabels;
    private Random rGen;
    private HashMap<String, Matrix> feedForwardMap;
    private int maxIters;
}
