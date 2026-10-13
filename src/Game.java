import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Random;

public class Game extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private BufferedImage back;
    private int key, count;
    private double bpm, beatOffsetSeconds;
    private int beat = 1;
    private Random random = new Random();
    private String level;
    private ArrayList<Rhythm> rhythms = new ArrayList<>();
    private Rectangle t1, t2, t3, t4, t5;
    private ArrayList<Rectangle> trackRectangles = new ArrayList<>();
    private long nextBeatNanos;
    private Sound s = new Sound();

    public Game(BeatDetector.Result result) {
        new Thread(this).start();
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        key = -1;
        count = 0;
        bpm = result.bpm;
        beatOffsetSeconds = result.offsetSeconds;
        level = "start";
    }

    @SuppressWarnings("static-access")
    public void run() {
        try {
            while (true) {
                Thread.currentThread().sleep(5);
                repaint();
            }
        } catch (Exception e) {
        }
    }

    public void screen(Graphics g2d) {
        switch (level) {

            case "start":
                drawStart(g2d);
                break;

            case "game":
                setRhythms();
                drawGame(g2d);

                count++;
                break;

        }

    }

    public void setRhythms() {
        long now = System.nanoTime();
        long beatIntervalNanos = (long) (60_000_000_000.0 / bpm);

        if (now >= nextBeatNanos) {
            System.out.println("beat " + beat);
            beat++;
            nextBeatNanos += beatIntervalNanos;

            int randX = random.nextInt(5) * 200 + (getWidth() / 2) - 500;
            rhythms.add(new Rhythm(randX, 0, 200, 45, getRandomColor()));
        }
    }

    public void paint(Graphics g) {
        Graphics2D twoDgraph = (Graphics2D) g;

        if (back == null)
            back = (BufferedImage) (createImage(getWidth(), getHeight()));

        Graphics g2d = back.createGraphics();

        g2d.clearRect(0, 0, getSize().width, getSize().height);

        screen(g2d);
        g2d.setFont(new Font("Courier New", Font.BOLD, 50));

        twoDgraph.drawImage(back, 0, 0, null);
    }

    public void drawStart(Graphics g2d) {
        // g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
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
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));

        drawRhythms(g2d, rhythms);
        drawTrack(g2d);
        drawRects(g2d);
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
            rhythm.setDy(1);
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
        for (Rhythm rhythm : rhythms) {
            Rectangle rhythmRect = new Rectangle(
                    rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());

            if (rhythmRect.intersects(hitRect)) {
                rhythms.remove(rhythm);
                return true;
            }

        }
        return false;
    }

    public Color getRandomColor() {
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);
        return new Color(r, g, b);
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
                level = "game";
                nextBeatNanos = System.nanoTime()
                    + (long) (beatOffsetSeconds * 1_000_000_000.0);

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
        }

        if (level == "game") {
            if (key == 68) {
                key = -1;
                if (checkHit(t1)) {
                    System.out.println("t1 hit");
                } else {
                    System.out.println("t1 miss");
                }
            }
            if (key == 70) {
                key = -1;
                if (checkHit(t2)) {
                    System.out.println("t2 hit");
                } else {
                    System.out.println("t2 miss");
                }
            }
            if (key == 74) {
                key = -1;
                if (checkHit(t3)) {
                    System.out.println("t3 hit");
                } else {
                    System.out.println("t3 miss");
                }
            }
            if (key == 75) {
                key = -1;
                if (checkHit(t4)) {
                    System.out.println("t4 hit");
                } else {
                    System.out.println("t4 miss");
                }
            }
            if (key == 76) {
                key = -1;
                if (checkHit(t5)) {
                    System.out.println("t5 hit");
                } else {
                    System.out.println("t5 miss");
                }
            }
            if (key == 82) {
                key = -1;
                rhythms.clear();
                count = 0;
                beat = 1;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == key) {
            key = -1;
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}