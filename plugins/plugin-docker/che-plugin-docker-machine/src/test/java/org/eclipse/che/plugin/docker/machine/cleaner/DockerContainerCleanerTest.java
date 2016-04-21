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

import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerFromList;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DockerContainerCleaner}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerCleanerTest {

    private static final String machineId1 = "machineid1";

    private static final String workspaceId1 = "workspaceid1";

    private static final String imageName1 = "imageName1";
    private static final String imageName2 = "imageName2";

    private static final String containerId1 = "containerId1";
    private static final String containerId2 = "containerId2";

    private static final String EXITED_STATUS  = "exited";
    private static final String RUNNING_STATUS = "running";

    @Mock
    private MachineManager               machineManager;
    @Mock
    private DockerConnector              dockerConnector;
    @Mock
    private DockerContainerNameGenerator nameGenerator;

    @Mock
    private MachineImpl machineImpl1;

    @Mock
    private ContainerFromList container1;
    @Mock
    private ContainerFromList container2;

    @Mock
    private ContainerNameInfo containerNameInfo1;
    @Mock
    private ContainerNameInfo containerNameInfo2;

    @InjectMocks
    private DockerContainerCleaner cleaner;

    @BeforeMethod
    public void setUp() throws MachineException, IOException {
        when(machineManager.getMachines()).thenReturn(singletonList(machineImpl1));
        when(dockerConnector.listContainers(any())).thenReturn(new ContainerFromList[] {container1, container2});
        when(machineImpl1.getId()).thenReturn(machineId1);
        when(machineImpl1.getWorkspaceId()).thenReturn(workspaceId1);

        when(container1.getImage()).thenReturn(imageName1);
        when(container1.getStatus()).thenReturn(RUNNING_STATUS);
        when(container1.getId()).thenReturn(containerId1);

        when(container2.getImage()).thenReturn(imageName2);
        when(container2.getStatus()).thenReturn(RUNNING_STATUS);
        when(container2.getId()).thenReturn(containerId2);

        when(nameGenerator.parse(imageName1)).thenReturn(containerNameInfo1);
        when(nameGenerator.parse(imageName2)).thenReturn(containerNameInfo2);

        when(containerNameInfo1.getMachineId()).thenReturn(machineId1);
        when(containerNameInfo1.getWorkspaceId()).thenReturn(workspaceId1);
    }

    @Test
    public void dockerContainerShouldBeKilledAndRemoved() throws MachineException, IOException {
        cleaner.run();

        verify(dockerConnector).listContainers(any());
        verify(machineManager).getMachines();

        verify(container1).getImage();
        verify(container2).getImage();

        verify(nameGenerator).parse(imageName1);
        verify(nameGenerator).parse(imageName2);

        verify(containerNameInfo1).getMachineId();
        verify(containerNameInfo2).getMachineId();

        verify(container2, times(2)).getId();
        verify(container2).getStatus();
        verify(dockerConnector).killContainer(containerId2);
        verify(dockerConnector).removeContainer(containerId2, true, true);

        verify(dockerConnector, never()).killContainer(containerId1);
        verify(dockerConnector, never()).removeContainer(containerId1, true, true);
    }

    @Test
    public void dockerContainerShouldNotBeKilledButRemoved() throws IOException, MachineException {
        when(container2.getStatus()).thenReturn(EXITED_STATUS);
        cleaner.run();

        verify(dockerConnector, never()).killContainer(containerId2);
        verify(dockerConnector).removeContainer(containerId2, true, true);
    }

    @Test
    public void noneContainerShouldBeRemoved() throws IOException {
        when(nameGenerator.parse(imageName2)).thenReturn(null);

        cleaner.run();

        verify(dockerConnector, never()).killContainer(containerId1);
        verify(dockerConnector, never()).removeContainer(containerId1, true, true);

        verify(dockerConnector, never()).killContainer(containerId2);
        verify(dockerConnector, never()).removeContainer(containerId2, true, true);
    }
}