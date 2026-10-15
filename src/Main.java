import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

@SuppressWarnings("unused")
public class Main extends JFrame {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 900;
    public static final String filePath = "The Less I Know The Better.wav";

    public Main(BeatDetector.Result result, String filePath)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        super("Rhythm Trainer");
        setSize(WIDTH, HEIGHT);

        Game play = new Game(result, filePath);
        play.setFocusable(true);
        getContentPane().add(play);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            BeatDetector.Result result =
                    BeatDetector.analyze(new java.io.File(filePath));

            System.out.printf("BPM: %.1f%n", result.bpm);
            System.out.printf("Offset: %.2f seconds%n", result.offsetSeconds);

            new Main(result, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}