import java.awt.*;

/**
 * This class holds the information related to a single square
 * of the oxo board
 */
public class boardBox {

    private static final int sideLength = 150;

    // Constructed with the coordinates of the top left corner
    // as all other relevant data can be got from that.
    public boardBox(Point topleft) {
        this.topLeft = topleft;
        bottomRight = new Point(topleft.x + sideLength, topleft.y + sideLength);
    }

    // True if Point p is within the bounds
    // of this boardBox
    public boolean isInBounds(Point p) {
        return !(p.x < topLeft.x ||
                p.x > bottomRight.x ||
                p.y > bottomRight.y ||
                p.y < topLeft.y);
    }

    private Point topLeft;
    private Point bottomRight;
}
