import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Random;

public class Game extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private BufferedImage back;
    private int key, count;
    private Random random = new Random();
    private String level;
    private ArrayList<Rhythm> rhythms = new ArrayList<>();

    public Game() {
        new Thread(this).start();
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        key = -1;
        count = 0;
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
                drawRhythms(g2d, rhythms);
                break;

        }

    }

    public void setRhythms() {
        if (count % 450 == 0) {

            int randX = random.nextInt(5) * 200 + (getWidth() / 2) - 500;
            rhythms.add(new Rhythm(randX, 0, 200, 25, getRandomColor()));
            rhythms.get(rhythms.size() - 1).setDy(1);
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

        drawTrack(g2d);
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
        System.out.println(key);

        if (level == "start") {
            if (key == 32) {
                key = -1;
                level = "game";
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