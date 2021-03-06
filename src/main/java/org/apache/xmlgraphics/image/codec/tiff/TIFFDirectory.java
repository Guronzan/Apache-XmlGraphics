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

/* $Id: TIFFDirectory.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.image.codec.tiff;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.xmlgraphics.image.codec.util.PropertyUtil;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;

// CSOFF: ConstantName
// CSOFF: EmptyStatement
// CSOFF: InnerAssignment
// CSOFF: LocalVariableName
// CSOFF: MemberName
// CSOFF: MultipleVariableDeclarations
// CSOFF: NeedBraces
// CSOFF: ParameterName
// CSOFF: WhitespaceAround

/**
 * A class representing an Image File Directory (IFD) from a TIFF 6.0 stream.
 * The TIFF file format is described in more detail in the comments for the
 * TIFFDescriptor class.
 *
 * <p>
 * A TIFF IFD consists of a set of TIFFField tags. Methods are provided to query
 * the set of tags and to obtain the raw field array. In addition, convenience
 * methods are provided for acquiring the values of tags that contain a single
 * value that fits into a byte, int, long, float, or double.
 *
 * <p>
 * Every TIFF file is made up of one or more public IFDs that are joined in a
 * linked list, rooted in the file header. A file may also contain so-called
 * private IFDs that are referenced from tag data and do not appear in the main
 * list.
 *
 * <p>
 * <b> This class is not a committed part of the JAI API. It may be removed or
 * changed in future releases of JAI.</b>
 *
 * @see TIFFField
 * @version $Id: TIFFDirectory.java 1345683 2012-06-03 14:50:33Z gadams $
 */
