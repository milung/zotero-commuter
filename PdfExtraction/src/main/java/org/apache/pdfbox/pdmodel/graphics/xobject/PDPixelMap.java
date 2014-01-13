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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;



/**
 * This class contains a PixelMap Image.
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @version $Revision: 1.10 $
 */
public class PDPixelMap extends PDXObjectImage
{

    private static final String PNG = "png";

    /**
     * Standard constructor. Basically does nothing.
     * @param pdStream The stream that holds the pixel map.
     */
    public PDPixelMap(PDStream pdStream)
    {
        super(pdStream, PNG);
    }


    /**
     * DecodeParms is an optional parameter for filters.
     *
     * It is provided if any of the filters has nondefault parameters. If there
     * is only one filter it is a dictionary, if there are multiple filters it
     * is an array with an entry for each filter. An array entry can hold a null
     * value if only the default values are used or a dictionary with
     * parameters.
     *
     * @return The decoding parameters.
     *
     * @deprecated Use {@link org.apache.pdfbox.pdmodel.common.PDStream#getDecodeParms() } instead
     */
    public COSDictionary getDecodeParams()
    {
        COSBase decodeParms = getCOSStream().getDictionaryObject(COSName.DECODE_PARMS);
        if (decodeParms != null)
        {
            if (decodeParms instanceof COSDictionary)
            {
                return (COSDictionary) decodeParms;
            }
            else if (decodeParms instanceof COSArray)
            {
                // not implemented yet, which index should we use?
                return null;//(COSDictionary)((COSArray)decodeParms).get(0);
            }
            else
            {
                return null;
            }
        }
        return null;
    }

    /**
     * A code that selects the predictor algorithm.
     *
     * <ul>
     * <li>1 No prediction (the default value)
     * <li>2 TIFF Predictor 2
     * <li>10 PNG prediction (on encoding, PNG None on all rows)
     * <li>11 PNG prediction (on encoding, PNG Sub on all rows)
     * <li>12 PNG prediction (on encoding, PNG Up on all rows)
     * <li>13 PNG prediction (on encoding, PNG Average on all rows)
     * <li>14 PNG prediction (on encoding, PNG Path on all rows)
     * <li>15 PNG prediction (on encoding, PNG optimum)
     * </ul>
     *
     * Default value: 1.
     *
     * @return predictor algorithm code
     * 
     * @deprecated see {@link org.apache.pdfbox.filter.FlateFilter}
     * 
     */
    public int getPredictor()
    {
        COSDictionary decodeParms = getDecodeParams();
        if (decodeParms != null)
        {
            int i = decodeParms.getInt(COSName.PREDICTOR);
            if (i != -1)
            {
                return i;
            }
        }
        return 1;
    }
}
