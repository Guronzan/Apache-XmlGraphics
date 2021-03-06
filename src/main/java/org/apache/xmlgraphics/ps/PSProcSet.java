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

/* $Id: PSProcSet.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.ps;

/**
 * PSResource subclass that represents a ProcSet resource.
 */
public class PSProcSet extends PSResource {

    private final float version;
    private final int revision;

    /**
     * Creates a new instance.
     * 
     * @param name
     *            name of the resource
     */
    public PSProcSet(final String name) {
        this(name, 1.0f, 0);
    }

    /**
     * Creates a new instance.
     * 
     * @param name
     *            name of the resource
     * @param version
     *            version of the resource
     * @param revision
     *            revision of the resource
     */
    public PSProcSet(final String name, final float version, final int revision) {
        super(TYPE_PROCSET, name);
        this.version = version;
        this.revision = revision;
    }

    /** @return the version */
    public float getVersion() {
        return this.version;
    }

    /** @return the revision */
    public int getRevision() {
        return this.revision;
    }

    /** @return the <resource> specification as defined in DSC v3.0 spec. */
    @Override
    public String getResourceSpecification() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getType()).append(" ")
                .append(PSGenerator.convertStringToDSC(getName()));
        sb.append(" ").append(PSGenerator.convertRealToDSC(getVersion()));
        sb.append(" ").append(Integer.toString(getRevision()));
        return sb.toString();
    }

}
