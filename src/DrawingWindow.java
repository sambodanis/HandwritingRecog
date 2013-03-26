/**
 * This class is used in gathering training data to be stored in a file.
 * The user is repeatedly prompted to enter a drawing for a specific label.
 * This isn't really part of the game so it is inacessable from the main program.
 */

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class DrawingWindow extends JFrame implements MouseInputListener {

    public DrawingWindow() {
        setSize(800, 800);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseMotionListener(this);
        addMouseListener(this);
        pointArray = new ArrayList<Point>();
        currFrame = this.getGraphics();
    }

    // Returns the array of all points visited.
    public ArrayList<Point> getPointArray() {
        return this.pointArray;
    }

    // Shorthand printing functions
    private void printSimple(String str) {
        System.out.println(str);
    }

    private void printPoint(Point p) {
        System.out.println("(" + p.x + ", " + p.y + ")");
    }


    // KEEP TRACK OF EUCLIDEAN DISTANCES BETWEEN POINTS,
    // BIG CHANGE = NEW WORD
    // Draw a line between the current and previous points
    // that the mouse has visited.
    public void mouseDragged(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());
        int currX = (int)currentPoint.getX();
        int currY = (int)currentPoint.getY();
        addToPointArray(currentPoint);
        if (previousPoint == null) previousPoint = currentPoint;
        currFrame.drawLine((int)previousPoint.getX(), (int)previousPoint.getY(),
                currX, currY);
        addIntermediatePoints(currentPoint, previousPoint);
        previousPoint = currentPoint;
    }

    // Adds a point that has been drawn to the
    // arrayList of path points provided that the point
    // is unique.
    private void addToPointArray(Point pNew) {
        for (Point p : pointArray) {
            if (p.x == pNew.x && p.y == pNew.y) return;
        }
        pointArray.add(pNew);
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
    private ArrayList<Point> pointArray;
}
