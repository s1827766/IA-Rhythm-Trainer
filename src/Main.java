import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("unused")
public class Main extends JFrame {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 985;

    public Main() {
        super("Rhythm Trainer");
        setSize(WIDTH, HEIGHT);
        Game play = new Game();
        ((Component) play).setFocusable(true);

        setBackground(Color.black);

        getContentPane().add(play);

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        Main run = new Main();

    }

}