/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.params;

import org.eclipse.che.plugin.docker.client.json.Filters;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#listContainers(ListContainersParams)}.
 *
 * @author Alexander Andrienko
 */
public class ListContainersParams {
    private boolean all;
    private int limit;
    private  String since;
    private String before;
    private boolean size;
    private Filters filters;

    public ListContainersParams withAll(boolean all) {
        this.all = all;
        return this;
    }

    public ListContainersParams withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public ListContainersParams withSince(String since) {
        this.since = since;
        return this;
    }

    public ListContainersParams withBefore(String before) {
        this.before = before;
        return this;
    }

    public ListContainersParams withSize(boolean size) {
        this.size = size;
        return this;
    }

    public ListContainersParams withFilters(Filters filters) {
        this.filters = filters;
        return this;
    }

    public boolean isAll() {
        return all;
    }

    public int getLimit() {
        return limit;
    }

    public String getSince() {
        return since;
    }

    public String getBefore() {
        return before;
    }

    public boolean isSize() {
        return size;
    }

    public Filters getFilters() {
        return filters;
    }
}
