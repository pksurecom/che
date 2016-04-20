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
package org.eclipse.che.plugin.docker.client.helper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;

/**
 * This class created for simple generation docker container query filter expression.
 *
 * @author Alexander Andrienko
 */
public class ContainersQueryFilter {//todo test but I think we will delete this class and will use {@link Filters} instead of
    private static final String QUERY_KEY = "filters";
    private static final Gson   GSON      = new Gson();

    private String[] exited;
    private String[] label;
    private String[] ancestor;
    private String[] before;
    private String[] since;
    private String[] volume;

    private Status[]    status;
    private Isolation[] isolations;

    public ContainersQueryFilter withExited(String... exited) {
        this.exited = exited;
        return this;
    }

    public ContainersQueryFilter withLabel(String... label) {
        this.label = label;
        return this;
    }

    public ContainersQueryFilter setAncestor(String... ancestor) {
        this.ancestor = ancestor;
        return this;
    }

    public ContainersQueryFilter setBefore(String... before) {
        this.before = before;
        return this;
    }

    public ContainersQueryFilter setSince(String... since) {
        this.since = since;
        return this;
    }

    public ContainersQueryFilter setVolume(String... volume) {
        this.volume = volume;
        return this;
    }

    public ContainersQueryFilter setStatus(Status... status) {
        this.status = status;
        return this;
    }

    public ContainersQueryFilter setIsolations(Isolation... isolations) {
        this.isolations = isolations;
        return this;
    }

    public String getQueryKey() {
        return QUERY_KEY;
    }

    public String toJson() throws UnsupportedEncodingException {
        return GSON.toJson(this);
    }

    public enum Status {
        @SerializedName("created") CREATED,
        @SerializedName("restarting") RESTARTING,
        @SerializedName("running") RUNNING,
        @SerializedName("paused") PAUSED,
        @SerializedName("exited") EXITED,
        @SerializedName("dead") DEAD;
    }

    public enum Isolation {
        @SerializedName("default") DEFAULT,
        @SerializedName("process") PROCESS,
        @SerializedName("hyperv") HYPERV;
    }
}
