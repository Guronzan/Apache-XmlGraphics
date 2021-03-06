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

/* $Id: PropertyUtil.java 1345683 2012-06-03 14:50:33Z gadams $ */

package org.apache.xmlgraphics.image.codec.util;

import java.util.MissingResourceException;

import lombok.extern.slf4j.Slf4j;

import org.apache.xmlgraphics.util.i18n.LocalizableSupport;

@Slf4j
public final class PropertyUtil {

    private PropertyUtil() {
    }

    protected static final String RESOURCES = "org.apache.xmlgraphics.image.codec.Messages";

    protected static LocalizableSupport localizableSupport = new LocalizableSupport(
            RESOURCES, PropertyUtil.class.getClassLoader());

    public static String getString(final String key) {
        try {
            return localizableSupport.formatMessage(key, null);
        } catch (final MissingResourceException e) {
            log.error("MissingResourceException", e);
            return key;
        }
    }
}
