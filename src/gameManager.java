/**
 * Game manager deals with holding the game state.
 * It also acts as a central hub between the
 * Play and the neural network classifier.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//Class plays the game
public class gameManager {

    // Files to load training data from
    private static final String trainingFile = "dataOXO.txt";
    private static final String labelFile = "labelsOXO.txt";

    // Number of iteration to train the neural network for
    // More iteration == greater accuracy
    // But only up until a certain point at which the
    // model over-fits the data.
    // eg: fitting a 6th order polynomial to 7 near linear data points
    // it may be very accurate but would not generalise well.
    private static final int iterations = 30;

    // Game manager constructor initialises all iVars
    // Neural networks trains upon construction which is a bit of a wait.
    // Board positions are initialised to ""
    public gameManager() {
        boardInternal = new String[3][3];
        Matrix xData = getTrainingData(trainingFile);
        Matrix yData = getTrainingData(labelFile);
        classifier = new NeuralNetworkNum(xData, yData, iterations, null, null);
        currentPlayer = null;
        indexToPointConversions = new HashMap<Integer, Point>();
        int k = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                indexToPointConversions.put(k++, new Point(i, j));
                boardInternal[i][j] = "";
            }
        }
    }

    // Given a set of points from the Play, use the Neural Network to
    // determine whether its a X or an O.
    // The location of the drawing is also given so that the
    // manager's internal state can be updated accordingly.
    // currentPlayer is lazily instantiated to take on the value of
    // whatever the first shape drawn is.
    public boolean predict(ArrayList<Point> points, int boxIndex) {
        Point boardPoint = indexToPointConversions.get(boxIndex);
        if (!boardInternal[boardPoint.x][boardPoint.y].equals("")
                || points == null) return false;
        int prediction = classifier.predict(points);
        if (currentPlayer == null) {
            currentPlayer = prediction == 0 ? Player.O : Player.X;
        }
        if ((prediction == 0 && currentPlayer.equals(Player.O)) ||
                (prediction == 1 && currentPlayer.equals(Player.X))) {
            boardInternal[boardPoint.x][boardPoint.y] = currentPlayer.toString();
            currentPlayer = currentPlayer.other();
            return true;
        }
        return false;
    }

    // Prints out the board for debugging
    public String toString() {
        return  "     1   2   3\n\n" +
                " a   " + boardInternal[0][0]
                + " | " + boardInternal[0][1]
                + " | " + boardInternal[0][2] + " \n" +
                "    ---+---+---\n" +
                " b   " + boardInternal[1][0]
                + " | " + boardInternal[1][1]
                + " | " + boardInternal[1][2] + " \n" +
                "    ---+---+---\n" +
                " c   " + boardInternal[2][0]
                + " | " + boardInternal[2][1]
                + " | " + boardInternal[2][2] + " \n";
    }

    // Reads the contents of a file into an arrayList
    // This is then turned into a matrix.
    // Data format:
    // 1 training example per new line
    // each feature of the example separated by a space.
    public Matrix getTrainingData(String filename) {
        ArrayList<String> allLines = new ArrayList<String>();
        String currentLine;
        BufferedReader trainingData;

        try {
            trainingData = new BufferedReader(new FileReader(filename));
            while ((currentLine = trainingData.readLine()) != null) {
                allLines.add(currentLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to open file");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Line cannot be read");
        }
        return new Matrix(null).makeMatrixFromStringArrayList(allLines);
    }

    //Finds out who the current player is
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Determines if there is a winner.
    public Player winner() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boardInternal[i][j].equals(boardInternal[i][(j + 1) % 3]) &&
                        boardInternal[i][(j + 1) % 3].equals(boardInternal[i][(j + 2) % 3]) &&
                        !boardInternal[i][j].equals("")) {
                    // Vertical win
                    return boardInternal[i][j].equals(currentPlayer.name()) ? currentPlayer : currentPlayer.other();
                } else if (boardInternal[i][j].equals(boardInternal[(i + 1) % 3][j]) &&
                        boardInternal[(i + 1) % 3][j].equals(boardInternal[(i + 2) % 3][j]) &&
                        !boardInternal[i][j].equals("")) {
                    // Horizontal win
                    return boardInternal[i][j].equals(currentPlayer.name()) ? currentPlayer : currentPlayer.other();
                } else if (i == j && boardInternal[i][j].equals(boardInternal[(i + 1) % 3][(j + 1) % 3]) &&
                        boardInternal[(i + 1) % 3][(j + 1) % 3].equals(boardInternal[(i + 2) % 3][(j + 2) % 3]) &&
                        !boardInternal[i][j].equals("")) {
                    // Diagonal down from top left win
                    return boardInternal[i][j].equals(currentPlayer.name()) ? currentPlayer : currentPlayer.other();
                } else if (i + j == 2 && boardInternal[0][2].equals(boardInternal[1][1]) &&
                        boardInternal[1][1].equals(boardInternal[2][0]) && !boardInternal[1][1].equals("")) {
                    // Diagonal down from top right win.
                    return boardInternal[0][2].equals(currentPlayer.name()) ? currentPlayer : currentPlayer.other();

                }
            }
        }
        boolean boardIsFilled = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boardInternal[i][j].equals("")) boardIsFilled = false;
            }
        }
        if (boardIsFilled) return Player.Both;
        return Player.None;
    }

    //If there is a winner the game is closed and a new window is opened
    // announcing the winner
    public void winmes()  {
        JFrame content = new JFrame();
        JPanel textArea = new JPanel(new FlowLayout(FlowLayout.CENTER));
        content.setTitle("WINNER");
        JButton b3 = new JButton("New Game");
        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent){
                Play newg = new Play();
            }
        });
        GridLayout choice = new GridLayout(2, 0);
        textArea.setLayout(choice);
        textArea.add(win());
        textArea.add(b3);
        content.add(textArea);
        content.setSize(new Dimension(200, 230));
        content.setResizable(false);
        content.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content.setLocationByPlatform(true);
        content.setVisible(true);
    }

    //Label that is printed if someone wins
    private JLabel win() {
        JLabel label = new JLabel();
        if (winner().equals(Player.X)) {
            label.setText("X WINS!!!!!!!");
        } else if (winner().equals(Player.O)) {
            label.setText("O WINS!!!!!!!");
        }
        label.setHorizontalAlignment( SwingConstants.CENTER );
        return label;
    }



    private String[][] boardInternal;
    private NeuralNetworkNum classifier;
    private Player currentPlayer;
    private HashMap<Integer, Point> indexToPointConversions;
}
