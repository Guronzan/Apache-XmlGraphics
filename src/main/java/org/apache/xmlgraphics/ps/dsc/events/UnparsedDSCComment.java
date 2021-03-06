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

/* $Id: UnparsedDSCComment.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.ps.dsc.events;

import java.io.IOException;

import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Represents a DSC comment that is not parsed into one of the concrete
 * DSCComment subclasses. It is used whenever a DSC comment is encountered that
 * is unknown to the parser.
 *
 * @see org.apache.xmlgraphics.ps.dsc.DSCCommentFactory
 */
public class UnparsedDSCComment extends AbstractEvent implements DSCComment {

    private final String name;
    private String value;

    /**
     * Creates a new instance.
     *
     * @param name
     *            the name of the DSC comment
     */
    public UnparsedDSCComment(final String name) {
        this.name = name;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#hasValues()
     */
    @Override
    public boolean hasValues() {
        return this.value != null;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#isAtend()
     */
    @Override
    public boolean isAtend() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#parseValue(java.lang.String)
     */
    @Override
    public void parseValue(final String value) {
        this.value = value;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(org.apache.xmlgraphics.ps.PSGenerator)
     */
    @Override
    public void generate(final PSGenerator gen) throws IOException {
        gen.writeln("%%" + this.name + (hasValues() ? ": " + this.value : ""));
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#isDSCComment()
     */
    @Override
    public boolean isDSCComment() {
        return true;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#getEventType()
     */
    @Override
    public int getEventType() {
        return DSC_COMMENT;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#asDSCComment()
     */
    @Override
    public DSCComment asDSCComment() {
        return this;
    }

}
