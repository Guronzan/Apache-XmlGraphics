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

/* $Id: PNGImageWriter.java 1345751 2012-06-03 19:47:56Z gadams $ */

package org.apache.xmlgraphics.image.writer.internal;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.codec.png.PNGImageEncoder;
import org.apache.xmlgraphics.image.writer.AbstractImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;

/**
 * ImageWriter implementation that uses the internal PNG codec to write PNG
 * files.
 *
 * @version $Id: PNGImageWriter.java 1345751 2012-06-03 19:47:56Z gadams $
 */
public class PNGImageWriter extends AbstractImageWriter {

    /** {@inheritDoc} */
    @Override
    public void writeImage(final RenderedImage image, final OutputStream out)
            throws IOException {
        writeImage(image, out, null);
    }

    /** {@inheritDoc} */
    @Override
    public void writeImage(final RenderedImage image, final OutputStream out,
            final ImageWriterParams params) throws IOException {
        final PNGImageEncoder encoder = new PNGImageEncoder(out, null);
        encoder.encode(image);
    }

    /** {@inheritDoc} */
    @Override
    public String getMIMEType() {
        return "image/png";
    }
}
