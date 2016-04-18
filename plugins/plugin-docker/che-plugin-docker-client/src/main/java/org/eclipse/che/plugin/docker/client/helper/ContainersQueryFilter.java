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

/**
 * This class created for simple generation docker container query filter expression.
 *
 * @author Alexander Andrienko
 */
public class ContainersQueryFilter {
    private static final String KEY = "filters";

    private static final String LABEL  = "label";
    private static final String STATUS = "status";
    private static final String EXITED = "exited";
    private static final String ISOLATION = "isolation";

    private ContainerStatus[] statuses;
    private String[]         labels;
    private Integer[]        exited;

    public void setStatus(ContainerStatus... statuses) {
        this.statuses = statuses;
    }

    public void setExitedCode(Integer... exited) {
        this.exited = exited;
    }

    public void setLabels(String... labels) {
        this.labels = labels;
    }

    public String getKey() {
        return KEY;
    }

    public String getValue() {

        return null;
    }

    public enum ContainerStatus {
        CREATED("created"), RESTARTING("restarting"), RUNNING("running"), PAUSED("paused"), EXITED("exited");

        private String status;

        ContainerStatus(String status) {
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
}
