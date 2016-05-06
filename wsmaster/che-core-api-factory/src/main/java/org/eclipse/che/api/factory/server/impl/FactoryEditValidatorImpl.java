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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Singleton;

import static java.lang.String.format;

/**
 * This validator ensures that a factory can be edited by a user that has the associated rights (author or account owner)
 *
 * @author Florent Benoit
 */
@Singleton
public class FactoryEditValidatorImpl implements FactoryEditValidator {

    /**
     * Validates given factory by checking the current user is granted to edit the factory
     *
     * @param factory
     *         factory object to validate
     * @throws ForbiddenException
     *         occurs if the current user is not granted to edit the factory
     * @throws ServerException
     *         when any server error occurs
     */
    @Override
    public void validate(Factory factory) throws ForbiddenException, ServerException {
        // ensure user has the correct permissions
        final String userId = EnvironmentContext.getCurrent().getUser().getId();
        // Checks if there is an author from the factory (It may be missing for some old factories)
        Author author = factory.getCreator();
        if (author == null || author.getUserId() == null) {
            throw new ServerException(format("Invalid factory without author stored. Please contact the support about the factory ID '%s'",
                                             factory.getId()));
        }
    }
}
