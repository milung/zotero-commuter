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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

/**
 * This class represents an Indexed color space.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDIndexed extends PDColorSpace
{

    /**
     * The name of this color space.
     */
    public static final String NAME = "Indexed";

    /**
     * The abbreviated name of this color space.
     */
    public static final String ABBREVIATED_NAME = "I";

    private COSArray array;

    private PDColorSpace baseColorspace = null;


    /**
     * Constructor, default DeviceRGB, hival 255.
     */
    public PDIndexed()
    {
        array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(255));
        array.add(org.apache.pdfbox.cos.COSNull.NULL);
    }

    /**
     * Constructor.
     * 
     * @param indexedArray The array containing the indexed parameters
     */
    public PDIndexed(COSArray indexedArray)
    {
        array = indexedArray;
    }

    /**
     * This will return the number of color components. This will return the number of color components in the base
     * color.
     * 
     * @return The number of components in this color space.
     * 
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return getBaseColorSpace().getNumberOfComponents();
    }

    /**
     * This will return the name of the color space.
     * 
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * This will get the color space that acts as the index for this color space.
     * 
     * @return The base color space.
     * 
     * @throws IOException If there is error creating the base color space.
     */
    public PDColorSpace getBaseColorSpace() throws IOException
    {
        if (baseColorspace == null)
        {
            COSBase base = array.getObject(1);
            baseColorspace = PDColorSpaceFactory.createColorSpace(base);
        }
        return baseColorspace;
    }



}
