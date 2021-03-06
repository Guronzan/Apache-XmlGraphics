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

/* $Id: ImageCacheStatistics.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.image.loader.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class that gathers statistical information about the image cache.
 */
public class ImageCacheStatistics implements ImageCacheListener {

    private int invalidHits;
    private int imageInfoCacheHits;
    private int imageInfoCacheMisses;
    private int imageCacheHits;
    private int imageCacheMisses;
    private Map<ImageKey, Integer> imageCacheHitMap;
    private Map<ImageKey, Integer> imageCacheMissMap;

    /**
     * Main constructor.
     * 
     * @param detailed
     *            true if the cache hits/misses for each Image instance should
     *            be recorded.
     */
    public ImageCacheStatistics(final boolean detailed) {
        if (detailed) {
            this.imageCacheHitMap = new HashMap<>();
            this.imageCacheMissMap = new HashMap<>();
        }
    }

    /**
     * Reset the gathered statistics information.
     */
    public void reset() {
        this.imageInfoCacheHits = 0;
        this.imageInfoCacheMisses = 0;
        this.invalidHits = 0;
    }

    /** {@inheritDoc} */
    @Override
    public void invalidHit(final String uri) {
        this.invalidHits++;
    }

    /** {@inheritDoc} */
    @Override
    public void cacheHitImageInfo(final String uri) {
        this.imageInfoCacheHits++;
    }

    /** {@inheritDoc} */
    @Override
    public void cacheMissImageInfo(final String uri) {
        this.imageInfoCacheMisses++;
    }

    private void increaseEntry(final Map<ImageKey, Integer> map,
            final ImageKey key) {
        Integer v = map.get(key);
        if (v == null) {
            v = 1;
        } else {
            v = v.intValue() + 1;
        }
        map.put(key, v);
    }

    /** {@inheritDoc} */
    @Override
    public void cacheHitImage(final ImageKey key) {
        this.imageCacheHits++;
        if (this.imageCacheHitMap != null) {
            increaseEntry(this.imageCacheHitMap, key);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void cacheMissImage(final ImageKey key) {
        this.imageCacheMisses++;
        if (this.imageCacheMissMap != null) {
            increaseEntry(this.imageCacheMissMap, key);
        }
    }

    /**
     * Returns the number of times an invalid URI is tried.
     * 
     * @return the number of times an invalid URI is tried.
     */
    public int getInvalidHits() {
        return this.invalidHits;
    }

    /**
     * Returns the number of cache hits for ImageInfo instances.
     * 
     * @return the number of cache hits for ImageInfo instances.
     */
    public int getImageInfoCacheHits() {
        return this.imageInfoCacheHits;
    }

    /**
     * Returns the number of cache misses for ImageInfo instances.
     * 
     * @return the number of cache misses for ImageInfo instances.
     */
    public int getImageInfoCacheMisses() {
        return this.imageInfoCacheMisses;
    }

    /**
     * Returns the number of cache hits for Image instances.
     * 
     * @return the number of cache hits for Image instances.
     */
    public int getImageCacheHits() {
        return this.imageCacheHits;
    }

    /**
     * Returns the number of cache misses for Image instances.
     * 
     * @return the number of cache misses for Image instances.
     */
    public int getImageCacheMisses() {
        return this.imageCacheMisses;
    }

    /**
     * Returns a Map<ImageKey, Integer> with the number of cache hits.
     * 
     * @return a Map<ImageKey, Integer> with the number of cache hits
     */
    public Map<ImageKey, Integer> getImageCacheHitMap() {
        return Collections.unmodifiableMap(this.imageCacheHitMap);
    }

    /**
     * Returns a Map<ImageKey, Integer> with the number of cache misses.
     * 
     * @return a Map<ImageKey, Integer> with the number of cache misses
     */
    public Map<ImageKey, Integer> getImageCacheMissMap() {
        return Collections.unmodifiableMap(this.imageCacheMissMap);
    }

}
