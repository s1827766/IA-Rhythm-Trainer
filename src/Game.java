import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Random;

public class Game extends JPanel implements KeyListener {

    private int key, combo, highestCombo, beats, score;
    private double bpm, beatOffsetSeconds, totalAccuracy, travelBeats = 4.0;
    private long nextBeatIndex, musicStartNanos;
    private double[] beatTimes;

    private String level, selectedSong = "Unknown";
    private boolean musicStarted;
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
        totalAccuracy = 0;
        beats = 0;
        score = 0;
        bpm = result.bpm;
        beatOffsetSeconds = result.offsetSeconds;
        beatTimes = result.beatTimes.clone();
        level = "start";
        selectedSong = musicFile.substring(6, musicFile.length() - 4);
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

            for (Rectangle trackRect : trackRectangles) {
                checkMiss(trackRect);
            }

            checkEnd();

            break;

        case "results":
            drawResults(g2d);
            break;
        }

    }

    public void updateRhythms() {
        long now = System.nanoTime();
        if (!musicStarted && now >= musicStartNanos) {
            s.playPreparedMusic();
            musicStarted = true;
        }

        double songSeconds = musicStarted ? s.getPlaybackSeconds() : (now - musicStartNanos) / 1_000_000_000.0;
        double beatInterval = 60.0 / bpm;
        double travelSeconds = travelBeats * beatInterval;

        while (true) {
            if (beatTimes.length > 0 && nextBeatIndex >= beatTimes.length) {
                break;
            }

            double targetSeconds = beatTimes.length > 0
                    ? beatTimes[(int) nextBeatIndex]
                    : beatOffsetSeconds + nextBeatIndex * beatInterval;
            if (targetSeconds - travelSeconds > songSeconds) {
                break;
            }

            if (targetSeconds > songSeconds) {
                int randX = random.nextInt(5) * 200 + (getWidth() / 2) - 500;
                rhythms.add(new Rhythm(randX, 0, 200, 45, getRandomColor(), targetSeconds));
            }

            nextBeatIndex++;
        }

        double speed = (t1.y - 45) / travelSeconds;
        java.util.Iterator<Rhythm> iterator = rhythms.iterator();
        while (iterator.hasNext()) {
            Rhythm rhythm = iterator.next();
            double secondsUntilHit = rhythm.getTargetSeconds() - songSeconds;
            rhythm.setY((int) Math.round(t1.y - rhythm.getH() - secondsUntilHit * speed));
            if (secondsUntilHit < -beatInterval) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        screen(g);
    }

    public void drawStart(Graphics g2d) {
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(new Color(100, 225, 247));
        g2d.setFont(new Font("Courier New", Font.BOLD, 150));
        g2d.drawString("Rhythm Trainer", (getWidth() - g2d.getFontMetrics().stringWidth("Rhythm Trainer")) / 2, 400);
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 30));
        g2d.drawString("Selected Song: " + selectedSong, (getWidth() - g2d.getFontMetrics().stringWidth("Selected Song: " + selectedSong)) / 2, getHeight() - 305);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        g2d.drawString("Press Space to start", (getWidth() - g2d.getFontMetrics().stringWidth("Press Space to start")) / 2, getHeight() - 250);
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

        if (combo >= highestCombo && combo > 0) {
            g2d.setColor(Color.CYAN);
        } else {
            g2d.setColor(Color.GRAY);
        }
        g2d.drawString("Highest Combo: " + highestCombo, 50, 150);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Combo: " + combo, 50, 180);
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
    }

    public void drawResults(Graphics g2d) {
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(new Color(100, 225, 247));
        g2d.setFont(new Font("Courier New", Font.BOLD, 100));
        g2d.drawString("Results", (getWidth() - g2d.getFontMetrics().stringWidth("Results")) / 2, 150);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 40));
        g2d.drawString("Final Score: " + score, (getWidth() - g2d.getFontMetrics().stringWidth("Final Score: " + score)) / 4, 400);
        g2d.drawString("Final Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%",
                (getWidth() - g2d.getFontMetrics().stringWidth("Final Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%")) / 4, 450);
        g2d.drawString("Highest Combo: " + highestCombo, (getWidth() - g2d.getFontMetrics().stringWidth("Highest Combo: " + highestCombo)) / 4, 500);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        g2d.drawString("Press R to restart", (getWidth() - g2d.getFontMetrics().stringWidth("Press R to restart")) / 2, getHeight() - 100);
        g2d.drawString("Rank", 3 * (getWidth() - g2d.getFontMetrics().stringWidth("Rank")) / 4, 350);

        String rank = getRank();
        Color rankColor = getRankColor(rank);
        g2d.setColor(rankColor);
        g2d.setFont(new Font("Courier New", Font.BOLD, 300));
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
        for (int i = 0; i < rhythms.size(); i++) {
            Rhythm rhythm = rhythms.get(i);
            Rectangle rhythmRect = new Rectangle(rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());

            if (rhythmRect.intersects(hitRect)) {
                totalAccuracy += accuracy(rhythmRect, hitRect);
                beats++;
                showIndicator(trackRectangles.indexOf(hitRect), accuracy(rhythmRect, hitRect));
                rhythms.remove(i);
                return true;
            }
        }
        beats++;
        showIndicator(trackRectangles.indexOf(hitRect), 0);
        return false;
    }

    public double accuracy(Rectangle rhythmRect, Rectangle hitRect) {
        double distance = Math.abs(rhythmRect.getCenterY() - hitRect.getCenterY());
        double maxDistance = (rhythmRect.height + hitRect.height) / 2.0;

        return Math.max(0, (1.0 - distance / maxDistance) * 100.0);
    }

    public void checkMiss(Rectangle hitRect) {
        for (int i = 0; i < rhythms.size(); i++) {
            Rhythm rhythm = rhythms.get(i);
            if (rhythm.getY() > hitRect.getY() + hitRect.getHeight()) {
                rhythms.remove(i);
                beats++;
                i--;
                combo = 0;
                score -= 30;
                showIndicator(trackRectangles.indexOf(hitRect), 0);
            }
        }
    }

    public void checkEnd() {
        if (musicStarted && s.getPlaybackSeconds() >= s.getMusicLengthSeconds()) {
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

    public void showIndicator(int track, double accuracy) {
        if (accuracy >= 90) {
            messages[track] = "Perfect";
            colors[track] = Color.CYAN;
            score += 100;
        } else if (accuracy >= 50) {
            messages[track] = "Good";
            colors[track] = Color.GREEN;
            score += 50;
        } else if (accuracy > 0) {
            messages[track] = "OK";
            colors[track] = Color.YELLOW;
            score += 20;
        } else {
            messages[track] = "Miss";
            colors[track] = Color.RED;
            score -= 30;
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

    public String getRank() {
        if (beats == 0)
            return "--";

        double accuracy = totalAccuracy / beats;
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
        return "F";
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
        System.out.println(key);

        if (level == "start") {
            if (key == 32) {
                key = -1;

                createTracks();

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
                break; // D
            case 70:
                target = t2;
                break; // F
            case 74:
                target = t3;
                break; // J
            case 75:
                target = t4;
                break; // K
            case 76:
                target = t5;
                break; // L
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
                totalAccuracy += 30;
            }
            if (key == 45) {
                totalAccuracy -= 30;
            }

        }
    }

    private void reset() {
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
        nextBeatIndex = 0;
        musicStarted = false;

        double travelSeconds = travelBeats * 60.0 / bpm;
        double leadInSeconds = Math.max(0, travelSeconds - beatOffsetSeconds);
        musicStartNanos = System.nanoTime() + (long) (leadInSeconds * 1_000_000_000.0);
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == key) {
            key = -1;
        }

    }

}