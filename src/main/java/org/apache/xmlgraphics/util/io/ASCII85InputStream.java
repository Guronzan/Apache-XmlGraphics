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

/* $Id: ASCII85InputStream.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class applies a ASCII85 decoding to the stream.
 * <p>
 * The class is derived from InputStream instead of FilteredInputStream because
 * we can use the read(byte[], int, int) method from InputStream which simply
 * delegates to read(). This makes the implementation easier.
 * <p>
 * The filter is described in chapter 3.13.3 of the PostScript Language
 * Reference (third edition).
 *
 * @version $Id: ASCII85InputStream.java 1345683 2012-06-03 14:50:33Z gadams $
 */
public class ASCII85InputStream extends InputStream implements ASCII85Constants {

    private final InputStream in;
    private boolean eodReached = false;
    private final int[] b = new int[4]; // decoded
    private int bSize = 0;
    private int bIndex = 0;

    /** @see java.io.FilterInputStream **/
    public ASCII85InputStream(final InputStream in) {
        super();
        this.in = in;
    }

    /** @see java.io.FilterInputStream **/
    @Override
    public int read() throws IOException {
        // Check if we need to read the next tuple
        if (this.bIndex >= this.bSize) {
            if (this.eodReached) {
                return -1;
            }
            readNextTuple();
            if (this.bSize == 0) {
                if (!this.eodReached) {
                    throw new IllegalStateException("Internal error");
                }
                return -1;
            }
        }
        int result = this.b[this.bIndex];
        result = result < 0 ? 256 + result : result;
        this.bIndex++;
        return result;
    }

    private int filteredRead() throws IOException {
        int buf;
        while (true) {
            buf = this.in.read();
            switch (buf) {
            case 0: // null
            case 9: // tab
            case 10: // LF
            case 12: // FF
            case 13: // CR
            case 32: // space
                continue; // ignore
            case ZERO:
            case 126: // = EOD[0] = '~'
                return buf;
            default:
                if (buf >= START && buf <= END) {
                    return buf;
                } else {
                    throw new IOException("Illegal character detected: " + buf);
                }
            }
        }
    }

    private void handleEOD() throws IOException {
        final int buf = this.in.read();
        if (buf != EOD[1]) {
            throw new IOException("'>' expected after '~' (EOD)");
        }
        this.eodReached = true;
        this.bSize = 0;
        this.bIndex = 0;
    }

    private void readNextTuple() throws IOException {
        int buf;
        long tuple = 0;
        // Read ahead and check for special "z"
        buf = filteredRead();
        if (buf == ZERO) {
            java.util.Arrays.fill(this.b, 0);
            this.bSize = 4;
            this.bIndex = 0;
        } else if (buf == EOD[0]) {
            handleEOD();
        } else {
            int cIndex = 0;
            tuple = (buf - START) * POW85[cIndex];
            cIndex++;
            while (cIndex < 5) {
                buf = filteredRead();
                if (buf == EOD[0]) {
                    handleEOD();
                    break;
                } else if (buf == ZERO) {
                    // Violation 2
                    throw new IOException("Illegal 'z' within tuple");
                } else {
                    tuple += (buf - START) * POW85[cIndex];
                    cIndex++;
                }
            }
            final int cSize = cIndex;
            if (cSize == 1) {
                // Violation 3
                throw new IOException("Only one character in tuple");
            }
            // Handle optional, trailing, incomplete tuple
            while (cIndex < 5) {
                tuple += POW85[cIndex - 1];
                cIndex++;
            }
            if (tuple > (2L << 31) - 1) {
                // Violation 1
                throw new IOException("Illegal tuple (> 2^32 - 1)");
            }
            // Convert tuple
            this.b[0] = (byte) (tuple >> 24 & 0xFF);
            this.b[1] = (byte) (tuple >> 16 & 0xFF);
            this.b[2] = (byte) (tuple >> 8 & 0xFF);
            this.b[3] = (byte) (tuple & 0xFF);
            this.bSize = cSize - 1;
            this.bIndex = 0;
        }
    }

}
