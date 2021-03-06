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

/* $Id: SimpleURIResolverBasedImageSessionContext.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.image.loader;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import lombok.extern.slf4j.Slf4j;

import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;

/**
 * ImageSessionContext which uses a URIResolver to resolve URIs.
 */
@Slf4j
public class SimpleURIResolverBasedImageSessionContext extends
        DefaultImageSessionContext {

    private final URIResolver resolver;

    /**
     * Main constructor
     * 
     * @param context
     *            the parent image context
     * @param baseDir
     *            the base directory
     * @param resolver
     *            the URI resolver
     */
    public SimpleURIResolverBasedImageSessionContext(
            final ImageContext context, final File baseDir,
            final URIResolver resolver) {
        super(context, baseDir);
        this.resolver = resolver;
    }

    /** {@inheritDoc} */
    @Override
    protected Source resolveURI(final String uri) {
        try {
            return this.resolver.resolve(uri, getBaseDir().toURI()
                    .toASCIIString());
        } catch (final TransformerException e) {
            log.error("Exception", e);
            return null;
        }
    }

}