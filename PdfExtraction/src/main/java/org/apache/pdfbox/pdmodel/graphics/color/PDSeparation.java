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


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

/**
 * This class represents a Separation color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDSeparation extends PDColorSpace
{
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDSeparation.class);

    /**
     * The name of this color space.
     */
    public static final String NAME = "Separation";


    /**
     * Constructor.
     */
    public PDSeparation()
    {
        array = new COSArray();
        array.add( COSName.SEPARATION );
        array.add( COSName.getPDFName( "" ) );
    }

    /**
     * Constructor.
     *
     * @param separation The array containing all separation information.
     */
    public PDSeparation( COSArray separation )
    {
        array = separation;
    }

    /**
     * This will return the name of the color space.  For a PDSeparation object
     * this will always return "Separation"
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * This will get the number of components that this color space is made up of.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return getAlternateColorSpace().getNumberOfComponents();
    }





    /**
     * This will get the alternate color space for this separation.
     *
     * @return The alternate color space.
     *
     * @throws IOException If there is an error getting the alternate color space.
     */
    public PDColorSpace getAlternateColorSpace() throws IOException
    {
        COSBase alternate = array.getObject( 2 );
        PDColorSpace cs = PDColorSpaceFactory.createColorSpace( alternate );
        return cs;
    }



    /**
     * This will get the tint transform function.
     *
     * @return The tint transform function.
     *
     * @throws IOException If there is an error creating the PDFunction
     */
    public PDFunction getTintTransform() throws IOException
    {
        return PDFunction.create( array.getObject( 3 ) );
    }




}
