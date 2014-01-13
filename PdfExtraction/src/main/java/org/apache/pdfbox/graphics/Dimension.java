package org.apache.pdfbox.graphics;

public class Dimension
{
    private final int width, height;

    public Dimension(int x, int y)
    {
        this.width = x;
        this.height = y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
