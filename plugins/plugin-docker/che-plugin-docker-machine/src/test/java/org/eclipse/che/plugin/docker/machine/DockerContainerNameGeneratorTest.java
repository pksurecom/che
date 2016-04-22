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
import org.testng.annotations.DataProvider;
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
    private static final String USER_NAME    = "some_user";

    @InjectMocks
    private DockerContainerNameGenerator nameGenerator;

    @Test
    public void containerNameShouldBeGenerated() {
        String expectedResult = "workspacebbbx2ree3iykn8gc_machineic131ppamujngv6y_some_user_ws-machine";
        String actualResult = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void machineNameShouldBeReturnedByGeneratedContainerName() {
        String generatedName = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);

        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(generatedName);

        assertEquals(containerNameInfoParser.getMachineId(), MACHINE_ID);
        assertEquals(containerNameInfoParser.getWorkspaceId(), WORKSPACE_ID);
    }

    @DataProvider(name = "validContainerNames")
    public static Object[][] validContainerNames() {
        return new Object[][] {{"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_u_a"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_-_-"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j____"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__tfdfd_klk"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_tfdf_dKlk"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_a_"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__o_"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_tfdfdklk____"}};
    }

    @Test(dataProvider = "validContainerNames")
    public void testValidContainerNames(String containerName) {
        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(containerName);

        assertEquals(containerNameInfoParser.getMachineId(), "machineri6bxnoj5jq7ll9j");
        assertEquals(containerNameInfoParser.getWorkspaceId(), "workspacep2bivvctac5ciwoh");
    }

    @DataProvider(name = "invalidContainerNames")
    public static Object[][] inValidContainerNames() {
        return new Object[][] {{"Workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwohmachineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9juser-name_ws-machine-name"},
                               {"workspacep2bivvctac5ciwoh__machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"1orkspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh5_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j7_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac%ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctackciwoh_machineri6%xnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctackciwoh_machineri6o*noj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciWoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciw_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll_user-name_ws-machine-name"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_a"},
                               {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
                               {"/workspacep2bivvctac5ciwoh_"},
                               {"/pong"},
                               {"workspace"},
                               {"machine"},
                               {"/workspace"},
                               {"/machine"},
                               {"/workspace_machine"},
                               {"workspaceid"},
                               {"machineid"},
                               {"workspacerere_machinedfdf"},
                               {"/workspacerere_machinedfdf"},
                               {"someusercontainer"},
                               {"/machineri6bxnoj5jq7ll9j_workspacep2bivvctac5ciwoh_user-name_ws-machine-name"}};
    }

    @Test(dataProvider = "invalidContainerNames")
    public void testInvalidContainerNames(String containerName) {
        DockerContainerNameGenerator.ContainerNameInfo containerNameInfoParser = nameGenerator.parse(containerName);

        assertEquals(containerNameInfoParser, null);
    }
}

