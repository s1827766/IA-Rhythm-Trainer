import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("unused")
public class Main extends JFrame {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 900;

    public Main(BeatDetector.Result result) {
        super("Rhythm Trainer");
        setSize(WIDTH, HEIGHT);

        Game play = new Game(result);
        play.setFocusable(true);
        getContentPane().add(play);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            BeatDetector.Result result =
                    BeatDetector.analyze(new java.io.File("Bang Bang Bang.wav"));

            System.out.printf("BPM: %.1f%n", result.bpm);
            System.out.printf("Offset: %.2f seconds%n", result.offsetSeconds);

            new Main(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}