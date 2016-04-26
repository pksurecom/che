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
package org.eclipse.che.plugin.docker.machine.cleaner;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;

/**
 * Job for periodically clean up inactive docker containers
 *
 * @author Alexander Andrienko
 */
@Singleton
public class DockerContainerCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DockerContainerCleaner.class);

    private final MachineManager               machineManager;
    private final DockerConnector              dockerConnector;
    private final DockerContainerNameGenerator nameGenerator;

    @Inject
    public DockerContainerCleaner(MachineManager machineManager,
                                  DockerConnector dockerConnector,
                                  DockerContainerNameGenerator nameGenerator) {
        this.machineManager = machineManager;
        this.dockerConnector = dockerConnector;
        this.nameGenerator = nameGenerator;
    }

    @ScheduleRate(periodParameterName = "machine.docker.unused_containers_cleanup_period_min",
                  initialDelayParameterName = "machine.docker.unused_containers_cleanup_period_min",
                  unit = TimeUnit.MINUTES)
    @Override
    public void run() {
        try {
            List<ContainerListEntry> allDockerContainers = dockerConnector.listContainers();
            List<MachineImpl> machines = machineManager.getMachines();

            List<ContainerListEntry> unUsedContainers = findUnUsedContainers(allDockerContainers, machines);

            for (ContainerListEntry container : unUsedContainers) {
                killContainer(container);
                removeContainer(container);
            }
        } catch (IOException e) {
            LOG.error("Failed to get list docker containers", e);
        } catch (MachineException e) {
            LOG.error("Failed to get list machines to clean up unused containers", e);
        } catch (Exception e) {
            LOG.error("Failed to clean up inactive containers", e);
        }
    }

    private List<ContainerListEntry> findUnUsedContainers(List<ContainerListEntry> containers, List<MachineImpl> machines) {
        List<ContainerListEntry> unUsedContainers = new ArrayList<>();
        for (ContainerListEntry container : containers) {
            Optional<ContainerNameInfo> optional = nameGenerator.parse(container.getNames()[0]);
            if (!optional.isPresent()) {
                continue;
            }
            final ContainerNameInfo containerNameInfo = optional.get();
            boolean containerIsUsed = machines.stream()
                                              .anyMatch(machine -> machine.getId().equals(containerNameInfo.getMachineId())
                                                                   && machine.getWorkspaceId().equals(containerNameInfo.getWorkspaceId()));
            if (!containerIsUsed) {
                unUsedContainers.add(container);
            }
        }
        return unUsedContainers;
    }

    private void killContainer(ContainerListEntry container) {
        String containerId = container.getId();
        String containerName = container.getNames()[0];
        try {
            if (container.getStatus().startsWith("Up")) {
                dockerConnector.killContainer(containerId);
                LOG.warn("Container with 'id': '{}' and 'name': '{}' was killed ", containerId, containerName);
            }
        } catch (IOException e) {
            LOG.error("Failed to kill unused container with 'id': '{}' and 'name': '{}'", containerId, containerName, e);
        }
    }

    private void removeContainer(ContainerListEntry container) {
        String containerId = container.getId();
        String containerName = container.getNames()[0];
        try {
            //remove force with volumes
            dockerConnector.removeContainer(containerId, true, true);
            LOG.warn("Container with 'id': '{}' and 'name': '{}' was removed", containerId, containerName);
        } catch (IOException e) {
            LOG.error("Failed to delete unused container with 'id': '{}' and 'name': '{}'", containerId, containerName, e);
        }
    }
}
