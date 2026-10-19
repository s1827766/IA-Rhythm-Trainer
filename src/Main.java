import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class Main extends JFrame {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 900;

    public Main(BeatDetector.Result result, String filePath)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        super("IA Rhythm Trainer");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        Game play = new Game(result, filePath);
        play.setFocusable(true);
        getContentPane().add(play);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::selectSongAndStart);
    }

    private static void selectSongAndStart() {
        File songsFolder = new File("songs");
        File[] songs = songsFolder.listFiles(file -> file.isFile()
                && (file.getName().toLowerCase(Locale.ROOT).endsWith(".wav")));

        if (songs == null || songs.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "No WAV songs were found in the songs folder.",
                    "No songs found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Arrays.sort(songs, (first, second) ->
                first.getName().compareToIgnoreCase(second.getName()));
        String[] songNames = Arrays.stream(songs)
                .map(File::getName)
                .toArray(String[]::new);

        JList<String> songList = new JList<>(songNames);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setVisibleRowCount(Math.min(songNames.length, 10));
        songList.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.setPreferredSize(new Dimension(300, 100));

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    scrollPane,
                    "Choose a song!",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            String selectedName = songList.getSelectedValue();
            if (selectedName == null) {
                JOptionPane.showMessageDialog(null,
                        "Select a song from the list.",
                        "No song selected", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            File selectedSong = new File(songsFolder, selectedName);
            try {
                BeatDetector.Result detected = BeatDetector.analyze(selectedSong);
                Double selectedBpm = askForBpm(detected.bpm);

                if (selectedBpm == null) {
                    continue;
                }

                BeatDetector.Result gameSettings =
                        new BeatDetector.Result(selectedBpm, detected.offsetSeconds);

                System.out.printf("Song: %s%n", selectedName);
                System.out.printf("Detected BPM: %.1f%n", detected.bpm);
                System.out.printf("BPM used: %.1f%n", selectedBpm);
                System.out.printf("Offset: %.2f seconds%n", detected.offsetSeconds);

                new Main(gameSettings, selectedSong.getPath());
                return;
            } catch (IOException | UnsupportedAudioFileException
                    | LineUnavailableException | IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not load or analyze " + selectedName + ":\n"
                                + e.getMessage()
                                + "\nTry a WAV file, or install a Java Sound MP3 provider.",
                        "Unable to load song", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Double askForBpm(double detectedBpm) {
        JTextField bpmField = new JTextField(
                String.format(Locale.ROOT, "%.1f", detectedBpm), 10);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel(String.format(
                Locale.ROOT, "Detected BPM: %.1f", detectedBpm)));
        panel.add(new JLabel("BPM to use (edit if needed):"));
        panel.add(bpmField);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Confirm BPM",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                double bpm = Double.parseDouble(bpmField.getText().trim());
                if (!Double.isNaN(bpm) && !Double.isInfinite(bpm) && bpm > 0) {
                    return bpm;
                }
            } catch (NumberFormatException e) {
                // Show the validation message below and let the user try again.
            }

            JOptionPane.showMessageDialog(null,
                    "Enter a valid BPM greater than zero.",
                    "Invalid BPM", JOptionPane.WARNING_MESSAGE);
        }
    }
}