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

/* $Id: ImageUtil.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.image.loader.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSource;
import org.xml.sax.InputSource;

/**
 * Helper and convenience methods for working with the image package.
 */
@Slf4j
public final class ImageUtil {

    private ImageUtil() {
    }

    /**
     * Returns the InputStream of a Source object.
     *
     * @param src
     *            the Source object
     * @return the InputStream (or null if there's not InputStream available)
     */
    public static InputStream getInputStream(final Source src) {
        if (src instanceof StreamSource) {
            return ((StreamSource) src).getInputStream();
        } else if (src instanceof ImageSource) {
            return new ImageInputStreamAdapter(
                    ((ImageSource) src).getImageInputStream());
        } else if (src instanceof SAXSource) {
            final InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                return is.getByteStream();
            }
        }
        return null;
    }

    /**
     * Returns the ImageInputStream of a Source object.
     *
     * @param src
     *            the Source object
     * @return the ImageInputStream (or null if there's not ImageInputStream
     *         available)
     */
    public static ImageInputStream getImageInputStream(final Source src) {
        if (src instanceof ImageSource) {
            return ((ImageSource) src).getImageInputStream();
        } else {
            return null;
        }
    }

    /**
     * Returns the InputStream of a Source object. This method throws an
     * IllegalArgumentException if there's no InputStream instance available
     * from the Source object.
     *
     * @param src
     *            the Source object
     * @return the InputStream
     */
    public static InputStream needInputStream(final Source src) {
        final InputStream in = getInputStream(src);
        if (in != null) {
            return in;
        } else {
            throw new IllegalArgumentException(
                    "Source must be a StreamSource with an InputStream"
                            + " or an ImageSource");
        }
    }

    /**
     * Returns the ImageInputStream of a Source object. This method throws an
     * IllegalArgumentException if there's no ImageInputStream instance
     * available from the Source object.
     *
     * @param src
     *            the Source object
     * @return the ImageInputStream
     */
    public static ImageInputStream needImageInputStream(final Source src) {
        if (src instanceof ImageSource) {
            final ImageSource isrc = (ImageSource) src;
            if (isrc.getImageInputStream() == null) {
                throw new IllegalArgumentException(
                        "ImageInputStream is null/cleared on ImageSource");
            }
            return isrc.getImageInputStream();
        } else {
            throw new IllegalArgumentException("Source must be an ImageSource");
        }
    }

    /**
     * Indicates whether the Source object has an InputStream instance.
     *
     * @param src
     *            the Source object
     * @return true if an InputStream is available
     * @throws IOException
     */
    public static boolean hasInputStream(final Source src) throws IOException {
        if (src instanceof StreamSource) {
            try (final InputStream in = ((StreamSource) src).getInputStream()) {
                return in != null;
            }
        } else if (src instanceof ImageSource) {
            return hasImageInputStream(src);
        } else if (src instanceof SAXSource) {
            final InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                return is.getByteStream() != null;
            }
        }
        return false;
    }

    /**
     * Indicates whether the Source object has a Reader instance.
     *
     * @param src
     *            the Source object
     * @return true if an Reader is available
     * @throws IOException
     */
    public static boolean hasReader(final Source src) throws IOException {
        if (src instanceof StreamSource) {
            try (final Reader reader = ((StreamSource) src).getReader()) {
                return reader != null;
            }
        } else if (src instanceof SAXSource) {
            final InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                return is.getCharacterStream() != null;
            }
        }
        return false;
    }

    /**
     * Indicates whether the Source object has an ImageInputStream instance.
     *
     * @param src
     *            the Source object
     * @return true if an ImageInputStream is available
     * @throws IOException
     */
    public static boolean hasImageInputStream(final Source src)
            throws IOException {
        if (src instanceof ImageSource) {
            try (final ImageInputStream in = ((ImageSource) src)
                    .getImageInputStream()) {
                if (in != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes any references to InputStreams or Readers from the given Source
     * to prohibit accidental/unwanted use by a component further downstream.
     *
     * @param src
     *            the Source object
     */
    public static void removeStreams(final Source src) {
        if (src instanceof ImageSource) {
            final ImageSource isrc = (ImageSource) src;
            isrc.setImageInputStream(null);
        } else if (src instanceof StreamSource) {
            final StreamSource ssrc = (StreamSource) src;
            ssrc.setInputStream(null);
            ssrc.setReader(null);
        } else if (src instanceof SAXSource) {
            final InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                is.setByteStream(null);
                is.setCharacterStream(null);
            }
        }
    }

    /**
     * Closes the InputStreams or ImageInputStreams of Source objects. Any
     * exception occurring while closing the stream is ignored.
     *
     * @param src
     *            the Source object
     */
    public static void closeQuietly(final Source src) {
        if (src == null) {
            return;
        } else if (src instanceof StreamSource) {
            final StreamSource streamSource = (StreamSource) src;
            IOUtils.closeQuietly(streamSource.getInputStream());
            streamSource.setInputStream(null);
            IOUtils.closeQuietly(streamSource.getReader());
            streamSource.setReader(null);
        } else if (src instanceof ImageSource) {
            final ImageSource imageSource = (ImageSource) src;
            if (imageSource.getImageInputStream() != null) {
                try {
                    imageSource.getImageInputStream().close();
                } catch (final IOException ioe) {
                    log.error("IOException", ioe);
                }
                imageSource.setImageInputStream(null);
            }
        } else if (src instanceof SAXSource) {
            final InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                IOUtils.closeQuietly(is.getByteStream());
                is.setByteStream(null);
                IOUtils.closeQuietly(is.getCharacterStream());
                is.setCharacterStream(null);
            }
        }
    }

    /**
     * Decorates an ImageInputStream so the flush*() methods are ignored and
     * have no effect. The decoration is implemented using a dynamic proxy.
     *
     * @param in
     *            the ImageInputStream
     * @return the decorated ImageInputStream
     */
    public static ImageInputStream ignoreFlushing(final ImageInputStream in) {
        return (ImageInputStream) Proxy.newProxyInstance(in.getClass()
                .getClassLoader(), new Class[] { ImageInputStream.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy,
                            final Method method, final Object[] args)
                            throws Throwable {
                        final String methodName = method.getName();
                        // Ignore calls to flush*()
                        if (!methodName.startsWith("flush")) {
                            try {
                                return method.invoke(in, args);
                            } catch (final InvocationTargetException ite) {
                                log.error("InvocationTargetException", ite);
                                throw ite.getCause();
                            }
                        } else {
                            return null;
                        }
                    }
                });
    }

    /**
     * GZIP header magic number bytes, like found in a gzipped files, which are
     * encoded in Intel format (i.&#x2e;e&#x2e; little indian).
     */
    private static final byte[] GZIP_MAGIC = { (byte) 0x1f, (byte) 0x8b };

    /**
     * Indicates whether an InputStream is GZIP compressed. The InputStream must
     * support mark()/reset().
     *
     * @param in
     *            the InputStream (must return true on markSupported())
     * @return true if the InputStream is GZIP compressed
     * @throws IOException
     *             in case of an I/O error
     */
    public static boolean isGZIPCompressed(final InputStream in)
            throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException(
                    "InputStream must support mark()!");
        }
        final byte[] data = new byte[2];
        in.mark(2);
        in.read(data);
        in.reset();
        return data[0] == GZIP_MAGIC[0] && data[1] == GZIP_MAGIC[1];
    }

    /**
     * Decorates an InputStream with a BufferedInputStream if it doesn't support
     * mark()/reset().
     *
     * @param in
     *            the InputStream
     * @return the decorated InputStream
     */
    public static InputStream decorateMarkSupported(final InputStream in) {
        if (in.markSupported()) {
            return in;
        } else {
            return new BufferedInputStream(in);
        }
    }

    /**
     * Automatically decorates an InputStream so it is buffered. Furthermore, it
     * makes sure it is decorated with a GZIPInputStream if the stream is GZIP
     * compressed.
     *
     * @param in
     *            the InputStream
     * @return the decorated InputStream
     * @throws IOException
     *             in case of an I/O error
     */
    public static InputStream autoDecorateInputStream(final InputStream in)
            throws IOException {
        final InputStream inValue = decorateMarkSupported(in);
        if (isGZIPCompressed(inValue)) {
            return new GZIPInputStream(inValue);
        }
        return inValue;
    }

    /**
     * Creates a new hint Map with values from the FOUserAgent.
     *
     * @param session
     *            the session context
     * @return a Map of hints
     */
    public static Map<Object, Object> getDefaultHints(
            final ImageSessionContext session) {
        final Map<Object, Object> hints = new HashMap<>();
        hints.put(ImageProcessingHints.SOURCE_RESOLUTION, new Float(session
                .getParentContext().getSourceResolution()));
        hints.put(ImageProcessingHints.TARGET_RESOLUTION,
                new Float(session.getTargetResolution()));
        hints.put(ImageProcessingHints.IMAGE_SESSION_CONTEXT, session);
        return hints;
    }

    private static final String PAGE_INDICATOR = "page=";

    /**
     * Extracts page index information from a URI. The expected pattern is
     * "page=x" where x is a non-negative integer number. The page index must be
     * specified as part of the URI fragment and is 1-based, i.e. the first page
     * is 1 but the the method returns a zero-based page index. An example:
     * <code>http://www.foo.bar/images/scan1.tif#page=4</code> (The method will
     * return 3.)
     * <p>
     * If no page index information is found in the URI or if the URI cannot be
     * parsed, the method returns null.
     *
     * @param uri
     *            the URI that should be inspected
     * @return the page index (0 is the first page) or null if there's no page
     *         index information in the URI
     */
    public static Integer getPageIndexFromURI(final String uri) {
        if (uri.indexOf('#') < 0) {
            return null;
        }
        try {
            final URI u = new URI(uri);
            final String fragment = u.getFragment();
            if (fragment != null) {
                int pos = fragment.indexOf(PAGE_INDICATOR);
                if (pos >= 0) {
                    pos += PAGE_INDICATOR.length();
                    final StringBuilder sb = new StringBuilder();
                    while (pos < fragment.length()) {
                        final char c = fragment.charAt(pos);
                        if (c >= '0' && c <= '9') {
                            sb.append(c);
                        } else {
                            break;
                        }
                        pos++;
                    }
                    if (sb.length() > 0) {
                        int pageIndex = Integer.parseInt(sb.toString()) - 1;
                        pageIndex = Math.max(0, pageIndex);
                        return pageIndex;
                    }
                }
            }
        } catch (final URISyntaxException e) {
            log.error("URISyntaxException", e);
            throw new IllegalArgumentException("URI is invalid: "
                    + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Extracts page index information from a URI. The expected pattern is
     * "page=x" where x is a non-negative integer number. The page index must be
     * specified as part of the URI fragment and is 1-based, i.e. the first page
     * is 1 but the the method returns a zero-based page index. An example:
     * <code>http://www.foo.bar/images/scan1.tif#page=4</code> (The method will
     * return 3.)
     * <p>
     * If no page index information is found in the URI, the method just returns
     * 0 which indicates the first page.
     *
     * @param uri
     *            the URI that should be inspected
     * @return the page index (0 is the first page)
     */
    public static int needPageIndexFromURI(final String uri) {
        final Integer res = getPageIndexFromURI(uri);
        if (res != null) {
            return res.intValue();
        } else {
            return 0;
        }
    }

}
