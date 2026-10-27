import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.*;
import java.util.Random;

public class Game extends JPanel implements KeyListener {

    private int key, combo, highestCombo, beats, score;
    private int difficulty = 2;
    private double bpm, beatOffsetSeconds, accuracy, totalAccuracy, travelBeats, latency, hitwindow;
    private long nextBeatIndex, musicStartNanos;
    private double nextPatternTime;
    private int nextPatternStep;
    private int nextPatternLane;
    private double inputOffsetTotal;
    private int inputOffsetCount;

    private String level, selectedSong;
    private boolean musicStarted, calibrating, timingCalibrated;
    private ArrayList<Double> calibrationTaps = new ArrayList<>();
    private Random random = new Random();
    private Sound s = new Sound();
    private ImageIcon background = new ImageIcon("space.png");

    private Rectangle t1, t2, t3, t4, t5;
    private ArrayList<Rectangle> trackRectangles = new ArrayList<>();
    private ArrayList<Rhythm> rhythms = new ArrayList<>();

    private String[] messages = new String[5];
    private int[] alpha = new int[5];
    private Color[] colors = new Color[5];
    private Timer[] fadeTimers = new Timer[5];

    public Game(BeatDetector.Result result, String musicFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.addKeyListener(this);

        key = -1;
        combo = 0;
        highestCombo = 0;
        beats = 0;
        score = 0;
        latency = 0;
        inputOffsetTotal = 0;
        inputOffsetCount = 0;

        bpm = result.bpm;
        beatOffsetSeconds = result.offsetSeconds;
        totalAccuracy = 0;
        travelBeats = 4.0;
        hitwindow = 0.2;

        level = "start";
        selectedSong = musicFile.substring(6, musicFile.length() - 4);
        resetPatternState();
        s.loadMusic(musicFile);

        Timer timer = new Timer(16, e -> {
            if ("game".equals(level)) {
                updateRhythms();
            }
            repaint();
        });
        timer.start();
    }

    public void screen(Graphics g2d) {
        switch (level) {

        case "start":
            drawStart(g2d);
            break;

        case "game":
            drawGame(g2d);
            checkMiss();
            checkEnd();
            break;

        case "results":
            drawResults(g2d);
            break;
        }

    }

    public void updateRhythms() {
        if (!musicStarted && System.nanoTime() >= musicStartNanos) {
            s.playPreparedMusic();
            musicStarted = true;
        }

        double songSeconds = getSongSeconds();
        double beatInterval = 60.0 / bpm;
        double travelSeconds = travelBeats * beatInterval;
        double futureLimit = Math.max(songSeconds, 0.0) + travelSeconds + beatInterval * 12;

        while (nextPatternTime <= futureLimit) {
            double targetSeconds = nextPatternTime;
            if (targetSeconds > s.getMusicLengthSeconds() + 2) {
                break;
            }

            int lane = nextPatternLane % trackRectangles.size();
            nextPatternLane++;
            int trackX = trackRectangles.get(lane).x;
            rhythms.add(new Rhythm(trackX, 0, 200, 45, getRandomColor(), targetSeconds));
            nextPatternTime += getPatternStepSize() * beatInterval;
        }

        double hitY = t1.y + t1.height / 2.0;
        double speed = hitY / travelSeconds;
        java.util.Iterator<Rhythm> iterator = rhythms.iterator();
        while (iterator.hasNext()) {
            Rhythm rhythm = iterator.next();
            double secondsUntilHit = rhythm.getTargetSeconds() - songSeconds;
            if (!rhythm.isHit()) {
                rhythm.setY((int) Math.round(hitY - rhythm.getH() / 2.0 - secondsUntilHit * speed));
            }
            if (secondsUntilHit < -beatInterval) {
                iterator.remove();
            }
        }
    }

    private double getPatternStepSize() {
        double[] pattern;
        if (difficulty == 1) {
            pattern = new double[] { 2.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0 };
        } else if (difficulty == 3) {
            pattern = new double[] { 0.5, 0.3333333333, 0.25, 0.5, 1.0, 0.3333333333, 0.5, 0.25, 1.0, 0.5, 0.3333333333, 0.25 };
        } else {
            pattern = new double[] { 1.0, 0.5, 1.0, 0.5, 1.0, 0.5, 2.0, 0.5 };
        }

        double step = pattern[nextPatternStep % pattern.length];
        nextPatternStep++;
        return step;
    }

