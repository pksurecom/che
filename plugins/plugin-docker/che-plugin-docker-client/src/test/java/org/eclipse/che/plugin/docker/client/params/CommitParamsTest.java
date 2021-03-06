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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Mykola Morhun
 */
public class CommitParamsTest {

    private static final String CONTAINER  = "container";
    private static final String REPOSITORY = "repository";
    private static final String TAG        = "tag";
    private static final String COMMENT    = "comment";
    private static final String AUTHOR     = "author";

    private CommitParams commitParams;

    @BeforeMethod
    private void prepare() {
        commitParams = CommitParams.from(CONTAINER, REPOSITORY);
    }

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        commitParams = CommitParams.from(CONTAINER, REPOSITORY);

        assertEquals(commitParams.getContainer(), CONTAINER);
        assertEquals(commitParams.getRepository(), REPOSITORY);

        assertNull(commitParams.getTag());
        assertNull(commitParams.getComment());
        assertNull(commitParams.getAuthor());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        commitParams = CommitParams.from(CONTAINER, REPOSITORY)
                                                .withTag(TAG)
                                                .withComment(COMMENT)
                                                .withAuthor(AUTHOR);

        assertEquals(commitParams.getContainer(), CONTAINER);
        assertEquals(commitParams.getRepository(), REPOSITORY);
        assertEquals(commitParams.getTag(), TAG);
        assertEquals(commitParams.getComment(), COMMENT);
        assertEquals(commitParams.getAuthor(), AUTHOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        commitParams = CommitParams.from(null, REPOSITORY);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterIsNull() {
        commitParams = CommitParams.from(CONTAINER, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        commitParams.withContainer(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterResetWithNull() {
        commitParams.withRepository(null);
    }

    @Test
    public void tagParameterShouldEqualsNullIfItNotSet() {
       commitParams.withAuthor(AUTHOR)
                   .withComment(COMMENT);

        assertNull(commitParams.getTag());
    }

    @Test
    public void commentParameterShouldEqualsNullIfItNotSet() {
        commitParams.withTag(TAG)
                    .withAuthor(AUTHOR);

        assertNull(commitParams.getComment());
    }

    @Test
    public void authorParameterShouldEqualsNullIfItNotSet() {
        commitParams.withTag(TAG)
                    .withComment(COMMENT);

        assertNull(commitParams.getAuthor());
    }

}
