import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class Game extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private BufferedImage back;
    private int key;
    private String level;

    public Game() {
        new Thread(this).start();
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        key = -1;
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
                drawGame(g2d);
                break;

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

    private void drawStart(Graphics g2d) {
        //g2d.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(new Color(100, 225, 247));
		g2d.setFont(new Font("Courier New", Font.BOLD, 150));
		g2d.drawString("Rhythm Trainer", (getWidth() - g2d.getFontMetrics().stringWidth("Rhythm Trainer")) / 2, 400);
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Courier New", Font.BOLD, 60));
		g2d.drawString("Press Space to start", (getWidth() - g2d.getFontMetrics().stringWidth("Press Space to start")) / 2, 700);
    }

    private void drawGame(Graphics g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
    }

    private void drawTrack(Graphics g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD, 60));
        g2d.drawString("Track Level", (getWidth() - g2d.getFontMetrics().stringWidth("Track Level")) / 2, 400);
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