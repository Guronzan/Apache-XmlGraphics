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

/* $Id$ */

package org.apache.xmlgraphics.image.loader.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.MockImageSessionContext;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.Test;

public class ImageLoaderRawPNGTestCase {

    private final ImageLoaderRawPNG ilrpng = new ImageLoaderRawPNG();

    @Test
    public void testGetUsagePenalty() {
        assertEquals(1000, this.ilrpng.getUsagePenalty());
    }

    @Test
    public void testLoadImageBadMime() throws ImageException, IOException {
        final ImageContext context = MockImageContext.newSafeInstance();
        final ImageSessionContext session = new MockImageSessionContext(context);
        final ImageInfo info = new ImageInfo("basn2c08.png",
                MimeConstants.MIME_JPEG);
        try {
            this.ilrpng.loadImage(info, null, session);
            fail("An exception should have been thrown above");
        } catch (final IllegalArgumentException e) {
            // do nothing; this was expected
        }
    }

    @Test
    public void testGetTargetFlavor() {
        assertEquals(ImageFlavor.RAW_PNG, this.ilrpng.getTargetFlavor());
    }

    @Test
    public void testLoadImageGoodMime() throws ImageException, IOException {
        final ImageContext context = MockImageContext.newSafeInstance();
        final ImageSessionContext session = new MockImageSessionContext(context);
        final ImageInfo info = new ImageInfo("basn2c08.png",
                MimeConstants.MIME_PNG);
        final Image im = this.ilrpng.loadImage(info, null, session);
        assertTrue(im instanceof ImageRawPNG);
    }

}
