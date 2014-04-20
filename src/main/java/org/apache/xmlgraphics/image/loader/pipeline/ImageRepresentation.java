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

/* $Id: ImageRepresentation.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.image.loader.pipeline;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.util.dijkstra.Vertex;

/**
 * This class represents a combination of MIME type and an image flavor. It is
 * used in conjunction with Dijkstra's algorithm to find and construct a
 * conversion pipeline for images.
 */
public class ImageRepresentation implements Vertex {

    private final ImageFlavor flavor;

    /**
     * Main constructor
     * 
     * @param flavor
     *            the image flavor
     */
    public ImageRepresentation(final ImageFlavor flavor) {
        if (flavor == null) {
            throw new NullPointerException("flavor must not be null");
        }
        this.flavor = flavor;
    }

    /**
     * Returns the image flavor.
     * 
     * @return the image flavor
     */
    public ImageFlavor getFlavor() {
        return this.flavor;
    }

    // /** {@inheritDoc} */
    // @Override
    // public boolean equals(final Object obj) {
    // return toString().equals(((ImageRepresentation) obj).toString());
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (this.flavor == null ? 0 : this.flavor.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageRepresentation other = (ImageRepresentation) obj;
        if (this.flavor == null) {
            if (other.flavor != null) {
                return false;
            }
        } else if (!toString().equals(other.toString())) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final Vertex obj) {
        return toString().compareTo(obj.toString());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getFlavor().toString();
    }

}
