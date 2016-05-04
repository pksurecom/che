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

import org.eclipse.che.plugin.docker.client.MessageProcessor;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#startExec(StartExecParams, MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class StartExecParams {

    private String  execId;
    private Boolean detach;
    private Boolean tty;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param execId
     *          exec id
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code execId} is null
     */
    public static StartExecParams from(@NotNull String execId) {
        return new StartExecParams().withExecId(execId);
    }

    private StartExecParams() {}

    /**
     * Adds exec id to this parameters.
     *
     * @param execId
     *         exec id
     * @return this params instance
     * @throws NullPointerException
     *         if {@code execId} is null
     */
    public StartExecParams withExecId(@NotNull String execId) {
        requireNonNull(execId);
        this.execId = execId;
        return this;
    }

    /**
     * Adds detach flag to this parameters.
     *
     * @param detach
     *         If detach is {@code true}, API returns after starting the exec command.
     *         Otherwise, API sets up an interactive session with the exec command.
     * @return this params instance
     */
    public StartExecParams withDetach(boolean detach) {
        this.detach = detach;
        return this;
    }

    /**
     * Adds pseudo-tty flag to this parameters.
     *
     * @param tty
     *         if {@code true} then will be allocated a pseudo-TTY
     * @return this params instance
     */
    public StartExecParams withTty(boolean tty) {
        this.tty = tty;
        return this;
    }

    public String getExecId() {
        return execId;
    }

    public Boolean isDetach() {
        return detach;
    }

    public Boolean isTty() {
        return tty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartExecParams that = (StartExecParams)o;
        return Objects.equals(execId, that.execId) &&
               Objects.equals(detach, that.detach) &&
               Objects.equals(tty, that.tty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(execId, detach, tty);
    }

}
