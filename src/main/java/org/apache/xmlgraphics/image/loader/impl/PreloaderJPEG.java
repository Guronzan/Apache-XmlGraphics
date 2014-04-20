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

/* $Id: PreloaderJPEG.java 696949 2008-09-19 07:13:02Z jeremias $ */

package org.apache.xmlgraphics.image.loader.impl;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.util.MimeConstants;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * Image preloader for JPEG images.
 */
public class PreloaderJPEG extends AbstractImagePreloader implements
        JPEGConstants {

    private static final int JPG_SIG_LENGTH = 3;

    /**
     * {@inheritDoc}
     * 
     * @throws ImageException
     */
    @Override
    public ImageInfo preloadImage(final String uri, final Source src,
            final ImageContext context) throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        try (ImageInputStream in = ImageUtil.needImageInputStream(src)) {
            final byte[] header = getHeader(in, JPG_SIG_LENGTH);
            final boolean supported = header[0] == (byte) MARK
                    && header[1] == (byte) SOI && header[2] == (byte) MARK;

            if (supported) {
                final ImageInfo info = new ImageInfo(uri,
                        MimeConstants.MIME_JPEG);
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
        try {
            final ImageSize size = new ImageSize();
            final JPEGFile jpeg = new JPEGFile(in);

            // TODO Read resolution from EXIF if there's no APP0
            // (for example with JPEGs from digicams)
            while (true) {
                final int segID = jpeg.readMarkerSegment();
                // log.info("Segment: " + Integer.toHexString(segID));
                switch (segID) {
                case SOI:
                case NULL:
                    break;
                case APP0:
                    int reclen = jpeg.readSegmentLength();
                    in.skipBytes(7);
                    final int densityUnits = in.read();
                    final int xdensity = in.readUnsignedShort();
                    final int ydensity = in.readUnsignedShort();
                    if (densityUnits == 2) {
                        // dots per centimeter
                        size.setResolution(xdensity * UnitConv.IN2CM, ydensity
                                * UnitConv.IN2CM);
                    } else if (densityUnits == 1) {
                        // dots per inch
                        size.setResolution(xdensity, ydensity);
                    } else {
                        // resolution not specified
                        size.setResolution(context.getSourceResolution());
                    }
                    if (size.getWidthPx() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    in.skipBytes(reclen - 14);
                    break;
                case SOF0:
                case SOF1:
                case SOF2: // SOF2 and SOFA are only supported by PDF 1.3
                case SOFA:
                    reclen = jpeg.readSegmentLength();
                    in.skipBytes(1);
                    final int height = in.readUnsignedShort();
                    final int width = in.readUnsignedShort();
                    size.setSizeInPixels(width, height);
                    if (size.getDpiHorizontal() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    in.skipBytes(reclen - 7);
                    break;
                case SOS:
                case EOI:
                    // Break as early as possible (we don't want to read the
                    // whole file here)
                    if (size.getDpiHorizontal() == 0) {
                        size.setResolution(context.getSourceResolution());
                        size.calcSizeFromPixels();
                    }
                    return size;
                default:
                    jpeg.skipCurrentMarkerSegment();
                }
            }
        } finally {
            in.reset();
        }
    }

}
