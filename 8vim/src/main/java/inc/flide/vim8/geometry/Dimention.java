package inc.flide.vim8.geometry;

public class Dimention {
    private int width;
    private int height;

    public Dimention() {
        this(0,0);
    }

    public Dimention(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
