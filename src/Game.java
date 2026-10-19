import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Random;

public class Game extends JPanel implements KeyListener {

    private int key, combo, beats;
    private double bpm, beatOffsetSeconds, totalAccuracy;
    private Random random = new Random();
    private String level;
    private ArrayList<Rhythm> rhythms = new ArrayList<>();
    private Rectangle t1, t2, t3, t4, t5;
    private String[] messages = new String[5];
    private int[] alpha = new int[5];
    private Color[] colors = new Color[5];
    private Timer[] fadeTimers = new Timer[5];
    private ArrayList<Rectangle> trackRectangles = new ArrayList<>();
    private long nextBeatIndex;
    private static final double TRAVEL_BEATS = 4.0;
    private Sound s = new Sound();
    private long musicStartNanos;
    private boolean musicStarted;
    private ImageIcon background = new ImageIcon("space.png");

    public Game(BeatDetector.Result result, String musicFile)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.addKeyListener(this);
        key = -1;
        combo = 0;
        totalAccuracy = 0;
        beats = 0;
        bpm = result.bpm;
        // bpm = 108.0;
        beatOffsetSeconds = result.offsetSeconds;
        level = "start";
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

                break;

        }

    }

    private void updateRhythms() {
        long now = System.nanoTime();
        if (!musicStarted && now >= musicStartNanos) {
            s.playPreparedMusic();
            musicStarted = true;
        }

        double songSeconds = musicStarted
                ? s.getPlaybackSeconds()
                : (now - musicStartNanos) / 1_000_000_000.0;
        double beatInterval = 60.0 / bpm;
        double travelSeconds = TRAVEL_BEATS * beatInterval;

        while (true) {
            double targetSeconds = beatOffsetSeconds + nextBeatIndex * beatInterval;
            if (targetSeconds - travelSeconds > songSeconds) {
                break;
            }

            if (targetSeconds > songSeconds) {
                int randX = random.nextInt(5) * 200 + (getWidth() / 2) - 500;
                rhythms.add(new Rhythm(randX, 0, 200, 45,
                        getRandomColor(), targetSeconds));
            }

            nextBeatIndex++;
        }

        double speed = (t1.y - 45) / travelSeconds;
        java.util.Iterator<Rhythm> iterator = rhythms.iterator();
        while (iterator.hasNext()) {
            Rhythm rhythm = iterator.next();
            double secondsUntilHit = rhythm.getTargetSeconds() - songSeconds;
            rhythm.setY((int) Math.round(
                    t1.y - rhythm.getH() - secondsUntilHit * speed));
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
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(new Color(100, 225, 247));
        g2d.setFont(new Font("Courier New", Font.BOLD, 150));
        g2d.drawString("Rhythm Trainer", (getWidth() - g2d.getFontMetrics().stringWidth("Rhythm Trainer")) / 2, 400);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        g2d.drawString("Press Space to start",
                (getWidth() - g2d.getFontMetrics().stringWidth("Press Space to start")) / 2, 700);
    }

    public void drawGame(Graphics g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 40));

        g2d.drawString("Combo: " + combo, 50, 100);
        g2d.drawString("Accuracy: " + (beats > 0 ? String.format("%.2f", totalAccuracy / beats) : "--.--") + "%", 50,
                150);

        drawRhythms(g2d, rhythms);
        drawTrack(g2d);
        drawRects(g2d);
        drawIndicators(g2d);
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

    public boolean checkHit(Rectangle hitRect) {
        for (int i = 0; i < rhythms.size(); i++) {
            Rhythm rhythm = rhythms.get(i);
            Rectangle rhythmRect = new Rectangle(
                    rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());

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

    private double accuracy(Rectangle rhythmRect, Rectangle hitRect) {
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
            }
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

    public void showIndicator(int lane, double accuracy) {
        if (accuracy >= 90) {
            messages[lane] = "Perfect";
            colors[lane] = Color.CYAN;
        } else if (accuracy >= 50) {
            messages[lane] = "Good";
            colors[lane] = Color.GREEN;
        } else if (accuracy > 0) {
            messages[lane] = "OK";
            colors[lane] = Color.YELLOW;
        } else {
            messages[lane] = "Miss";
            colors[lane] = Color.RED;
        }

        alpha[lane] = 255;

        if (fadeTimers[lane] != null) {
            fadeTimers[lane].stop();
        }

        fadeTimers[lane] = new Timer(30, e -> {
            alpha[lane] = Math.max(0, alpha[lane] - 8);
            repaint();

            if (alpha[lane] == 0) {
                fadeTimers[lane].stop();
            }
        });
        fadeTimers[lane].start();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        key = e.getKeyCode();
        // System.out.println(key);

        if (level == "start") {
            if (key == 32) {
                key = -1;

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

                double travelSeconds = TRAVEL_BEATS * 60.0 / bpm;
                double leadInSeconds = Math.max(0, travelSeconds - beatOffsetSeconds);
                musicStartNanos = System.nanoTime()
                        + (long) (leadInSeconds * 1_000_000_000.0);
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
                default:
                    return;
            }

            key = -1;
            combo = checkHit(target) ? combo + 1 : 0;
        }
    }

    private void reset() {
        s.resetMusic();
        rhythms.clear();
        combo = 0;
        nextBeatIndex = 0;
        musicStarted = false;

        double travelSeconds = TRAVEL_BEATS * 60.0 / bpm;
        double leadInSeconds = Math.max(0, travelSeconds - beatOffsetSeconds);
        musicStartNanos = System.nanoTime()
                + (long) (leadInSeconds * 1_000_000_000.0);
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == key) {
            key = -1;
        }

    }

}