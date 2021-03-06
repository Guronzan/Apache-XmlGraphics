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

/* $Id: MockTimeStampProvider.java 759144 2009-03-27 14:16:18Z jeremias $ */

package org.apache.xmlgraphics.image.loader.cache;

/**
 * Mock subclass of the TimeStampProvider.
 */
class MockTimeStampProvider extends TimeStampProvider {

    private long timestamp;

    public MockTimeStampProvider() {
        this(0);
    }

    public MockTimeStampProvider(final long timestamp) {
        setTimeStamp(timestamp);
    }

    public void setTimeStamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    /** {@inheritDoc} */
    @Override
    public long getTimeStamp() {
        return this.timestamp;
    }

}
