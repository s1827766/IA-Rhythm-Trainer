import javax.swing.ImageIcon;
import java.awt.Rectangle;

public class Player {
	private int x, y, width, height, dx, dy;
    public boolean up, down;
    private ImageIcon pic;

    public Player() {
        x = 0;
        y = 0;
        pic = new ImageIcon("bird.png");
        width = 100;
        height = 100;
        dx = 0;
        dy = 1;
        up = false;
        down = false;
    }

    public Player(int xV, int yV, int w, int h, int dx1, int dy1, ImageIcon p) {
        x = xV;
        y = yV;
        width = w;
        height = h;
        dx = dx1;
        dy = dy1;
        pic = p;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // public void setY(int yV) {
    //     y += yV;
    // }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return width;
    }

    public int getH() {
        return height;
    }

    public void setdy(int dy) {
        y += dy;
    }

    public ImageIcon getPic() {
        return pic;
    }

    public void setPic(ImageIcon pic) {
        this.pic = pic;
    }

    public boolean checkCollision(Rhythm rhythm) {
        Rectangle birdRect = new Rectangle(x, y, width, height);
        Rectangle beat = new Rectangle(rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());
        return birdRect.intersects(beat);
    }

    public boolean cheatCollision(Rhythm rhythm) {
        Rectangle birdRect = new Rectangle(x, y+20, width+270, height-40);
        Rectangle beat = new Rectangle(rhythm.getX(), rhythm.getY(), rhythm.getW(), rhythm.getH());
        return birdRect.intersects(beat);
    }

    public void setDy(int newdy) {
        dy=newdy;
    }

    public void move() {
        if (up) {
            y-=2;
        }
        else if (down) {
            y+=2;
        } else {
            
        }
    }

    public void setMoveup(boolean up) {
        this.up = up;
    }
    public void setMovedown(boolean down) {
        this.down = down;
    }


    

}