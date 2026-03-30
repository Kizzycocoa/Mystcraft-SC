package myst.synthetic.page.emblem;

public enum PageGlyphSlot {
    TOP(0, 0.50f, 0.22f),
    RIGHT(1, 0.78f, 0.50f),
    BOTTOM(2, 0.50f, 0.78f),
    LEFT(3, 0.22f, 0.50f);

    private final int index;
    private final float centerX;
    private final float centerY;

    PageGlyphSlot(int index, float centerX, float centerY) {
        this.index = index;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public int index() {
        return index;
    }

    public float centerX() {
        return centerX;
    }

    public float centerY() {
        return centerY;
    }

    public static PageGlyphSlot fromPoemIndex(int index) {
        return switch (index) {
            case 0 -> TOP;
            case 1 -> RIGHT;
            case 2 -> BOTTOM;
            default -> LEFT;
        };
    }
}