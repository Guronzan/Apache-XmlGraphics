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

/* $Id: DSCCommentPage.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.ps.dsc.events;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Represents a %%Page DSC comment.
 */
public class DSCCommentPage extends AbstractDSCComment {

    private String pageName;
    private int pagePosition = -1;

    /**
     * Creates a new instance.
     */
    public DSCCommentPage() {
    }

    /**
     * Creates a new instance.
     * 
     * @param pageName
     *            the name of the page
     * @param pagePosition
     *            the position of the page within the file (1-based)
     */
    public DSCCommentPage(final String pageName, final int pagePosition) {
        setPageName(pageName);
        setPagePosition(pagePosition);
    }

    /**
     * Creates a new instance. The page name will be set to the same value as
     * the page position.
     * 
     * @param pagePosition
     *            the position of the page within the file (1-based)
     */
    public DSCCommentPage(final int pagePosition) {
        this(Integer.toString(pagePosition), pagePosition);
    }

    /**
     * Returns the name of the page.
     * 
     * @return the page name
     */
    public String getPageName() {
        return this.pageName;
    }

    /**
     * Sets the page name.
     * 
     * @param name
     *            the page name
     */
    public void setPageName(final String name) {
        this.pageName = name;
    }

    /**
     * Returns the page position.
     * 
     * @return the page position (1-based)
     */
    public int getPagePosition() {
        return this.pagePosition;
    }

    /**
     * Sets the page position.
     * 
     * @param position
     *            the page position (1-based)
     */
    public void setPagePosition(final int position) {
        if (position <= 0) {
            throw new IllegalArgumentException("position must be 1 or above");
        }
        this.pagePosition = position;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    @Override
    public String getName() {
        return DSCConstants.PAGE;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#hasValues()
     */
    @Override
    public boolean hasValues() {
        return true;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#parseValue(java.lang.String)
     */
    @Override
    public void parseValue(final String value) {
        final List<String> params = splitParams(value);
        final Iterator<String> iter = params.iterator();
        this.pageName = iter.next();
        this.pagePosition = Integer.parseInt(iter.next());
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(org.apache.xmlgraphics.ps.PSGenerator)
     */
    @Override
    public void generate(final PSGenerator gen) throws IOException {
        gen.writeDSCComment(getName(), new Object[] { getPageName(),
                getPagePosition() });
    }

}
