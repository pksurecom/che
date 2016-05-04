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

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#putResource(PutResourceParams)}.
 *
 * @author Mykola Morhun
 */
public class PutResourceParams {

    private String      container;
    private String      targetPath;
    private InputStream sourceStream;
    private Boolean     noOverwriteDirNonDir;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param container
     *         container id or name
     * @param targetPath
     *         info about this parameter see {@link #withTargetPath(String)}
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code container} or {@code targetPath} is null
     */
    public static PutResourceParams from(@NotNull String container, @NotNull String targetPath) {
        return new PutResourceParams().withContainer(container)
                                      .withTargetPath(targetPath);
    }

    private PutResourceParams() {}

    /**
     * Adds container to this parameters.
     *
     * @param container
     *         container id or name
     * @return this params instance
     * @throws NullPointerException
     *         if {@code container} is null
     */
    public PutResourceParams withContainer(@NotNull String container) {
        requireNonNull(container);
        this.container = container;
        return this;
    }

    /**
     * Adds path to a directory to extract archive into to this parameters.
     *
     * @param targetPath
     *         path to a directory in the container to extract the archive’s contents into. Required.
     *         If not an absolute path, it is relative to the container’s root directory. The path resource must exist.
     * @return this params instance
     * @throws NullPointerException
     *         if {@code targetPath} is null
     */
    public PutResourceParams withTargetPath(@NotNull String targetPath) {
        requireNonNull(targetPath);
        this.targetPath = targetPath;
        return this;
    }

    /**
     * Adds stream of files to this parameters.
     *
     * @param sourceStream
     *         stream of files from source container, must be obtained from another container
     *          using {@link org.eclipse.che.plugin.docker.client.DockerConnector#getResource(GetResourceParams)}
     * @return this params instance
     */
    public PutResourceParams withSourceStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
        return this;
    }

    /**
     * Adds allowing replace flag to this parameters.
     *
     * @param noOverwriteDirNonDir
     *         if {@code true} then it will be an error if unpacking the given content would cause
     *          an existing directory to be replaced with a non-directory and vice versa.
     * @return this params instance
     */
    public PutResourceParams withNoOverwriteDirNonDir(Boolean noOverwriteDirNonDir) {
        this.noOverwriteDirNonDir = noOverwriteDirNonDir;
        return this;
    }

    public String getContainer() {
        return container;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public InputStream getSourceStream() {
        return sourceStream;
    }

    public Boolean isNoOverwriteDirNonDir() {
        return noOverwriteDirNonDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutResourceParams that = (PutResourceParams)o;
        return Objects.equals(container, that.container) &&
               Objects.equals(targetPath, that.targetPath) &&
               Objects.equals(sourceStream, that.sourceStream) &&
               Objects.equals(noOverwriteDirNonDir, that.noOverwriteDirNonDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, targetPath, sourceStream, noOverwriteDirNonDir);
    }

}