@Slf4j
public class TIFFDirectory implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2007844835460959003L;

    /** A boolean storing the endianness of the stream. */
    private final boolean isBigEndian;

    /** The number of entries in the IFD. */
    private int numEntries;

    /** An array of TIFFFields. */
    private TIFFField[] fields;

    /** A Hashtable indexing the fields by tag number. */
    private final Map<Integer, Integer> fieldIndex = new HashMap<>();

    /** The offset of this IFD. */
    private long IFDOffset = 8;

    /** The offset of the next IFD. */
    private long nextIFDOffset = 0;

    private static boolean isValidEndianTag(final int endian) {
        return endian == 0x4949 || endian == 0x4d4d;
    }

    /**
     * Constructs a TIFFDirectory from a SeekableStream. The directory parameter
     * specifies which directory to read from the linked list present in the
     * stream; directory 0 is normally read but it is possible to store multiple
     * images in a single TIFF file by maintaing multiple directories.
     *
     * @param stream
     *            a SeekableStream to read from.
     * @param directory
     *            the index of the directory to read.
     */
    public TIFFDirectory(final SeekableStream stream, final int directory)
            throws IOException {

        final long global_save_offset = stream.getFilePointer();
        long ifd_offset;

        // Read the TIFF header
        stream.seek(0L);
        final int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory1"));
        }
        this.isBigEndian = endian == 0x4d4d;

        final int magic = readUnsignedShort(stream);
        if (magic != 42) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory2"));
        }

        // Get the initial ifd offset as an unsigned int (using a long)
        ifd_offset = readUnsignedInt(stream);

        for (int i = 0; i < directory; ++i) {
            if (ifd_offset == 0L) {
                throw new IllegalArgumentException(
                        PropertyUtil.getString("TIFFDirectory3"));
            }

            stream.seek(ifd_offset);
            final long entries = readUnsignedShort(stream);
            stream.skip(12 * entries);

            ifd_offset = readUnsignedInt(stream);
        }
        if (ifd_offset == 0L) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory3"));
        }

        stream.seek(ifd_offset);
        initialize(stream);
        stream.seek(global_save_offset);
    }

    /**
     * Constructs a TIFFDirectory by reading a SeekableStream. The ifd_offset
     * parameter specifies the stream offset from which to begin reading; this
     * mechanism is sometimes used to store private IFDs within a TIFF file that
     * are not part of the normal sequence of IFDs.
     *
     * @param stream
     *            a SeekableStream to read from.
     * @param ifd_offset
     *            the long byte offset of the directory.
     * @param directory
     *            the index of the directory to read beyond the one at the
     *            current stream offset; zero indicates the IFD at the current
     *            offset.
     */
    public TIFFDirectory(final SeekableStream stream, long ifd_offset,
            final int directory) throws IOException {

        final long global_save_offset = stream.getFilePointer();
        stream.seek(0L);
        final int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory1"));
        }
        this.isBigEndian = endian == 0x4d4d;

        // Seek to the first IFD.
        stream.seek(ifd_offset);

        // Seek to desired IFD if necessary.
        int dirNum = 0;
        while (dirNum < directory) {
            // Get the number of fields in the current IFD.
            final long numEntries = readUnsignedShort(stream);

            // Skip to the next IFD offset value field.
            stream.seek(ifd_offset + 12 * numEntries);

            // Read the offset to the next IFD beyond this one.
            ifd_offset = readUnsignedInt(stream);

            // Seek to the next IFD.
            stream.seek(ifd_offset);

            // Increment the directory.
            dirNum++;
        }

        initialize(stream);
        stream.seek(global_save_offset);
    }

    private static final int[] sizeOfType = { 0, // 0 = n/a
        1, // 1 = byte
        1, // 2 = ascii
        2, // 3 = short
        4, // 4 = long
        8, // 5 = rational
        1, // 6 = sbyte
        1, // 7 = undefined
        2, // 8 = sshort
        4, // 9 = slong
        8, // 10 = srational
        4, // 11 = float
        8 // 12 = double
    };

    private void initialize(final SeekableStream stream) throws IOException {
        long nextTagOffset;
        int i, j;

        this.IFDOffset = stream.getFilePointer();

        this.numEntries = readUnsignedShort(stream);
        this.fields = new TIFFField[this.numEntries];

        for (i = 0; i < this.numEntries; ++i) {
            final int tag = readUnsignedShort(stream);
            final int type = readUnsignedShort(stream);
            int count = (int) readUnsignedInt(stream);
            int value = 0;

            // The place to return to to read the next tag
            nextTagOffset = stream.getFilePointer() + 4;

            try {
                // If the tag data can't fit in 4 bytes, the next 4 bytes
                // contain the starting offset of the data
                if (count * sizeOfType[type] > 4) {
                    value = (int) readUnsignedInt(stream);
                    stream.seek(value);
                }
            } catch (final ArrayIndexOutOfBoundsException ae) {
                log.error("ArrayIndexOutOfBoundsException", ae);
                // log.error(tag + " " + "TIFFDirectory4"); TODO - log
                // this message
                // if the data type is unknown we should skip this TIFF Field
                stream.seek(nextTagOffset);
                continue;
            }

            this.fieldIndex.put(tag, i);
            Object obj = null;

            switch (type) {
            case TIFFField.TIFF_BYTE:
            case TIFFField.TIFF_SBYTE:
            case TIFFField.TIFF_UNDEFINED:
            case TIFFField.TIFF_ASCII:
                final byte[] bvalues = new byte[count];
                stream.readFully(bvalues, 0, count);

                if (type == TIFFField.TIFF_ASCII) {

                    // Can be multiple strings
                    int index = 0, prevIndex = 0;
                    final List<String> v = new ArrayList<>();

                    while (index < count) {

                        while (index < count && bvalues[index++] != 0) {
                            ;
                        }

                        // When we encountered zero, means one string has
                        // ended
                        v.add(new String(bvalues, prevIndex, index - prevIndex));
                        prevIndex = index;
                    }

                    count = v.size();
                    final String[] strings = new String[count];
                    v.toArray(strings);
                    obj = strings;
                } else {
                    obj = bvalues;
                }

                break;

            case TIFFField.TIFF_SHORT:
                final char[] cvalues = new char[count];
                for (j = 0; j < count; j++) {
                    cvalues[j] = (char) readUnsignedShort(stream);
                }
                obj = cvalues;
                break;

            case TIFFField.TIFF_LONG:
                final long[] lvalues = new long[count];
                for (j = 0; j < count; j++) {
                    lvalues[j] = readUnsignedInt(stream);
                }
                obj = lvalues;
                break;

            case TIFFField.TIFF_RATIONAL:
                final long[][] llvalues = new long[count][2];
                for (j = 0; j < count; j++) {
                    llvalues[j][0] = readUnsignedInt(stream);
                    llvalues[j][1] = readUnsignedInt(stream);
                }
                obj = llvalues;
                break;

            case TIFFField.TIFF_SSHORT:
                final short[] svalues = new short[count];
                for (j = 0; j < count; j++) {
                    svalues[j] = readShort(stream);
                }
                obj = svalues;
                break;

            case TIFFField.TIFF_SLONG:
                final int[] ivalues = new int[count];
                for (j = 0; j < count; j++) {
                    ivalues[j] = readInt(stream);
                }
                obj = ivalues;
                break;

            case TIFFField.TIFF_SRATIONAL:
                final int[][] iivalues = new int[count][2];
                for (j = 0; j < count; j++) {
                    iivalues[j][0] = readInt(stream);
                    iivalues[j][1] = readInt(stream);
                }
                obj = iivalues;
                break;

            case TIFFField.TIFF_FLOAT:
                final float[] fvalues = new float[count];
                for (j = 0; j < count; j++) {
                    fvalues[j] = readFloat(stream);
                }
                obj = fvalues;
                break;

            case TIFFField.TIFF_DOUBLE:
                final double[] dvalues = new double[count];
                for (j = 0; j < count; j++) {
                    dvalues[j] = readDouble(stream);
                }
                obj = dvalues;
                break;

            default:
                throw new RuntimeException(
                        PropertyUtil.getString("TIFFDirectory0"));
            }

            this.fields[i] = new TIFFField(tag, type, count, obj);
            stream.seek(nextTagOffset);
        }

        // Read the offset of the next IFD.
        this.nextIFDOffset = readUnsignedInt(stream);
    }

    /** Returns the number of directory entries. */
    public int getNumEntries() {
        return this.numEntries;
    }

    /**
     * Returns the value of a given tag as a TIFFField, or null if the tag is
     * not present.
     */
    public TIFFField getField(final int tag) {
        final Integer i = this.fieldIndex.get(tag);
        if (i == null) {
            return null;
        } else {
            return this.fields[i.intValue()];
        }
    }

    /**
     * Returns true if a tag appears in the directory.
     */
    public boolean isTagPresent(final int tag) {
        return this.fieldIndex.containsKey(tag);
    }

    /**
     * Returns an ordered array of ints indicating the tag values.
     */
    public int[] getTags() {
        final int[] tags = new int[this.fieldIndex.size()];
        int i = 0;
        for (final int key : this.fieldIndex.keySet()) {
            tags[i++] = ((Integer) key).intValue();
        }

        return tags;
    }

    /**
     * Returns an array of TIFFFields containing all the fields in this
     * directory.
     */
    public TIFFField[] getFields() {
        return this.fields;
    }

    /**
     * Returns the value of a particular index of a given tag as a byte. The
     * caller is responsible for ensuring that the tag is present and has type
     * TIFFField.TIFF_SBYTE, TIFF_BYTE, or TIFF_UNDEFINED.
     */
    public byte getFieldAsByte(final int tag, final int index) {
        final Integer i = this.fieldIndex.get(tag);
        final byte[] b = this.fields[i.intValue()].getAsBytes();
        return b[index];
    }

    /**
     * Returns the value of index 0 of a given tag as a byte. The caller is
     * responsible for ensuring that the tag is present and has type
     * TIFFField.TIFF_SBYTE, TIFF_BYTE, or TIFF_UNDEFINED.
     */
    public byte getFieldAsByte(final int tag) {
        return getFieldAsByte(tag, 0);
    }

    /**
     * Returns the value of a particular index of a given tag as a long. The
     * caller is responsible for ensuring that the tag is present and has type
     * TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT, TIFF_SSHORT,
     * TIFF_SLONG or TIFF_LONG.
     */
    public long getFieldAsLong(final int tag, final int index) {
        final Integer i = this.fieldIndex.get(tag);
        return this.fields[i.intValue()].getAsLong(index);
    }

    /**
     * Returns the value of index 0 of a given tag as a long. The caller is
     * responsible for ensuring that the tag is present and has type TIFF_BYTE,
     * TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT, TIFF_SSHORT, TIFF_SLONG or
     * TIFF_LONG.
     */
    public long getFieldAsLong(final int tag) {
        return getFieldAsLong(tag, 0);
    }

    /**
     * Returns the value of a particular index of a given tag as a float. The
     * caller is responsible for ensuring that the tag is present and has
     * numeric type (all but TIFF_UNDEFINED and TIFF_ASCII).
     */
    public float getFieldAsFloat(final int tag, final int index) {
        final Integer i = this.fieldIndex.get(tag);
        return this.fields[i.intValue()].getAsFloat(index);
    }

    /**
     * Returns the value of index 0 of a given tag as a float. The caller is
     * responsible for ensuring that the tag is present and has numeric type
     * (all but TIFF_UNDEFINED and TIFF_ASCII).
     */
    public float getFieldAsFloat(final int tag) {
        return getFieldAsFloat(tag, 0);
    }

    /**
     * Returns the value of a particular index of a given tag as a double. The
     * caller is responsible for ensuring that the tag is present and has
     * numeric type (all but TIFF_UNDEFINED and TIFF_ASCII).
     */
    public double getFieldAsDouble(final int tag, final int index) {
        final Integer i = this.fieldIndex.get(tag);
        return this.fields[i.intValue()].getAsDouble(index);
    }

    /**
     * Returns the value of index 0 of a given tag as a double. The caller is
     * responsible for ensuring that the tag is present and has numeric type
     * (all but TIFF_UNDEFINED and TIFF_ASCII).
     */
    public double getFieldAsDouble(final int tag) {
        return getFieldAsDouble(tag, 0);
    }

    // Methods to read primitive data types from the stream

    private short readShort(final SeekableStream stream) throws IOException {
        if (this.isBigEndian) {
            return stream.readShort();
        } else {
            return stream.readShortLE();
        }
    }

    private int readUnsignedShort(final SeekableStream stream)
            throws IOException {
        if (this.isBigEndian) {
            return stream.readUnsignedShort();
        } else {
            return stream.readUnsignedShortLE();
        }
    }

    private int readInt(final SeekableStream stream) throws IOException {
        if (this.isBigEndian) {
            return stream.readInt();
        } else {
            return stream.readIntLE();
        }
    }

    private long readUnsignedInt(final SeekableStream stream)
            throws IOException {
        if (this.isBigEndian) {
            return stream.readUnsignedInt();
        } else {
            return stream.readUnsignedIntLE();
        }
    }

    private float readFloat(final SeekableStream stream) throws IOException {
        if (this.isBigEndian) {
            return stream.readFloat();
        } else {
            return stream.readFloatLE();
        }
    }

    private double readDouble(final SeekableStream stream) throws IOException {
        if (this.isBigEndian) {
            return stream.readDouble();
        } else {
            return stream.readDoubleLE();
        }
    }

    private static int readUnsignedShort(final SeekableStream stream,
            final boolean isBigEndian) throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedShort();
        } else {
            return stream.readUnsignedShortLE();
        }
    }

    private static long readUnsignedInt(final SeekableStream stream,
            final boolean isBigEndian) throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedInt();
        } else {
            return stream.readUnsignedIntLE();
        }
    }

    // Utilities

    /**
     * Returns the number of image directories (subimages) stored in a given
     * TIFF file, represented by a <code>SeekableStream</code>.
     */
    public static int getNumDirectories(final SeekableStream stream)
            throws IOException {
        final long pointer = stream.getFilePointer(); // Save stream pointer

        stream.seek(0L);
        final int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory1"));
        }
        final boolean isBigEndian = endian == 0x4d4d;
        final int magic = readUnsignedShort(stream, isBigEndian);
        if (magic != 42) {
            throw new IllegalArgumentException(
                    PropertyUtil.getString("TIFFDirectory2"));
        }

        stream.seek(4L);
        long offset = readUnsignedInt(stream, isBigEndian);

        int numDirectories = 0;
        while (offset != 0L) {
            ++numDirectories;

            stream.seek(offset);
            final long entries = readUnsignedShort(stream, isBigEndian);
            stream.skip(12 * entries);
            offset = readUnsignedInt(stream, isBigEndian);
        }

        stream.seek(pointer); // Reset stream pointer
        return numDirectories;
    }

    /**
     * Returns a boolean indicating whether the byte order used in the the TIFF
     * file is big-endian. That is, whether the byte order is from the most
     * significant to the least significant.
     */
    public boolean isBigEndian() {
        return this.isBigEndian;
    }

    /**
     * Returns the offset of the IFD corresponding to this
     * <code>TIFFDirectory</code>.
     */
    public long getIFDOffset() {
        return this.IFDOffset;
    }

    /**
     * Returns the offset of the next IFD after the IFD corresponding to this
     * <code>TIFFDirectory</code>.
     */
    public long getNextIFDOffset() {
        return this.nextIFDOffset;
    }
}
