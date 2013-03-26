import javax.swing.*;
import java.awt.*;

//Class tells which players go it is and opens it in a window
public class whichPlayer extends JFrame {

    // Constructs a JFrame that informs the user of the
    // current player.
    public whichPlayer(gameManager input) {
        JLabel player = new JLabel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (input.getCurrentPlayer() == null) {
            player.setText("Waiting for first move");
        } else if (input.getCurrentPlayer().equals(Player.X)) {
            player.setText("The Current Player is X");
        }
        else {
            player.setText("The Current Player is O");
        }
        add(player);
        setSize(new Dimension(200, 200));
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setVisible(true);
    }

}