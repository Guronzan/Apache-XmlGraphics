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

/* $Id: ImagePreloaderTestCase.java 696964 2008-09-19 07:48:30Z jeremias $ */

package org.apache.xmlgraphics.image.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import junit.framework.TestCase;

import org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Tests for bundled Imagepreloader implementations.
 */
public class ImagePreloaderTestCase extends TestCase {

    private final MockImageContext imageContext = MockImageContext
            .getInstance();

    public ImagePreloaderTestCase(final String name) {
        super(name);
    }

    @Test
    public void testImageLoaderFactory() {
        final ImageManager manager = this.imageContext.getImageManager();
        final ImageInfo info = new ImageInfo(null, MimeConstants.MIME_PNG);
        final ImageLoaderFactory ilf = manager.getRegistry()
                .getImageLoaderFactory(info, ImageFlavor.BUFFERED_IMAGE);
        assertNotNull(ilf);
    }

    @Test
    public void testFileNotFound() throws ImageException, IOException {
        final String uri = "doesnotexistanywhere.png";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();
        try {
            manager.preloadImage(uri, sessionContext);
            fail("Expected a FileNotFoundException!");
        } catch (final FileNotFoundException e) {
            // expected!
        }
    }

    @Test
    public void testPNG() throws ImageException, IOException {
        final String uri = "asf-logo.png";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_PNG, info.getMimeType());
        assertEquals("asf-logo.png", info.getOriginalURI());
        assertEquals(169, info.getSize().getWidthPx());
        assertEquals(51, info.getSize().getHeightPx());
        assertEquals(96, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(126734, info.getSize().getWidthMpt());
        assertEquals(38245, info.getSize().getHeightMpt());
    }

    @Test
    public void testPNGNoResolution() throws ImageException, IOException {
        final String uri = "no-resolution.png";
        // This file contains a pHYs chunk but the resolution is set to zero.
        // Reported in Bugzilla #45789

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_PNG, info.getMimeType());
        assertEquals("no-resolution.png", info.getOriginalURI());
        assertEquals(51, info.getSize().getWidthPx());
        assertEquals(24, info.getSize().getHeightPx());
        // Without resolution information (or resolution=0), the default shall
        // be used
        assertEquals(72, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(51000, info.getSize().getWidthMpt());
        assertEquals(24000, info.getSize().getHeightMpt());
    }

