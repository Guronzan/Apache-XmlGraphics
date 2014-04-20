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

/* $Id: MockImageSessionContext.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.image.loader;

import java.io.File;

import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;

/**
 * Mock implementation for testing.
 */
public class MockImageSessionContext extends DefaultImageSessionContext {

    public static final File IMAGE_BASE_DIR = new File("./test/images/");

    public MockImageSessionContext(final ImageContext context) {
        super(context, IMAGE_BASE_DIR);
    }

    /** {@inheritDoc} */
    @Override
    public float getTargetResolution() {
        return 300;
    }

}
