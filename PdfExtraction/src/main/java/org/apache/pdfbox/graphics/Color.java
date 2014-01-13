package org.apache.pdfbox.graphics;


import java.util.Arrays;

/**
 * Created by sk1u00e5 on 10.1.2014.
 */
public class Color
{
    public static final Color BLACK = new Color(0f,0f,0f);

    private float[] rgb = new float[3];

    public Color(float red, float green, float blue)
    {
        rgb[0] = red;
        rgb[1] = green;
        rgb[2] = blue;
    }

    public Color(int rgb)
    {

    }


    public float[] getRGBColorComponents(float[] o)
    {
        return Arrays.copyOf(rgb,3);
    }
}
