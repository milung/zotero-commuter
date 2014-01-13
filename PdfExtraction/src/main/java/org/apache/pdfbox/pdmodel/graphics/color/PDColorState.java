/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.color;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.graphics.Color;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;

/**
 * This class represents a color space and the color value for that colorspace.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDColorState implements Cloneable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDColorState.class);

    /**
     * The default color that can be set to replace all colors in { ICC_ColorSpace ICC color spaces}.
     *
     * @see #setIccOverrideColor(Color)
     */
    private static volatile Color iccOverrideColor = null;

    /**
     * Sets the default color to replace all colors in { ICC_ColorSpace ICC color spaces}. This will work around a
     * potential JVM crash caused by broken native ICC color manipulation code in the Sun class libraries.
     * <p>
     * The default override can be specified by setting the color code in
     * <code>org.apache.pdfbox.ICC_override_color</code> system property (see { Color#getColor(String)}. If this
     * system property is not specified, then the override is not enabled unless this method is explicitly called.
     * 
     * @param color ICC override color, or <code>null</code> to disable the override
     * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-511">PDFBOX-511</a>
     * @since Apache PDFBox 0.8.1
     */
    public static void setIccOverrideColor(Color color)
    {
        iccOverrideColor = color;
    }

    private PDColorSpace colorSpace = new PDDeviceGray();
    private COSArray colorSpaceValue = new COSArray();
    private PDPatternResources pattern = null;


    /**
     * Default constructor.
     * 
     */
    public PDColorState()
    {
        setColorSpaceValue(new float[] { 0 });
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDColorState retval = new PDColorState();
        retval.colorSpace = colorSpace;
        retval.colorSpaceValue.clear();
        retval.colorSpaceValue.addAll(colorSpaceValue);
        retval.setPattern(getPattern());
        return retval;
    }



    /**
     * Constructor with an existing color set. Default colorspace is PDDeviceGray.
     * 
     * @param csValues The color space values.
     */
    public PDColorState(COSArray csValues)
    {
        colorSpaceValue = csValues;
    }

    /**
     * This will get the current colorspace.
     * 
     * @return The current colorspace.
     */
    public PDColorSpace getColorSpace()
    {
        return colorSpace;
    }

    /**
     * This will set the current colorspace.
     * 
     * @param value The new colorspace.
     */
    public void setColorSpace(PDColorSpace value)
    {
        colorSpace = value;
        // Clear color cache and current pattern
        pattern = null;
    }



    /**
     * This will get the color space values. Either 1 for gray or 3 for RGB.
     * 
     * @return The colorspace values.
     */
    public COSArray getCOSColorSpaceValue()
    {
        return colorSpaceValue;
    }

    /**
     * This will update the colorspace values.
     * 
     * @param value The new colorspace values.
     */
    public void setColorSpaceValue(float[] value)
    {
        colorSpaceValue.setFloatArray(value);
        // Clear color cache and current pattern
        pattern = null;
    }

    /**
     * This will get the current pattern.
     * 
     * @return The current pattern.
     */
    public PDPatternResources getPattern()
    {
        return pattern;
    }

    /**
     * This will update the current pattern.
     * 
     * @param patternValue The new pattern.
     */
    public void setPattern(PDPatternResources patternValue)
    {
        pattern = patternValue;
    }

}
