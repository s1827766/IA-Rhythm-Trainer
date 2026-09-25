import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class Game extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private BufferedImage back;
    private int key;
    private char screen;

    public Game() {
        new Thread(this).start();
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        key = -1;
        screen = 'S';

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
        switch (screen) {

            case 'S':

                break;

            case 'G':

                g2d.clearRect(0, 0, getSize().width, getSize().height);

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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        key = e.getKeyCode();
        System.out.println(key);

        if (e.getKeyCode() == 0) {

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