import javax.swing.ImageIcon;
import java.awt.Color;

public class Rhythm {
    private int x, y, width, height, dy;
    private ImageIcon image;
    private Color color;

    public Rhythm() {
        x = 0;
        y = 0;
        image = new ImageIcon("rhythm.png");
        width = 0;
        height = 0;
        dy = 0;
        color = Color.WHITE;
    }

    public Rhythm(int xV, int yV, int w, int h, Color c) {
        x = xV;
        y = yV;
        width = w;
        height = h;
        color = c;
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

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        y += dy;
    }

    public ImageIcon getPic() {
        return image;
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color c) {
        this.color = c;
    }

}