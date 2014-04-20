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

/* $Id: DSCCommentDocumentSuppliedResources.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.ps.dsc.events;

import java.util.Collection;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Represents a %%DocumentSuppliedResources DSC comment.
 */
public class DSCCommentDocumentSuppliedResources extends
        AbstractResourcesDSCComment {

    /**
     * Creates a new instance.
     */
    public DSCCommentDocumentSuppliedResources() {
        super();
    }

    /**
     * Creates a new instance.
     * 
     * @param resources
     *            a Collection of PSResource instances
     */
    public DSCCommentDocumentSuppliedResources(
            final Collection<PSResource> resources) {
        super(resources);
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    @Override
    public String getName() {
        return DSCConstants.DOCUMENT_SUPPLIED_RESOURCES;
    }

}
