import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

//Creates the Graphical User Interface
public class Play extends JFrame implements MouseInputListener{

    private static final int side_length = 150;
    private static final Point initialPoint = new Point(50, 30);
    private static final boolean debugging = false;

    //Constructor that generates the board and components in a window
    public Play() {
        JPanel content = new JPanel();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        JPanel options = new JPanel();
        GridLayout choice = new GridLayout(4, 0);
        options.setLayout(choice);
        JButton b2 = new JButton("Submit");
        JButton b3 = new JButton("New Game");
        JButton b4 = new JButton("Help");
        JButton player = new JButton("Current Player");

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent){
                int k = 0;
                for (int i : pointDrawLocationCounter) {
                    if (debugging) System.out.println(k++ + ": " + i);
                }
                if (manager.getCurrentPlayer() != null && debugging) {
                    System.out.print(manager.getCurrentPlayer().toString());
                }
                if (debugging) System.out.println(manager.toString());

                // if drawn shape is not allowed, it is whited out.
                if (!manager.predict(pointArray, returnPosition())) {
                    if (debugging) System.out.println("You cannot draw that!");
                    currFrame.setColor(Color.white);
                    for (Point p : pointArray) {
                        currFrame.drawLine(p.x, p.y, p.x, p.y);
                    }
                    currFrame.setColor(Color.black);
                }

                if (debugging) {
                    System.out.println("Player: " + manager.getCurrentPlayer());
                    System.out.println("Board: " + manager.toString());
                    System.out.println("Winner: " + manager.winner());
                }
                if (manager.winner().equals(Player.X) || manager.winner().equals(Player.O)) {
                    manager.winmes();
                    setVisible(false);
                    close();
                }
                // pointArray must be reinitalised each
                // submission so that it only contains
                // the points from the most recent shape.
                pointArray = new ArrayList<Point>();
            }
        });

        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent){
                close();
                Play newg = new Play();
            }
        });

        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Help newHelp = new Help();
            }
        });

        player.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                whichPlayer play = new whichPlayer(manager);
            }
        });

        options.add(b2);
        options.add(b3);
        options.add(b4);
        options.add(player);
        UserInputPanel p = new UserInputPanel();
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(p)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(options, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(p)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(options, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
        setContentPane(content);
        setTitle("Noughts and Crosses");
        addMouseMotionListener(this);
        addMouseListener(this);
        setSize(new Dimension(650, 650));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setVisible(true);
        currFrame = this.getGraphics();
    }

    // Removes the graphics window
    public void close() {
        this.dispose();
    }

    // Initialises the game board graphics.
    // The 'added' bool is required because paint is actually
    // called several times and without it, boxSquares contains
    // too many elements.
    public void paint(Graphics g) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point temp = new Point(initialPoint.y + side_length * j, initialPoint.x + side_length * i);
                g.drawRect(temp.x, temp.y, side_length, side_length);
                if (!added) boardSquares.add(new boardBox(temp));
            }
        }
        added = true;
    }

    // Required for formatting
    class UserInputPanel extends JPanel {}

    // Initialises the program
    public static void main(String[] args) {
        Play test = new Play();
    }

    // Draws where the mouse is dragged
    // as opposed to drawing individual pixels, it just connects
    // points the mouse passes with lines.
    public void mouseDragged(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());
        int currX = (int)currentPoint.getX();
        int currY = (int)currentPoint.getY();
        if (isInBounds(currX, currY)) {
            addToPointArray(currentPoint);
            if (previousPoint == null) previousPoint = currentPoint;
            currFrame.drawLine((int)previousPoint.getX(), (int)previousPoint.getY(), currX, currY);
            addIntermediatePoints(currentPoint, previousPoint);
            previousPoint = currentPoint;
        }
    }


    //Checks the points drawn and returns the location on the board
    // with the most points drawn into it.
    private int returnPosition() {
        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < pointDrawLocationCounter.length; i++) {
            if (pointDrawLocationCounter[i] > max) {
                max = pointDrawLocationCounter[i];
                maxIndex = i;
            }
        }
        pointDrawLocationCounter = new int[9];
        return maxIndex;
    }

    // Checks if the point is within bounds of the box
    private boolean isInBounds(int x, int y) {
        int i = 0;
        for (boardBox b: boardSquares) {
            if (b.isInBounds(new Point(x, y))) {
                pointDrawLocationCounter[i]++;
                return true;
            }
            i++;
        }
        return false;
    }

    // When java captures mouse movement it is not done particularly accurately
    // or with a very high 'recording' frequency.
    // What happens is that only about 1/5 of the visible points are
    // actually recorded.
    // The solution to this was to generate a function between two points
    // and evaluate that function at each point, adding the results to the
    // point arrayList.
    private void addIntermediatePoints(Point currentPoint, Point previousPoint) {
        boolean currXlessThanPrev = false;
        if (currentPoint.x < previousPoint.x) currXlessThanPrev = true;
        boolean currYlessThanPrev = false;
        if (currentPoint.y < previousPoint.y) currYlessThanPrev = true;
        int i = previousPoint.x;
        int j = previousPoint.y;
        while (true) {
            if (i == currentPoint.x && j == currentPoint.y) break;
            if (currXlessThanPrev) {
                if (i != currentPoint.x) i--;
            } else {
                if (i != currentPoint.x) i++;
            }
            if (currYlessThanPrev) {
                if (j != currentPoint.y) j--;
            } else {

                if (j != currentPoint.y) j++;
            }
            addToPointArray(new Point(i, j));
        }
    }

    // Adds a point that has been drawn to the
    // arrayList of path points provided that the po
    private void addToPointArray(Point pNew) {
        for (Point p : pointArray) {
            if (p.x == pNew.x && p.y == pNew.y) return;
        }
        pointArray.add(pNew);
    }

    // Mouse released at the end of a continuous line.
    // This is so that separate lines are not connected end to end.
    public void mouseReleased(MouseEvent e) {
        previousPoint = null;
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    private Point previousPoint;
    private Graphics currFrame;
    private ArrayList<Point> pointArray = new ArrayList<Point>();
    private ArrayList<boardBox> boardSquares = new ArrayList<boardBox>();
    private gameManager manager = new gameManager();
    private int[] pointDrawLocationCounter = new int[9];
    private boolean added = false;
}