    private void resetPatternState() {
        nextPatternTime = beatOffsetSeconds;
        nextPatternStep = 0;
        nextPatternLane = 0;
    }

    private double getSongSeconds() {
        double seconds = musicStarted ? s.getPlaybackSeconds() : (System.nanoTime() - musicStartNanos) / 1_000_000_000.0;
        return seconds - latency;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        screen(g);
    }

    public void drawStart(Graphics g2d) {
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);

        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Courier New", Font.BOLD, 150));
        g2d.drawString("Rhythm Trainer", (getWidth() - g2d.getFontMetrics().stringWidth("Rhythm Trainer")) / 2, 400);

        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 30));
        String selectedSongText = "Selected Song: " + selectedSong;
        g2d.drawString(selectedSongText, (getWidth() - g2d.getFontMetrics().stringWidth(selectedSongText)) / 2, getHeight() - 305);

        String difficultyName;
        if (difficulty == 1) {
            difficultyName = "Easy";
        } else if (difficulty == 3) {
            difficultyName = "Hard";
        } else {
            difficultyName = "Medium";
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        String instruction = "Press Space to start";
        g2d.drawString(instruction, (getWidth() - g2d.getFontMetrics().stringWidth(instruction)) / 2, getHeight() - 250);

        g2d.setFont(new Font("Courier New", Font.BOLD, 30));
        String difficultyText = "Difficulty: " + difficultyName + " (1 Easy | 2 Medium | 3 Hard)";
        g2d.drawString(difficultyText, (getWidth() - g2d.getFontMetrics().stringWidth(difficultyText)) / 2, getHeight() - 210);

        String calibrationStatus = calibrating ? "Tap Space with the beat; press Enter when done (" + calibrationTaps.size() + " taps)"
                : timingCalibrated ? String.format("Timing offset: %.0f ms | Press C to recalibrate", latency * 1000) : "Press C to calibrate";
        g2d.drawString(calibrationStatus, (getWidth() - g2d.getFontMetrics().stringWidth(calibrationStatus)) / 2, getHeight() - 170);
    }

    public void drawGame(Graphics g2d) {
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);

        drawRhythms(g2d, rhythms);
        drawTrack(g2d);
        drawRects(g2d);
        drawIndicators(g2d);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 30));

        g2d.drawString("Score: " + score, 50, 50);
        g2d.drawString("Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%", 50, 80);
        g2d.drawString("Combo: " + combo, 50, 180);

        if (combo >= highestCombo && combo > 0) {
            g2d.setColor(Color.CYAN);
        } else {
            g2d.setColor(Color.GRAY);
        }
        g2d.drawString("Highest Combo: " + highestCombo, 50, 150);

    }

    public void drawTrack(Graphics g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        ((Graphics2D) g2d).setStroke(new BasicStroke(5f));

        g2d.drawLine((getWidth() / 2) - 500, 100, (getWidth() / 2) - 500, getHeight() - 100);
        g2d.drawLine((getWidth() / 2) - 300, 100, (getWidth() / 2) - 300, getHeight() - 100);
        g2d.drawLine((getWidth() / 2) - 100, 100, (getWidth() / 2) - 100, getHeight() - 100);
        g2d.drawLine((getWidth() / 2) + 100, 100, (getWidth() / 2) + 100, getHeight() - 100);
        g2d.drawLine((getWidth() / 2) + 300, 100, (getWidth() / 2) + 300, getHeight() - 100);
        g2d.drawLine((getWidth() / 2) + 500, 100, (getWidth() / 2) + 500, getHeight() - 100);

        g2d.drawLine((getWidth() / 2) - 600, (getWidth() / 2) - 200, (getWidth() / 2) + 600, (getWidth() / 2) - 200);
        g2d.drawLine((getWidth() / 2) - 600, (getWidth() / 2) - 150, (getWidth() / 2) + 600, (getWidth() / 2) - 150);
    }

    public void drawRhythms(Graphics g2d, ArrayList<Rhythm> rhythms) {
        for (Rhythm rhythm : rhythms) {
            g2d.setColor(rhythm.getColor());
            g2d.fillRect(rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());
        }
    }

    public void drawRects(Graphics g2d) {
        g2d.setColor(Color.RED);
        g2d.drawRect(t1.x, t1.y, t1.width, t1.height);
        g2d.setColor(Color.BLUE);
        g2d.drawRect(t2.x, t2.y, t2.width, t2.height);
        g2d.setColor(Color.GREEN);
        g2d.drawRect(t3.x, t3.y, t3.width, t3.height);
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(t4.x, t4.y, t4.width, t4.height);
        g2d.setColor(Color.MAGENTA);
        g2d.drawRect(t5.x, t5.y, t5.width, t5.height);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 24));
        FontMetrics metrics = g2d.getFontMetrics();

        String[] labels = new String[] { "D", "F", "J", "K", "L" };
        Rectangle[] tracks = new Rectangle[] { t1, t2, t3, t4, t5 };

        for (int i = 0; i < tracks.length; i++) {
            Rectangle track = tracks[i];
            String label = labels[i];
            int x = track.x + (track.width - metrics.stringWidth(label)) / 2;
            int y = track.y + (track.height + metrics.getAscent()) / 2;
            g2d.drawString(label, x, y);
        }
    }

    public void drawResults(Graphics g2d) {
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);

        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Courier New", Font.BOLD, 100));
        g2d.drawString("Results", (getWidth() - g2d.getFontMetrics().stringWidth("Results")) / 2, 150);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 40));
        g2d.drawString("Final Score: " + score, (getWidth() - g2d.getFontMetrics().stringWidth("Final Score: " + score)) / 4, 400);
        g2d.drawString("Final Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%",
                (getWidth() - g2d.getFontMetrics().stringWidth("Final Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%")) / 4, 450);

        double avgInputOffset = inputOffsetCount == 0 ? 0.0 : inputOffsetTotal / inputOffsetCount;
        String offsetText = inputOffsetCount == 0 ? "--" : String.format("%.0f ms %s", Math.abs(avgInputOffset * 1000.0), avgInputOffset <= 0 ? "early" : "late");
        g2d.drawString("Avg input offset: " + offsetText, (getWidth() - g2d.getFontMetrics().stringWidth("Avg input offset: " + offsetText)) / 4, 500);
        g2d.drawString("Highest Combo: " + highestCombo, (getWidth() - g2d.getFontMetrics().stringWidth("Highest Combo: " + highestCombo)) / 4, 550);

        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        g2d.drawString("Press R to restart", (getWidth() - g2d.getFontMetrics().stringWidth("Press R to restart")) / 2, getHeight() - 100);
        g2d.drawString("Rank", 3 * (getWidth() - g2d.getFontMetrics().stringWidth("Rank")) / 4, 350);

        String rank = getRank();
        Color rankColor = getRankColor(rank);

        g2d.setFont(new Font("Courier New", Font.BOLD, 300));
        g2d.setColor(rankColor);
        g2d.drawString(rank, 3 * (getWidth() - g2d.getFontMetrics().stringWidth(rank)) / 4 + 10, 580);
    }

    public void createTracks() {
        trackRectangles.clear();

        t1 = new Rectangle((getWidth() / 2) - 500, (getWidth() / 2) - 200, 200, 50);
        t2 = new Rectangle((getWidth() / 2) - 300, (getWidth() / 2) - 200, 200, 50);
        t3 = new Rectangle((getWidth() / 2) - 100, (getWidth() / 2) - 200, 200, 50);
        t4 = new Rectangle((getWidth() / 2) + 100, (getWidth() / 2) - 200, 200, 50);
        t5 = new Rectangle((getWidth() / 2) + 300, (getWidth() / 2) - 200, 200, 50);

        trackRectangles.add(t1);
        trackRectangles.add(t2);
        trackRectangles.add(t3);
        trackRectangles.add(t4);
        trackRectangles.add(t5);
    }

    public boolean checkHit(Rectangle hitRect) {
        double songSeconds = getSongSeconds();
        for (int i = 0; i < rhythms.size(); i++) {
            Rhythm rhythm = rhythms.get(i);
<<<<<<< HEAD
            double timingError = songSeconds - rhythm.getTargetSeconds();
            double error = Math.abs(timingError);
=======
            double error = Math.abs(rhythm.getTars() - songSeconds);
>>>>>>> 903e067d55b2b1812c07689667da3b3aa78bcbfc

            if (rhythm.getX() == hitRect.x && error < hitwindow) {
                double offsetSeconds = songSeconds - rhythm.getTargetSeconds();
                inputOffsetTotal += offsetSeconds;
                inputOffsetCount++;
                totalAccuracy += (1 - error / hitwindow) * 100;
                beats++;
                rhythm.setHit(true);
                rhythm.setFading(true);
                fadeRhythm(rhythm, 2);
                showIndicator(trackRectangles.indexOf(hitRect), (1 - error / hitwindow) * 100, timingError);
                return true;
            }
        }
        beats++;
        showIndicator(trackRectangles.indexOf(hitRect), 0, 0);
        return false;
    }

    public void checkMiss() {
        for (Rhythm rhythm : rhythms) {
            if (rhythm.isFading() || rhythm.getY() <= t1.getY() + t1.getHeight()) {
                continue;
            }

            for (int track = 0; track < trackRectangles.size(); track++) {
                if (rhythm.getX() == trackRectangles.get(track).x) {
                    rhythm.setFading(true);
                    fadeRhythm(rhythm, 5);
                    beats++;
                    combo = 0;
                    showIndicator(track, 0, 0);
                    break;
                }
            }
        }
    }

    public void checkEnd() {
        if (musicStarted && s.getPlaybackSeconds() >= s.getMusicLengthSeconds() + 2) {
            level = "results";
        }
    }

    public void drawIndicators(Graphics g2d) {
        g2d.setFont(new Font("Courier New", Font.BOLD, 24));
        FontMetrics metrics = g2d.getFontMetrics();

        for (int i = 0; i < trackRectangles.size(); i++) {
            if (alpha[i] == 0 || messages[i] == null) {
                continue;
            }

            Rectangle track = trackRectangles.get(i);
            Color color = colors[i];
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha[i]));

            int x = track.x + (track.width - metrics.stringWidth(messages[i])) / 2;
            g2d.drawString(messages[i], x, track.y - 10);
        }
    }

    public Color getRandomColor() {
        int r = (int) (Math.random() * 225 + 30);
        int g = (int) (Math.random() * 225 + 30);
        int b = (int) (Math.random() * 225 + 30);
        return new Color(r, g, b);
    }

    public void showIndicator(int track, double accuracy, double timingError) {
        if (accuracy <= 0) {
            messages[track] = "Miss";
            colors[track] = Color.RED;
            score -= 30;
        } else if (Math.abs(timingError) <= 0.01) {
            messages[track] = "Perfect";
            colors[track] = Color.CYAN;
        } else if (Math.abs(timingError) <= 0.025) {
            messages[track] = "Good";
            colors[track] = Color.GREEN;
        } else if (Math.abs(timingError) >= 0.1) {
            messages[track] = timingError < 0 ? "Super Early!" : "Super Late!";
            colors[track] = Color.ORANGE;
        } else {
            messages[track] = timingError < 0 ? "Early!" : "Late!";
            colors[track] = Color.YELLOW;
        }

        if (accuracy >= 90) {
            score += 100;
        } else if (accuracy >= 50) {
            score += 50;
        } else if (accuracy > 0) {
            score += 20;
        }

        alpha[track] = 255;

        if (fadeTimers[track] != null) {
            fadeTimers[track].stop();
        }

        fadeTimers[track] = new Timer(30, e -> {
            alpha[track] = Math.max(0, alpha[track] - 8);
            repaint();

            if (alpha[track] == 0) {
                fadeTimers[track].stop();
            }
        });
        fadeTimers[track].start();
    }

    public void fadeRhythm(Rhythm rhythm, int speed) {
        Timer fadeTimer = new Timer(speed, e -> {
            Color currentColor = rhythm.getColor();
            int r = Math.max(0, currentColor.getRed() - 8);
            int g = Math.max(0, currentColor.getGreen() - 8);
            int b = Math.max(0, currentColor.getBlue() - 8);

            rhythm.setColor(new Color(r, g, b));

            if (r == 0 && g == 0 && b == 0) {
                rhythms.remove(rhythm);
                ((Timer) e.getSource()).stop();
            }
        });
        fadeTimer.start();
    }

    public String getRank() {
        if (beats == 0)
            return "--";

        accuracy = totalAccuracy / beats;
        if (accuracy >= 95)
            return "S";
        if (accuracy >= 90)
            return "A";
        if (accuracy >= 80)
            return "B";
        if (accuracy >= 70)
            return "C";
        if (accuracy >= 60)
            return "D";
        if (accuracy >= 30)
            return "F";
        return "Z";
    }

    public Color getRankColor(String rank) {
        switch (rank) {
        case "S":
            return Color.CYAN;
        case "A":
            return Color.GREEN;
        case "B":
            return Color.YELLOW;
        case "C":
            return Color.ORANGE;
        case "D":
            return Color.RED;
        case "F":
            return Color.GRAY;
        case "Z":
            return Color.DARK_GRAY;
        default:
            return Color.WHITE;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        key = e.getKeyCode();

        if (level == "start") {
            if (key == 49) {
                difficulty = 1;
                key = -1;
                return;
            }
            if (key == 50) {
                difficulty = 2;
                key = -1;
                return;
            }
            if (key == 51) {
                difficulty = 3;
                key = -1;
                return;
            }

            if (calibrating) {
                if (key == 32) {
                    double tapTime = s.getPlaybackSeconds();
                    if (calibrationTaps.isEmpty() || tapTime - calibrationTaps.get(calibrationTaps.size() - 1) >= 0.2) {
                        calibrationTaps.add(tapTime);
                    }
                } else if (key == 10) {
                    finishCalibration();
                }
                key = -1;
                return;
            }

            if (key == 67) {
                calibrationTaps.clear();
                s.resetMusic();
                s.playPreparedMusic();
                calibrating = true;
                key = -1;
                return;
            }

            if (key == 32) {
                key = -1;

                createTracks();
                resetPatternState();

                double travelSeconds = travelBeats * 60.0 / bpm;
                double leadInSeconds = Math.max(0, travelSeconds - beatOffsetSeconds);
                musicStartNanos = System.nanoTime() + (long) (leadInSeconds * 1_000_000_000.0);
                nextBeatIndex = 0;
                musicStarted = false;
                combo = 0;
                rhythms.clear();
                level = "game";
            }
        }

        if (level == "game") {
            Rectangle target;

            switch (key) {
            case 68:
                target = t1;
                break;
            case 70:
                target = t2;
                break;
            case 74:
                target = t3;
                break;
            case 75:
                target = t4;
                break;
            case 76:
                target = t5;
                break;
            case 82:
                key = -1;
                reset();
                return;
            case 32:
                key = -1;
                s.playmusic("stop");
                rhythms.clear();
                level = "results";
                return;
            default:
                return;
            }

            key = -1;
            combo = checkHit(target) ? combo + 1 : 0;
            if (combo > highestCombo) {
                highestCombo = combo;
            }
        }

        if (level == "results") {
            if (key == 82) {
                key = -1;
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
                Main.selectSongAndStart();
                return;
            }

            if (key == 61) {
                totalAccuracy += 10;
            }
            if (key == 45) {
                totalAccuracy -= 10;
            }

        }
    }

    public void reset() {
        s.resetMusic();
        rhythms.clear();
        createTracks();
        messages = new String[5];
        alpha = new int[5];
        combo = 0;
        beats = 0;
        score = 0;
        highestCombo = 0;
        totalAccuracy = 0;
        inputOffsetTotal = 0;
        inputOffsetCount = 0;
        nextBeatIndex = 0;
        musicStarted = false;
        resetPatternState();

        double travelSeconds = travelBeats * 60.0 / bpm;
        double leadInSeconds = Math.max(0, travelSeconds - beatOffsetSeconds);
        musicStartNanos = System.nanoTime() + (long) (leadInSeconds * 1_000_000_000.0);
    }

    private void finishCalibration() {
        if (calibrationTaps.size() < 5) {
            JOptionPane.showMessageDialog(this, "Tap at least five beats before finishing calibration.", "Not enough taps", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double beatInterval = 60.0 / bpm;
        double[] errors = new double[calibrationTaps.size()];

        for (int i = 0; i < errors.length; i++) {
            double sinceBeat = (calibrationTaps.get(i) - beatOffsetSeconds) % beatInterval;
            if (sinceBeat < 0) {
                sinceBeat += beatInterval;
            }
            if (sinceBeat > beatInterval / 2) {
                sinceBeat -= beatInterval;
            }
            errors[i] = sinceBeat;
        }

        Arrays.sort(errors);
        latency = errors[errors.length / 2];

        s.resetMusic();
        calibrating = false;
        timingCalibrated = true;
        calibrationTaps.clear();
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == key) {
            key = -1;
        }
    }

}
