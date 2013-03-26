import javax.swing.*;
import java.awt.*;

//Class makes the Help button in the Play
public class Help extends JFrame {

    //Makes the new window
    public Help() {
        JFrame content = new JFrame();
        JPanel textArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        setTitle("Help");
        textArea.add(info());
        add(textArea);
        textArea.setLocation(0, 0);
        setSize(new Dimension(250, 230));
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public static void main(String[] args) {
        Help test = new Help();
    }

    //The text inside the window
    private JLabel info() {
        JLabel text = new JLabel();
        text.setText("<HTML>Hello and welcome to Noughts <BR>" +
                        "and Crosses. The game is a two <BR>" +
                        "player game. Player 1 should begin <BR> + " +
                        "by choosing to be X or O. The <BR>" +
                        "winner of the game is the first <BR>" +
                        "player to get three of their <BR>" +
                        "symbols in a row.</HTML>");
        return text;
    }
}