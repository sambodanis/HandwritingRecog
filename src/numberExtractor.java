import java.awt.*;
import java.util.ArrayList;

/**
 * This class is able to separate two numbers that are both drawn on the screen.
 * However this program only uses it to compute bounding boxes
 */
public class numberExtractor {

    public numberExtractor() {
        firstNum = new ArrayList<Point>();
        secondNum = new ArrayList<Point>();
    }

    // Builds an internal model of points.
    public ArrayList<Point> scalePoints(ArrayList<Point> currPoints) {
        try {
            buildGraph(currPoints, true);
            return firstNum;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Returns the bounding box of a shape.
    public Point[] boundingBoxForSingleShape() {
        return bBox1;
    }

    // Returns seperated numbers, as arrayLists in an arrayList.
    public ArrayList<ArrayList<Point>> getSeperatedNumbers(ArrayList<Point> currPoints) {
        buildGraph(currPoints, false);
        ArrayList<ArrayList<Point>> result = new ArrayList<ArrayList<Point>>();
        result.add(firstNum);
        result.add(secondNum);
        return result;
    }

    // Returns bounding box, note, must be called after numbers are seperated.
    public ArrayList<Point[]> getBoundingBoxes() {
        ArrayList<Point[]> result = new ArrayList<Point[]>();
        if (bBox1 == null && bBox2 == null) return null;
        result.add(bBox1);
        result.add(bBox2);
        return result;
    }

    // Determine the bounding box of an array of points
    private Point[] boundingBoxFromPoints(ArrayList<Point> pathPoints) {
        Point[] result = new Point[2];
        int minX = pathPoints.get(0).x;
        int maxX = pathPoints.get(0).x;
        int minY = pathPoints.get(0).y;
        int maxY = pathPoints.get(0).y;

        for (Point elem : pathPoints) {
            if (elem.x < minX) {
                minX = elem.x;
            } else if (elem.x > maxX) maxX = elem.x;
            if (elem.y < minY) {
                minY = elem.y;
            } else if (elem.y > maxY) maxY = elem.y;
        }
        result[0] = new Point(minX, maxY);
        result[1] = new Point(maxX, minY);
        return result;
    }

    // Euclidean distance
    private double distanceBetweenPoints(int p1x, int p1y, int p2x, int p2y) {
        double xDiff = p2x - p1x;
        double yDiff = p2y - p1y;
        double distance = Math.pow(xDiff, 2.0) + Math.pow(yDiff, 2.0);
        return Math.sqrt(distance);
    }

    // Iterate through the arraylist, add all objects to the set of
    // the first number, once there is a big distance difference
    // between two consecutive elements, start adding to the second
    // number's set.
    private void buildGraph(ArrayList<Point> currPoints, boolean oneShape) {
        int maxDist = 0;
        int indexMaxDist = 0;
        Point previousPoint = currPoints.get(0);
        int distance;
        for (int i = 0; i < currPoints.size(); i++) {
            double xDiff = currPoints.get(i).getX() - previousPoint.getX();
            double yDiff = currPoints.get(i).getY() - previousPoint.getY();
            distance = (int)Math.pow(xDiff, 2.0) + (int)Math.pow(yDiff, 2.0);
            distance = (int)Math.sqrt((double)distance);
            previousPoint = currPoints.get(i);
            if (distance - maxDist > 0) {
                maxDist = distance;
                indexMaxDist = i;
            }
        }
        if (oneShape) {
            firstNum.addAll(currPoints);
            bBox1 = boundingBoxFromPoints(firstNum);
        } else {
            for (int i = 0; i < currPoints.size(); i++) {
                if (i < indexMaxDist) {
                    firstNum.add(currPoints.get(i));
                } else {
                    secondNum.add(currPoints.get(i));
                }
            }
            bBox1 = boundingBoxFromPoints(firstNum);
            bBox2 = boundingBoxFromPoints(secondNum);
        }
    }


    private ArrayList<Point> firstNum;
    private ArrayList<Point> secondNum;
    private Point[] bBox1;
    private Point[] bBox2;
}
