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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate docker container name in format:
 * userName + "_" + workspaceId + "_" + machineId +"_" + machineName
 *
 * @author Alexander Andrienko
 */
public class DockerContainerNameGenerator {
    private static final String WORKSPACE_ID_REGEX = "workspace[\\da-z]{16}";
    private static final String MACHINE_ID_REGEX   = "machine[\\da-z]{16}";
    private static final String REGEX              = "(.)+_" + WORKSPACE_ID_REGEX + '_' + MACHINE_ID_REGEX + "_(.)+";

    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile(REGEX);

    /**
     * Generate image name for docker container
     *
     * @param workspaceId
     *         unique workspace id
     * @param machineId
     *         unique machine id
     * @param machineName
     *         name of the workspace machine
     * @return
     */
    public static String generateContainerName(String workspaceId, String machineId, String machineName) {
        String userName = EnvironmentContext.getCurrent().getUser().getName();
        String containerName = userName + "_" + workspaceId + '_' + machineId + '_' + machineName;
        return containerName.toLowerCase();
    }

    /**
     * Return parsed machine name from {@code machineImageName}
     *
     * @param machineImageName
     *         machine image name
     */
    @Nullable
    public static String getMachineName(String machineImageName) {
        ContainerNameInfoParser containerNameInfo = parse(machineImageName);
        return containerNameInfo == null ? null : containerNameInfo.getMachineName();
    }

    private static ContainerNameInfoParser parse(String machineImageName) {
        int namespaceIndex = machineImageName.indexOf("/") + 1;
        String containerName = machineImageName.substring(namespaceIndex, machineImageName.length());

        Matcher matcher = CONTAINER_NAME_PATTERN.matcher(containerName);
        if (!matcher.matches()) {
            return null;
        }
        return new ContainerNameInfoParser(containerName);
    }

    private static class ContainerNameInfoParser {
        private static final Pattern PATTERN = Pattern.compile(WORKSPACE_ID_REGEX + "_" + MACHINE_ID_REGEX);

        private String workspaceId;
        private String machineId;
        private String userName;
        private String machineName;

        private ContainerNameInfoParser(String containerName) {
            Matcher workspaceIdMatcher = PATTERN.matcher(containerName);
            if (workspaceIdMatcher.find()) {
                int start = workspaceIdMatcher.start();
                int end = workspaceIdMatcher.end();

                userName = containerName.substring(0, start - 1);
                machineName = containerName.substring(end + 1, containerName.length());

                int separatorIndex = containerName.indexOf("_", start);
                workspaceId = containerName.substring(start, separatorIndex);
                machineId = containerName.substring(separatorIndex + 1, end);
            }
        }

        public String getMachineId() {
            return machineId;
        }

        public String getMachineName() {
            return machineName;
        }

        public String getUserName() {
            return userName;
        }

        public String getWorkspaceId() {
            return workspaceId;
        }
    }
}
