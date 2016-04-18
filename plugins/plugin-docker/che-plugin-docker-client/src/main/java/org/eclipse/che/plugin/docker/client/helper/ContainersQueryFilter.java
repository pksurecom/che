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

/**
 * This class created for simple generation docker container query filter expression.
 *
 * @author Alexander Andrienko
 */
public class ContainersQueryFilter {
    private static final String QUERY_KEY = "filters";
    private static final Gson   GSON      = new Gson();//todo static injection?

    private String[] exited;
    private String[] label;
    private String[] ancestor;
    private String[] before;
    private String[] since;
    private String[] volume;

    private Status[]    status;
    private Isolation[] isolations;

    public void setExited(String... exited) {
        this.exited = exited;
    }

    public void setLabel(String... label) {
        this.label = label;
    }

    public void setAncestor(String... ancestor) {
        this.ancestor = ancestor;
    }

    public void setBefore(String... before) {
        this.before = before;
    }

    public void setSince(String... since) {
        this.since = since;
    }

    public void setVolume(String... volume) {
        this.volume = volume;
    }

    public void setStatus(Status... status) {
        this.status = status;
    }

    public void setIsolations(Isolation... isolations) {
        this.isolations = isolations;
    }

    public String getQueryKey() {
        return QUERY_KEY;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public enum Status {
        CREATED("created"), RESTARTING("restarting"), RUNNING("running"), PAUSED("paused"), EXITED("exited"), DEAD("dead");

        private String status;

        Status(String status) {
            this.status = status;
        }

        public String getValue() {
            return status;
        }
    }

    public enum Isolation {
        DEFAULT("default"), PROCESS("process"), HYPERV("hyperv");

        private String value;

        Isolation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        ContainersQueryFilter containersQueryFilter = new ContainersQueryFilter();
        containersQueryFilter.setBefore(new String[]{"dfdfdf", "dfdfdfd"});
        containersQueryFilter.setExited(new String[]{"0", "255"});
        containersQueryFilter.setStatus(new Status[]{Status.CREATED, Status.RESTARTING});
        System.out.println(containersQueryFilter.toJson());
    }
}
