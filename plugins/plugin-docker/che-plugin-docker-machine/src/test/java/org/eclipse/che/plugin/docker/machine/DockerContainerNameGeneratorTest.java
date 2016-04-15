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

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
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

    @BeforeMethod
    public void setUp() {
        EnvironmentContext.getCurrent().setUser(User.ANONYMOUS);
    }

    @Test
    public void containerNameShouldBeGenerated() {
        String expectedResult = "anonymous_workspacebbbx2ree3iykn8gc_machineic131ppamujngv6y_ws-machine";
        String actualResult = DockerContainerNameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, MACHINE_NAME);
        assertEquals(expectedResult.toLowerCase(), actualResult);
    }

    @Test
    public void machineNameShouldBeReturnedByGeneratedContainerName() {
        String generatedName = DockerContainerNameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, MACHINE_NAME);

        assertEquals(DockerContainerNameGenerator.getMachineName(generatedName), MACHINE_NAME);
    }

    @Test
    public void machineNameByImageNameShouldBeReturned() {
        String imageName = "eclipse-che/user-name_workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_ws-machine-name";
        assertEquals(DockerContainerNameGenerator.getMachineName(imageName), "ws-machine-name");
    }

    @Test
    public void machineNameByContainerNameShouldBeReturned() {
        String imageName = "user-name_workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_ws-machine-name";
        assertEquals(DockerContainerNameGenerator.getMachineName(imageName), "ws-machine-name");
    }
}