    @Test
    public void testTIFF() throws ImageException, IOException {
        final String uri = "tiff_group4.tif";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_TIFF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(1560, info.getSize().getWidthPx());
        assertEquals(189, info.getSize().getHeightPx());
        assertEquals(204, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(550588, info.getSize().getWidthMpt());
        assertEquals(66706, info.getSize().getHeightMpt());
    }

    @Test
    public void testTIFFNoResolution() throws ImageException, IOException {
        final String uri = "no-resolution.tif";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_TIFF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(51, info.getSize().getWidthPx());
        assertEquals(24, info.getSize().getHeightPx());
        assertEquals(this.imageContext.getSourceResolution(), info.getSize()
                .getDpiHorizontal(), 0.1);
        assertEquals(51000, info.getSize().getWidthMpt());
        assertEquals(24000, info.getSize().getHeightMpt());
    }

    @Test
    public void testGIF() throws ImageException, IOException {
        final String uri = "bgimg72dpi.gif";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_GIF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(this.imageContext.getSourceResolution(), info.getSize()
                .getDpiHorizontal(), 0.1);
        assertEquals(192000, info.getSize().getWidthMpt());
        assertEquals(192000, info.getSize().getHeightMpt());
    }

    @Test
    public void testEMF() throws ImageException, IOException {
        final String uri = "img.emf";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/emf", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(76, info.getSize().getWidthPx());
        assertEquals(76, info.getSize().getHeightPx());
        assertEquals(96, info.getSize().getDpiHorizontal(), 1.0);
        assertEquals(56665, info.getSize().getWidthMpt());
        assertEquals(56665, info.getSize().getHeightMpt());
    }

    @Test
    public void testJPEG1() throws ImageException, IOException {
        final String uri = "bgimg300dpi.jpg";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_JPEG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(300, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(46080, info.getSize().getWidthMpt());
        assertEquals(46080, info.getSize().getHeightMpt());
    }

    @Test
    public void testJPEG2() throws ImageException, IOException {
        final String uri = "cmyk.jpg";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_JPEG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(160, info.getSize().getWidthPx());
        assertEquals(35, info.getSize().getHeightPx());
        assertEquals(72, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(160000, info.getSize().getWidthMpt());
        assertEquals(35000, info.getSize().getHeightMpt());
    }

    @Test
    public void testJPEG3() throws ImageException, IOException {
        final String uri = "cmyk-pxcm.jpg"; // Contains resolution as pixels per
        // centimeter

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_JPEG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(160, info.getSize().getWidthPx());
        assertEquals(35, info.getSize().getHeightPx());
        assertEquals(71.1, info.getSize().getDpiHorizontal(), 0.1); // 28 px/cm
        // = 71.1199
        // dpi
        assertEquals(161980, info.getSize().getWidthMpt());
        assertEquals(35433, info.getSize().getHeightMpt());
    }

    @Test
    public void testBMP() throws ImageException, IOException {
        final String uri = "bgimg300dpi.bmp";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/bmp", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(300, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(46092, info.getSize().getWidthMpt());
        assertEquals(46092, info.getSize().getHeightMpt());
    }

    @Test
    public void testBMPNoResolution() throws ImageException, IOException {
        final String uri = "no-resolution.bmp";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/bmp", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(50, info.getSize().getWidthPx());
        assertEquals(50, info.getSize().getHeightPx());
        assertEquals(this.imageContext.getSourceResolution(), info.getSize()
                .getDpiHorizontal(), 0.1);
        assertEquals(50000, info.getSize().getWidthMpt());
        assertEquals(50000, info.getSize().getHeightMpt());
    }

    @Test
    public void testEPSAscii() throws ImageException, IOException {
        final String uri = "barcode.eps";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_EPS, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(136, info.getSize().getWidthPx());
        assertEquals(43, info.getSize().getHeightPx());
        assertEquals(this.imageContext.getSourceResolution(), info.getSize()
                .getDpiHorizontal(), 0.1);
        assertEquals(135655, info.getSize().getWidthMpt());
        assertEquals(42525, info.getSize().getHeightMpt());
    }

    @Test
    public void testEPSBinary() throws ImageException, IOException {
        final String uri = "img-with-tiff-preview.eps";

        final ImageSessionContext sessionContext = this.imageContext
                .newSessionContext();
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_EPS, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(17, info.getSize().getWidthPx());
        assertEquals(17, info.getSize().getHeightPx());
        assertEquals(this.imageContext.getSourceResolution(), info.getSize()
                .getDpiHorizontal(), 0.1);
        assertEquals(17000, info.getSize().getWidthMpt());
        assertEquals(17000, info.getSize().getHeightMpt());
    }

    @Test
    public void testSAXSourceWithSystemID() throws ImageException, IOException {
        final URIResolver resolver = new URIResolver() {
            @Override
            public Source resolve(final String href, final String base)
                    throws TransformerException {
                if (href.startsWith("img:")) {
                    final String filename = href.substring(4);
                    final InputSource is = new InputSource(base + filename);
                    return new SAXSource(is);
                } else {
                    return null;
                }
            }
        };
        checkImageFound("img:asf-logo.png", resolver);
    }

    @Test
    public void testSAXSourceWithInputStream() throws ImageException,
            IOException {
        final URIResolver resolver = new URIResolver() {
            @Override
            public Source resolve(final String href, final String base)
                    throws TransformerException {
                if (href.startsWith("img:")) {
                    final String filename = href.substring(4);
                    InputSource is;
                    try {
                        is = new InputSource(new FileInputStream(new File(
                                MockImageSessionContext.IMAGE_BASE_DIR,
                                filename)));
                    } catch (final FileNotFoundException e) {
                        throw new TransformerException(e);
                    }
                    return new SAXSource(is);
                } else {
                    return null;
                }
            }
        };
        checkImageFound("img:asf-logo.png", resolver);
    }

    private void checkImageFound(final String uri, final URIResolver resolver)
            throws ImageException, IOException {
        final ImageSessionContext sessionContext = new SimpleURIResolverBasedImageSessionContext(
                this.imageContext, MockImageSessionContext.IMAGE_BASE_DIR,
                resolver);
        final ImageManager manager = this.imageContext.getImageManager();

        final ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(uri, info.getOriginalURI());
    }

}
