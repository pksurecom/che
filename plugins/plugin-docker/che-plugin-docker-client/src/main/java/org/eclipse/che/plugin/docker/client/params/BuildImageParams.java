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

import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.plugin.docker.client.params.ParamsUtils.requireNonEmptyArray;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#buildImage(BuildImageParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class BuildImageParams {

    private String      repository;
    private AuthConfigs authConfigs;
    private Boolean     doForcePull;
    private Long        memoryLimit;
    private Long        memorySwapLimit;
    private List<File>  files;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param files
     *         info about this parameter see {@link #withFiles(File...)}
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code files} is null
     * @throws IllegalArgumentException
     *         if {@code files} is empty array
     *
     */
    public static BuildImageParams from(@NotNull File... files) {
        return new BuildImageParams().withFiles(files);
    }

    private BuildImageParams() {}

    /**
     * Adds repository to this parameters.
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @return this params instance
     */
    public BuildImageParams withRepository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Adds auth configuration to this parameters.
     *
     * @param authConfigs
     *         authentication configuration for registries
     * @return this params instance
     */
    public BuildImageParams withAuthConfigs(AuthConfigs authConfigs) {
        this.authConfigs = authConfigs;
        return this;
    }

    /**
     * Adds force flag to this parameters.
     *
     * @param doForcePull
     *         if {@code true} attempts to pull the image even if an older image exists locally
     * @return this params instance
     */
    public BuildImageParams withDoForcePull(boolean doForcePull) {
        this.doForcePull = doForcePull;
        return this;
    }

    /**
     * Adds RAM memory limit to this parameters.
     *
     * @param memoryLimit
     *         RAM memory limit for build in bytes
     * @return this params instance
     */
    public BuildImageParams withMemoryLimit(long memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    /**
     * Adds swap memory limit to this parameters.
     *
     * @param memorySwapLimit
     *         total memory in bytes (memory + swap), -1 to enable unlimited swap
     * @return this params instance
     */
    public BuildImageParams withMemorySwapLimit(long memorySwapLimit) {
        this.memorySwapLimit = memorySwapLimit;
        return this;
    }

    /**
     * Sets list of files for creation docker image.
     * One of them must be Dockerfile.
     *
     * @param files
     *         files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile).
     *         One of them must be Dockerfile.
     * @return this params instance
     * @throws NullPointerException
     *         if {@code files} is null
     * @throws IllegalArgumentException
     *         if {@code files} is empty array
     */
    public BuildImageParams withFiles(@NotNull File... files) {
        requireNonNull(files);
        requireNonEmptyArray(files);
        this.files = new ArrayList<>(files.length + 1);
        return addFiles(files);
    }

    /**
     * Adds files to the file list.
     * see {@link #withFiles(File...)}
     *
     * @param files
     *         files to add to image
     * @return this params instance
     * @throws NullPointerException
     *         if {@code files} is null
     */
    public BuildImageParams addFiles(@NotNull File... files) {
        requireNonNull(files);
        for (File file : files) {
            requireNonNull(file);
            this.files.add(file);
        }
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public AuthConfigs getAuthConfigs() {
        return authConfigs;
    }

    public Boolean isDoForcePull() {
        return doForcePull;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public Long getMemorySwapLimit() {
        return memorySwapLimit;
    }

    public List<File> getFiles() {
        return files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildImageParams that = (BuildImageParams)o;
        return Objects.equals(repository, that.repository) &&
               Objects.equals(authConfigs, that.authConfigs) &&
               Objects.equals(doForcePull, that.doForcePull) &&
               Objects.equals(memoryLimit, that.memoryLimit) &&
               Objects.equals(memorySwapLimit, that.memorySwapLimit) &&
               Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, authConfigs, doForcePull, memoryLimit, memorySwapLimit, files);
    }

}
