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

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test for {@link DockerContainerNameGenerator}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerNameGeneratorTest {
    private static final String WORKSPACE_ID = "workspacebbbx2ree3iykn8gc";
    private static final String MACHINE_NAME = "ws-machine";
    private static final String MACHINE_ID   = "machineic131ppamujngv6y";
    private static final String USER_NAME = "some_user";

    @InjectMocks
    private DockerContainerNameGenerator nameGenerator;

    @Test
    public void containerNameShouldBeGenerated() {
        String expectedResult = "workspacebbbx2ree3iykn8gc_machineic131ppamujngv6y_some_user_ws-machine";
        String actualResult = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);
        assertEquals(expectedResult.toLowerCase(), actualResult);
    }

    @Test
    public void machineNameShouldBeReturnedByGeneratedContainerName() {
        String generatedName = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);

        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(generatedName);

        assertEquals(containerNameInfoParser.getMachineId(), MACHINE_ID);
        assertEquals(containerNameInfoParser.getWorkspaceId(), WORKSPACE_ID);
    }

    @Test
    public void machineNameByImageNameShouldBeReturned() {
        String imageName = "eclipse-che/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name";

        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(imageName);

        assertEquals(containerNameInfoParser.getMachineId(), "machineri6bxnoj5jq7ll9j");
        assertEquals(containerNameInfoParser.getWorkspaceId(), "workspacep2bivvctac5ciwoh");
    }

    @Test
    public void machineNameByContainerNameShouldBeReturned() {
        String imageName = "workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name";

        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(imageName);

        assertEquals(containerNameInfoParser.getMachineId(), "machineri6bxnoj5jq7ll9j");
        assertEquals(containerNameInfoParser.getWorkspaceId(), "workspacep2bivvctac5ciwoh");
    }
}

