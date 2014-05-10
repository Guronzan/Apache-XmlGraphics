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

/* $Id: Base64Test.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * This test validates that the Base64 encoder/decoders work properly.
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @version $Id: Base64Test.java 750418 2009-03-05 11:03:54Z vhennebert $
 */
@Slf4j
public class Base64Test extends TestCase {

    private void innerBase64Test(final String action, final URL inputIn,
            final URL inputRef) throws IOException {
        URL ref = inputRef;
        final URL in = inputIn;
        InputStream inIS = in.openStream();

        if (action.equals("ROUND")) {
            ref = in;
        } else if (!action.equals("ENCODE") && !action.equals("DECODE")) {
            fail("Bad action string");
        }

        try (final InputStream refIS = ref.openStream()) {

            if (action.equals("ENCODE") || action.equals("ROUND")) {
                // We need to encode the incomming data
                try (final PipedOutputStream pos = new PipedOutputStream()) {
                    try (final OutputStream os = new Base64EncodeStream(pos)) {

                        // Copy the input to the Base64 Encoder (in a seperate
                        // thread).
                        final Thread t = new StreamCopier(inIS, os);

                        // Read that from the piped output stream.
                        inIS = new PipedInputStream(pos);
                        t.start();
                    }

                    if (action.equals("DECODE") || action.equals("ROUND")) {
                        inIS = new Base64DecodeStream(inIS);
                    }

                    final boolean mismatch = compareStreams(inIS, refIS,
                            action.equals("ENCODE"));

                    if (!mismatch) {
                        fail("Wrong result");
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(inIS);
        }
    }

    private void innerBase64Test(final String action, final String in,
            final String ref) throws MalformedURLException, IOException {
        innerBase64Test(action, getClass().getResource(in), getClass()
                .getResource(ref));
    }

    private void innerBase64Test(final String in) throws MalformedURLException,
    IOException {
        innerBase64Test("ROUND", in, in);
    }

    private void testBase64Group(final String name)
            throws MalformedURLException, IOException {
        innerBase64Test("ENCODE", name, name + ".64");
        innerBase64Test("DECODE", name + ".64", name);
        innerBase64Test(name);
    }

    /**
     * This method will only throw exceptions if some aspect of the test's
     * internal operation fails.
     *
     * @throws IOException
     */
    @Test
    public void testBase64() throws IOException {
        log.info(new File(".").getCanonicalPath());
        testBase64Group("zeroByte");
        testBase64Group("oneByte");
        testBase64Group("twoByte");
        testBase64Group("threeByte");
        testBase64Group("fourByte");
        testBase64Group("tenByte");
        testBase64Group("small");
        testBase64Group("medium");
        innerBase64Test("DECODE", "medium.pc.64", "medium");
        innerBase64Test("large");
    }

    /**
     * Returns true if the contents of <tt>is1</tt> match the contents of
     * <tt>is2</tt>
     *
     * @throws IOException
     */
    public static boolean compareStreams(final InputStream i1,
            final InputStream i2, final boolean skipws) throws IOException {
        try (final ReadableByteChannel ch1 = Channels.newChannel(i1)) {
            try (final ReadableByteChannel ch2 = Channels.newChannel(i2)) {

                final ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
                final ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

                while (true) {

                    final int n1 = ch1.read(buf1);
                    final int n2 = ch2.read(buf2);

                    if (n1 == -1 || n2 == -1) {
                        return n1 == n2;
                    }

                    buf1.flip();
                    buf2.flip();

                    for (int i = 0; i < Math.min(n1, n2); ++i) {
                        if (buf1.get() != buf2.get()) {
                            return false;
                        }
                    }

                    buf1.compact();
                    buf2.compact();
                }
            }
        }
    }

    static class StreamCopier extends Thread {
        InputStream src;
        OutputStream dst;

        public StreamCopier(final InputStream src, final OutputStream dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public void run() {
            try {
                final byte[] data = new byte[1000];
                while (true) {
                    final int len = this.src.read(data, 0, data.length);
                    if (len == -1) {
                        break;
                    }

                    this.dst.write(data, 0, len);
                }
            } catch (final IOException ioe) {
                // Nothing
            }
            try {
                this.dst.close();
            } catch (final IOException ioe) {
                // Nothing
            }
        }
    }
}
