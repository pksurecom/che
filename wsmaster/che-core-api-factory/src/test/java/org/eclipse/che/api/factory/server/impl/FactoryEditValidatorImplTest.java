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
package org.eclipse.che.api.factory.server.impl;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FactoryEditValidator}
 * @author Florent Benoit
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryEditValidatorImplTest {

    @Mock
    private Factory factory;

    @InjectMocks
    private FactoryEditValidator factoryEditValidator = new FactoryEditValidatorImpl();

    /**
     * Check missing author data
     * @throws ApiException
     */
    @Test(expectedExceptions = ServerException.class)
    public void testNoAuthor() throws ApiException {
        setCurrentUser("");
        factoryEditValidator.validate(factory);
    }

    /**
     * Check when user is the same than the one than create the factory
     * @throws ApiException
     */
    @Test
    public void testUserIsTheAuthor() throws ApiException {
        String userId = "florent";
        setCurrentUser(userId);
        Author author = mock(Author.class);
        doReturn(author).when(factory)
                        .getCreator();
        doReturn(userId).when(author)
                        .getUserId();

        factoryEditValidator.validate(factory);
    }

    private void setCurrentUser(String userId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        EnvironmentContext.getCurrent().setUser(user);
    }
}
