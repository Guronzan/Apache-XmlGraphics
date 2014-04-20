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

/* $Id: ClasspathResource.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import lombok.extern.slf4j.Slf4j;

/**
 * A class to find resources in the classpath by their mime-type specified in
 * the MANIFEST.
 * <p>
 * This class searches for content entries in all META-INF/MANIFEST.MF files. It
 * will find files with a given Content-Type: attribute. This allows to add
 * arbitrary resources by content-type just by creating a JAR wrapper and adding
 * them to the classpath.
 * <p>
 * Example:<br>
 *
 * <pre>
 * Name: test.txt
 * Content-Type: text/plain
 * </pre>
 */
@Slf4j
public final class ClasspathResource {

    /**
     * Actual Type: Map&lt;String,List&lt;URL&gt;&gt;.
     */
    private final Map<String, List<URL>> contentMappings;

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private static final String CONTENT_TYPE_KEY = "Content-Type";

    private static ClasspathResource classpathResource;

    private ClasspathResource() {
        this.contentMappings = new HashMap<>();
        loadManifests();
    }

    /**
     * Retrieve the singleton instance of this class.
     *
     * @return the ClassPathResource instance.
     */
    public static synchronized ClasspathResource getInstance() {
        if (classpathResource == null) {
            classpathResource = new ClasspathResource();
        }
        return classpathResource;
    }

    /* Actual return type: Set<ClassLoader> */
    private Set<ClassLoader> getClassLoadersForResources() {
        final Set<ClassLoader> v = new HashSet<>();
        try {
            final ClassLoader l = ClassLoader.getSystemClassLoader();
            if (l != null) {
                v.add(l);
            }
        } catch (final SecurityException e) {
            log.error("SecurityException", e);
            // Ignore
        }
        try {
            final ClassLoader l = Thread.currentThread()
                    .getContextClassLoader();
            if (l != null) {
                v.add(l);
            }
        } catch (final SecurityException e) {
            log.error("SecurityException", e);
            // Ignore
        }
        try {
            final ClassLoader l = ClasspathResource.class.getClassLoader();
            if (l != null) {
                v.add(l);
            }
        } catch (final SecurityException e) {
            log.error("SecurityException", e);
            // Ignore
        }
        return v;
    }

    private void loadManifests() {
        Enumeration<URL> e;
        try {

            final Iterator<ClassLoader> it = getClassLoadersForResources()
                    .iterator();
            while (it.hasNext()) {
                final ClassLoader classLoader = it.next();

                e = classLoader.getResources(MANIFEST_PATH);

                while (e.hasMoreElements()) {
                    final URL u = e.nextElement();
                    try {
                        final Manifest manifest = new Manifest(u.openStream());
                        final Map<String, Attributes> entries = manifest
                                .getEntries();
                        for (final Entry<String, Attributes> entry : entries
                                .entrySet()) {
                            final String name = entry.getKey();
                            final Attributes attributes = entry.getValue();
                            final String contentType = attributes
                                    .getValue(CONTENT_TYPE_KEY);
                            if (contentType != null) {
                                addToMapping(contentType, name, classLoader);
                            }
                        }
                    } catch (final IOException io) {
                        log.error("IOException", io);
                    }
                }
            }

        } catch (final IOException io) {
            log.error("IOException", io);
        }
    }

    private void addToMapping(final String contentType, final String name,
            final ClassLoader classLoader) {
        List<URL> existingFiles = this.contentMappings.get(contentType);
        if (existingFiles == null) {
            existingFiles = new ArrayList<>();
            this.contentMappings.put(contentType, existingFiles);
        }
        final URL url = classLoader.getResource(name);
        if (url != null) {
            existingFiles.add(url);
        }
    }

    /**
     * Retrieve a list of resources known to have the given mime-type.
     *
     * @param mimeType
     *            the mime-type to search for.
     * @return a List&lt;URL&gt;, guaranteed to be != null.
     */
    public List<URL> listResourcesOfMimeType(final String mimeType) {
        final List<URL> content = this.contentMappings.get(mimeType);
        if (content == null) {
            return Collections.emptyList();
        } else {
            return content;
        }
    }

}
