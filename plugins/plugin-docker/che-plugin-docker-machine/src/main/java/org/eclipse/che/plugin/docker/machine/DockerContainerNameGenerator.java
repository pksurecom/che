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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.user.User;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for generation docker container name or parsing important information from docker container name.
 *
 * @author Alexander Andrienko
 */
public class DockerContainerNameGenerator {
    private static final String WORKSPACE_ID_REGEX      = "workspace[0-9a-z]{16}";
    private static final String MACHINE_ID_REGEX        = "machine[0-9a-z]{16}";

    private static final String  CONTAINER_NAME_REGEX   = "^" + WORKSPACE_ID_REGEX + '_' + MACHINE_ID_REGEX + "(_[a-z0-9_-]+){2}$";
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile(CONTAINER_NAME_REGEX);

    /**
     * Return generated name for docker container. Method generate name for docker container in format:
     * <br><p>workspaceId + "_" + machineId + "_" + userName +"_" + machineName</p>
     * Notice: if userName or machineName contains incorrect symbols for creation docker container, then we replace this
     * symbols to valid docker container name symbols.
     *
     * @param workspaceId
     *         unique workspace id, see more (@link WorkspaceConfig#getId)
     * @param machineId
     *         unique machine id, see more {@link Machine#getId()}
     * @param userName
     *         name of the user who is docker container owner, see more {@link User#getName()}
     * @param machineName
     *         name of the workspace machine, see more {@link MachineConfig#getName()}
     */
    public String generateContainerName(String workspaceId, String machineId, String userName, String machineName) {
        String containerName = workspaceId + '_' + machineId + '_' + userName + '_' + machineName;
        return containerName.toLowerCase().replaceAll("[^a-z0-9_-]+", "");
    }

    /**
     * Parse machine image name to get important identifier information about this container (like workspaceId, machineId).
     *
     * @param containerName
     *         name of the container
     * @return important information about container with {@code machineImageName}
     */
    public ContainerNameInfo parse(@Nullable String containerName) {
        containerName = containerName.replace("/", "");

        Matcher matcher = CONTAINER_NAME_PATTERN.matcher(containerName);
        if (!matcher.matches()) {
            return null;
        }
        return new ContainerNameInfo(containerName);
    }

    /**
     * Class contains information about docker container, which was parsed from docker container name.
     * Notice: This class doesn't parse information about userName or machineName,because we do not give guarantees
     * about the integrity this data(see more {@link #generateContainerName(String, String, String, String)})
     */
    public static class ContainerNameInfo {
        private static final Pattern PATTERN = Pattern.compile(WORKSPACE_ID_REGEX + "_" + MACHINE_ID_REGEX);

        private String workspaceId;
        private String machineId;

        public ContainerNameInfo(String containerName) {
            Matcher workspaceIdMatcher = PATTERN.matcher(containerName);
            if (workspaceIdMatcher.find()) {
                int end = workspaceIdMatcher.end();

                int separatorIndex = containerName.indexOf("_");
                workspaceId = containerName.substring(0, separatorIndex);
                machineId = containerName.substring(separatorIndex + 1, end);
            }
        }

        /**
         * Return machineId of the docker container.
         */
        public String getMachineId() {
            return machineId;
        }

        /**
         * Return workspaceId of the docker container.
         */
        public String getWorkspaceId() {
            return workspaceId;
        }

        @Override
        public String toString() {
            return "ContainerNameInfo{" +
                   "workspaceId='" + workspaceId + '\'' +
                   ", machineId='" + machineId + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ContainerNameInfo)) return false;
            final ContainerNameInfo other = (ContainerNameInfo)obj;
            return Objects.equals(workspaceId, other.workspaceId)
                   && Objects.equals(machineId, other.machineId);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(workspaceId);
            hash = 31 * hash + Objects.hashCode(machineId);
            return hash;
        }
    }
}

