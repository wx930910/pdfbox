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

import java.awt.Color;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.TilingPaint;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Pattern color space is either a Tiling pattern or a Shading pattern.
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDPattern extends PDSpecialColorSpace
{
    /**
     * log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDPattern.class);
    
    private final Map<String, PDAbstractPattern> patterns;
    private PDColorSpace underlyingColorSpace;

    /**
     * Creates a new pattern color space.
     * 
     * @param patterns The pattern map.
     */
    public PDPattern(Map<String, PDAbstractPattern> patterns)
    {
        this.patterns = patterns;
    }

    /**
     * Creates a new uncolored tiling pattern color space.
     * 
     * @param patterns The pattern map.
     * @param colorSpace The underlying colorspace.
     */
    public PDPattern(Map<String, PDAbstractPattern> patterns, PDColorSpace colorSpace)
    {
        this.patterns = patterns;
        this.underlyingColorSpace = colorSpace;
    }

    @Override
    public String getName()
    {
        return COSName.PATTERN.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PDColor getInitialColor()
    {
        return PDColor.EMPTY_PATTERN;
    }

    @Override
    public float[] toRGB(float[] value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Paint toPaint(PDFRenderer renderer, PDColor color, Matrix substreamMatrix,
                         AffineTransform xform) throws IOException
    {
        PDAbstractPattern pattern = getPattern(color);
        if (pattern instanceof PDTilingPattern)
        {
            PDTilingPattern tilingPattern = (PDTilingPattern) pattern;
                        
            Matrix patternMatrix = tilingPattern.getMatrix();
            Matrix matrix;
            if (patternMatrix == null)
            {
                matrix = substreamMatrix;
            }
            else
            {
                matrix = patternMatrix.multiply(substreamMatrix);
            }
            
            if (tilingPattern.getPaintType() == PDTilingPattern.PAINT_COLORED)
            {
                // colored tiling pattern
                return new TilingPaint(renderer, tilingPattern, 
                        matrix, xform);
            }
            else
            {
                // uncolored tiling pattern
                return new TilingPaint(renderer, tilingPattern, underlyingColorSpace, color, 
                        matrix, xform);
            }
        }
        else
        {
            PDShadingPattern shadingPattern = (PDShadingPattern)pattern;
            PDShading shading = shadingPattern.getShading();
            if (shading == null)
            {
                LOG.error("shadingPattern is null, will be filled with transparency");
                return new Color(0,0,0,0);
            }
            Matrix patternMatrix = shadingPattern.getMatrix();
            if (patternMatrix == null)
            {
                return shading.toPaint(substreamMatrix);
            }
            return shading.toPaint(patternMatrix.multiply(substreamMatrix));
        }
    }

    /**
     * Returns the pattern for the given color.
     * 
     * @param color color containing a pattern name
     * 
     * @return pattern for the given color
     * 
     * @throws java.io.IOException if the pattern name was not found.
     */
    public final PDAbstractPattern getPattern(PDColor color) throws IOException
    {
      if (!patterns.containsKey(color.getPatternName()))
      {
        throw new IOException("pattern " + color.getPatternName() + " was not found");
      }
      return patterns.get(color.getPatternName());
    }

    @Override
    public String toString()
    {
        return "Pattern";
    }
}
