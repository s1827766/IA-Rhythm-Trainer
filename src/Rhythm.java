import java.awt.Color;

public class Rhythm {
    private int x, y, width, height, dy;
    private Color color;
    private double targetSeconds;
    private boolean fading, hit;

    public Rhythm() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        dy = 0;
        color = Color.WHITE;
    }

    public Rhythm(int xV, int yV, int w, int h, Color c, double targetSeconds) {
        x = xV;
        y = yV;
        width = w;
        height = h;
        color = c;
        this.targetSeconds = targetSeconds;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public double getTargetSeconds() {
        return targetSeconds;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isFading() {
        return fading;
    }

    public void setFading(boolean fading) {
        this.fading = fading;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }
}