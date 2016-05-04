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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Mykola Morhun
 */
public class CreateExecParamsTest {

    private static final String   CONTAINER = "container";
    private static final boolean  DETACH    = false;
    private static final String[] CMD       = {"command", "arg1", "arg2"};

    private CreateExecParams createExecParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        createExecParams = CreateExecParams.from(CONTAINER, CMD);

        assertEquals(createExecParams.getContainer(), CONTAINER);
        assertEquals(createExecParams.getCmd(), CMD);

        assertNull(createExecParams.isDetach());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        createExecParams = CreateExecParams.from(CONTAINER, CMD)
                                           .withDetach(DETACH);

        assertEquals(createExecParams.getContainer(), CONTAINER);
        assertEquals(createExecParams.getCmd(), CMD);
        assertTrue(createExecParams.isDetach() == DETACH);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        createExecParams = CreateExecParams.from(null, CMD);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowAnNullPointerExceptionIfCmdRequiredParameterIsNull() {
        createExecParams = CreateExecParams.from(CONTAINER, null);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSetEmptyArray() {
        createExecParams = CreateExecParams.from(CONTAINER, CMD);

        String[] cmd = new String[0];
        createExecParams.withCmd(cmd);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSetEmptyCommand() {
        createExecParams = CreateExecParams.from(CONTAINER, CMD);

        String[] cmd = {"", "arg"};
        createExecParams.withCmd(cmd);
    }

    @Test
    public void detachParameterShouldEqualsNullIfItNotSet() {
        createExecParams = CreateExecParams.from(CONTAINER, CMD);

        assertNull(createExecParams.isDetach());
    }

}
