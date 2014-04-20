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

/* $Id: PreloaderEMF.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.image.loader.impl;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * Image preloader for EMF images.
 */
public class PreloaderEMF extends AbstractImagePreloader {

    /** Length of the EMF header */
    protected static final int EMF_SIG_LENGTH = 88;

    /** offset to signature */
    private static final int SIGNATURE_OFFSET = 40;
    /** offset to width */
    private static final int WIDTH_OFFSET = 32;
    /** offset to horizontal resolution in pixel */
    private static final int HRES_PIXEL_OFFSET = 72;

    /** {@inheritDoc} */
    @Override
    public ImageInfo preloadImage(final String uri, final Source src,
            final ImageContext context) throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        try (ImageInputStream in = ImageUtil.needImageInputStream(src)) {
            final byte[] header = getHeader(in, EMF_SIG_LENGTH);
            final boolean supported = header[SIGNATURE_OFFSET + 0] == (byte) 0x20
                    && header[SIGNATURE_OFFSET + 1] == (byte) 0x45
                    && header[SIGNATURE_OFFSET + 2] == (byte) 0x4D
                    && header[SIGNATURE_OFFSET + 3] == (byte) 0x46;

            if (supported) {
                final ImageInfo info = new ImageInfo(uri, "image/emf");
                info.setSize(determineSize(in, context));
                return info;
            } else {
                return null;
            }
        }
    }

    private ImageSize determineSize(final ImageInputStream in,
            final ImageContext context) throws IOException {
        in.mark();
        final ByteOrder oldByteOrder = in.getByteOrder();
        try {
            final ImageSize size = new ImageSize();

            // BMP uses little endian notation!
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            // resolution
            in.skipBytes(WIDTH_OFFSET);
            int width = (int) in.readUnsignedInt();
            int height = (int) in.readUnsignedInt();

            in.skipBytes(HRES_PIXEL_OFFSET - WIDTH_OFFSET - 8);
            final long hresPixel = in.readUnsignedInt();
            final long vresPixel = in.readUnsignedInt();
            final long hresMM = in.readUnsignedInt();
            final long vresMM = in.readUnsignedInt();
            final double resHorz = hresPixel / UnitConv.mm2in(hresMM);
            final double resVert = vresPixel / UnitConv.mm2in(vresMM);
            size.setResolution(resHorz, resVert);

            width = (int) Math.round(UnitConv.mm2mpt(width / 100f));
            height = (int) Math.round(UnitConv.mm2mpt(height / 100f));
            size.setSizeInMillipoints(width, height);
            size.calcPixelsFromSize();

            return size;
        } finally {
            in.setByteOrder(oldByteOrder);
            in.reset();
        }
    }

}
