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

/* $Id: CommonURIResolver.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.util.uri;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import lombok.extern.slf4j.Slf4j;

import org.apache.xmlgraphics.util.Service;

/**
 * A URI Resolver which supports pluggable entities via the {@link Service}
 * mechanism.
 * <p>
 * This resolver will try all resolvers registered as an {@link URIResolver}
 * class. For proper operation, the registers URIResolvers must return null if
 * they cannot handle the given URI and fail fast.
 */
@Slf4j
public class CommonURIResolver implements URIResolver {

    private final List<URIResolver> uriResolvers = new LinkedList<>();

    private static final class DefaultInstanceHolder {
        private static final CommonURIResolver INSTANCE = new CommonURIResolver();
    }

    /**
     * Creates a new CommonURIResolver. Use this if you need support for
     * resolvers in the current context.
     *
     * @see CommonURIResolver#getDefaultURIResolver()
     */
    public CommonURIResolver() {
        final Iterator<URIResolver> iter = Service.providers(URIResolver.class);
        while (iter.hasNext()) {
            final URIResolver resolver = iter.next();
            register(resolver);
        }
    }

    /**
     * Retrieve the default resolver instance.
     *
     * @return the default resolver instance.
     */
    public static CommonURIResolver getDefaultURIResolver() {
        return DefaultInstanceHolder.INSTANCE;
    }

    /** {@inheritDoc} */
    @Override
    public Source resolve(final String href, final String base) {
        synchronized (this.uriResolvers) {
            final Iterator<URIResolver> it = this.uriResolvers.iterator();
            while (it.hasNext()) {
                final URIResolver currentResolver = it.next();
                try {
                    final Source result = currentResolver.resolve(href, base);
                    if (result != null) {
                        return result;
                    }
                } catch (final TransformerException e) {
                    log.error("TransformerException", e);
                    // Ignore.
                }
            }
        }
        return null;
    }

    /**
     * Register a given {@link URIResolver} while the software is running.
     *
     * @param uriResolver
     *            the resolver to register.
     */
    public void register(final URIResolver uriResolver) {
        synchronized (this.uriResolvers) {
            this.uriResolvers.add(uriResolver);
        }
    }

    /**
     * Unregister a given {@link URIResolver} while the software is running.
     *
     * @param uriResolver
     *            the resolver to unregister.
     */
    public void unregister(final URIResolver uriResolver) {
        synchronized (this.uriResolvers) {
            this.uriResolvers.remove(uriResolver);
        }
    }

}
