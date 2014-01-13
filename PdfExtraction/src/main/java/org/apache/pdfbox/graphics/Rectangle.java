package org.apache.pdfbox.graphics;


public class Rectangle
{
    private final float rectX, rectY, width, height;

    public Rectangle(float x, float y, float width, float height)
    {
        this.rectX = x;
        this.rectY = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Dimension dimension)
    {
        this.rectX = 0;
        this.rectY = 0;
        this.width = dimension.getWidth();
        this.height = dimension.getHeight();
    }

    public boolean contains(float x, float y)
    {
         return isInInterval(x,rectX,width) && isInInterval(y,rectY,height);
    }

    private boolean isInInterval(float point, float start, float size)
    {
        return point >= start && point <= start+size;
    }
}
