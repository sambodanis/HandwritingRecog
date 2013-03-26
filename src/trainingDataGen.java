/**
 * This class is not directly run from the main program,
 * its used to add more training examples to the datafiles.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class trainingDataGen implements Runnable {

    private static final String trainingFile = "dataOXO.txt";
    private static final String labelFile = "labelsOXO.txt";

    public static void main(String[] args) {
        trainingDataGen program = new trainingDataGen();

        SwingUtilities.invokeLater(program);
    }

    // Lets the user enter a drawing for a specific shape and submit it.
    private void getTrainingExample(int exampleIndex) {
        final DrawingWindow drawWindow = new DrawingWindow();
        Container content = drawWindow.getContentPane();
        content.setLayout(new FlowLayout());
        JButton doneButton = new JButton(numTrainingExamples < 100 ? "O - add to file" : "X - add to file");// "Click Once Finished Drawing");
        JLabel whichYouAreDrawing = new JLabel(exampleIndex == 0 ? "O" : "X");
        JLabel howManyDrawn = new JLabel("Example: " + numTrainingExamples);
        doneButton.setLocation(new Point(100, 100));
        doneButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                points = drawWindow.getPointArray();
                addToTrainingDataFile();
                System.out.println("Printed to file!");
                if (numTrainingExamples == 200) training = false;
                points = null;
                run();
            }

        });
        content.add(doneButton);
        content.add(whichYouAreDrawing);
        content.add(howManyDrawn);
        drawWindow.setContentPane(content);
    }

    // Appends a new training example and label to the datafiles
    private void addToTrainingDataFile() {
        numberExtractor numex = new numberExtractor();
        ArrayList<Point> scaledPoints = numex.scalePoints(points);
        Point[] boundingBox = numex.boundingBoxForSingleShape();
        Matrix pointsMatrix = new Matrix(null);
        pointsMatrix.makeMatrixFromPointsArrayList(scaledPoints, boundingBox);
        pointsMatrix.emulateWriting();
        Matrix convertedToRow = pointsMatrix.scaleDown().matrixToRowVector();
        try {
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(trainingFile, true));
            BufferedWriter labelWriter = new BufferedWriter(new FileWriter(labelFile, true));
            for (int i = 0; i < convertedToRow.getColumns(); i++) {
                dataWriter.write(String.valueOf(convertedToRow.objectAtPoint(0, i)));
                dataWriter.write(" ");
            }
            dataWriter.write("\n");
            if (numTrainingExamples < 100) {
                labelWriter.write("0\n");

            } else {
                labelWriter.write("1\n");
            }
            dataWriter.flush();
            dataWriter.close();
            labelWriter.flush();
            labelWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to open file");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Line cannot be read");
        }
    }

    public void run() {
        if (training) getTrainingExample(numTrainingExamples++ > 100 ? 1 : 0);
    }

    private boolean training;
    private int numTrainingExamples = 0;
    private ArrayList<Point> points;
}
