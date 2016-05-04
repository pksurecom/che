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

/**
 * @author Mykola Morhun
 */
public class StartExecParamsTest {
    private static final String  EXEC_ID = "exec_id";
    private static final Boolean DETACH  = Boolean.FALSE;
    private static final Boolean TTY     = Boolean.TRUE;

    private StartExecParams startExecParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        startExecParams = StartExecParams.from(EXEC_ID);

        assertEquals(startExecParams.getExecId(), EXEC_ID);

        assertNull(startExecParams.isDetach());
        assertNull(startExecParams.isTty());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        startExecParams = StartExecParams.from(EXEC_ID)
                                         .withDetach(DETACH)
                                         .withTty(TTY);

        assertEquals(startExecParams.getExecId(), EXEC_ID);
        assertEquals(startExecParams.isDetach(), DETACH);
        assertEquals(startExecParams.isTty(), TTY);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfExecIdRequiredParameterIsNull() {
        startExecParams = StartExecParams.from(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfExecIdRequiredParameterResetWithNull() {
        startExecParams = StartExecParams.from(EXEC_ID)
                                         .withExecId(null);
    }

    @Test
    public void detachParameterShouldEqualsNullIfItNotSet() {
        startExecParams = StartExecParams.from(EXEC_ID)
                                         .withTty(TTY);

        assertNull(startExecParams.isDetach());
    }

    @Test
    public void ttyParameterShouldEqualsNullIfItNotSet() {
        startExecParams = StartExecParams.from(EXEC_ID)
                                         .withDetach(DETACH);

        assertNull(startExecParams.isTty());
    }

}
