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

/* $Id: NamedColorProfileParserTest.java 1051421 2010-12-21 08:54:25Z jeremias $ */

package org.apache.xmlgraphics.java2d.color.profile;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;
import org.junit.Test;

/**
 * Tests the {@link NamedColorProfileParser}.
 */
public class NamedColorProfileParserTest extends TestCase {

    private static final String NCP_EXAMPLE_FILE = "ncp-example.icc";

    @Test
    public void testParser() throws IOException {
        final InputStream in = getClass().getResourceAsStream(NCP_EXAMPLE_FILE);
        assertNotNull(NCP_EXAMPLE_FILE + " is missing!", in);
        ICC_Profile iccProfile;
        try {
            iccProfile = ICC_Profile.getInstance(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        final NamedColorProfileParser parser = new NamedColorProfileParser();
        final NamedColorProfile ncp = parser.parseProfile(iccProfile);
        assertEquals("Named Color Profile Example", ncp.getProfileName());
        assertEquals("The Apache Software Foundation", ncp.getCopyright());
        assertEquals(RenderingIntent.PERCEPTUAL, ncp.getRenderingIntent());
        final NamedColorSpace[] namedColors = ncp.getNamedColors();
        assertEquals(2, namedColors.length);
        NamedColorSpace ncs;
        ncs = namedColors[0];
        assertEquals("Postgelb", ncs.getColorName());
        final float[] xyz = ncs.getXYZ();
        assertEquals(0.6763079f, xyz[0], 0.01f);
        assertEquals(0.6263507f, xyz[1], 0.01f);
        assertEquals(0.04217565f, xyz[2], 0.01f);

        ncs = namedColors[1];
        assertEquals("MyRed", ncs.getColorName());
    }

}
