import javax.swing.ImageIcon;

public class Rhythm {
    private int x, y, width, height, dx;
    private ImageIcon image;

    public Rhythm() {
        x = 0;
        y = 0;
        image = new ImageIcon("rhythm.png");
        width = 0;
        height = 0;
        dx = 0;
    }

    public Rhythm(int xV, int yV, int w, int h, ImageIcon p) {
        x = xV;
        y = yV;
        width = w;
        height = h;
        image = p;
    }

    public int getX() {
        return x;
    }

    public void setX(int xV) {
        x += xV;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return width;
    }

    public int getH() {
        return height;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        x += dx;
    }

    public ImageIcon getPic() {
        return image;
    }

}