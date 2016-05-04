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

import org.eclipse.che.plugin.docker.client.json.Filters;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonMap;

/**
 *
 * Test for {@link ListContainersParams}
 *
 * @author Alexander Andrienko
 */
public class ListContainersParamsTest {

    private static final String  TEXT    = "to be or not be";
    private static final Integer LIMIT   = 100;
    private static final Filters filters = new Filters().withFilter(TEXT, TEXT);

    private ListContainersParams listContainersParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        listContainersParams = ListContainersParams.from();

        assertNull(listContainersParams.isAll());
        assertNull(listContainersParams.isSize());
        assertNull(listContainersParams.getBefore());
        assertNull(listContainersParams.getSince());
        assertNull(listContainersParams.getLimit());
        assertNull(listContainersParams.getFilters());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSize(true)
                                                   .withSince(TEXT)
                                                   .withBefore(TEXT)
                                                   .withFilters(filters)
                                                   .withLimit(LIMIT);

        assertEquals(listContainersParams.isAll(), TRUE);
        assertEquals(listContainersParams.isSize(), TRUE);
        assertEquals(listContainersParams.getBefore(), TEXT);
        assertEquals(listContainersParams.getSince(), TEXT);
        assertEquals(listContainersParams.getLimit(), LIMIT);
        assertEquals(listContainersParams.getFilters().getFilters(), singletonMap(TEXT, singletonList(TEXT)));
    }

    @Test
    public void shouldCreateParamsObjectWithNullAllParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withSize(true)
                                                   .withSince(TEXT)
                                                   .withBefore(TEXT)
                                                   .withFilters(filters)
                                                   .withLimit(LIMIT);
        assertNull(listContainersParams.isAll(), null);
    }

    @Test
    public void shouldCreateParamsObjectWithNullSizeParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSince(TEXT)
                                                   .withBefore(TEXT)
                                                   .withFilters(filters)
                                                   .withLimit(LIMIT);
        assertNull(listContainersParams.isSize(), null);
    }

    @Test
    public void shouldCreateParamsObjectWithNullSinceParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSize(true)
                                                   .withBefore(TEXT)
                                                   .withFilters(filters)
                                                   .withLimit(LIMIT);
        assertNull(listContainersParams.getSince(), null);
    }

    @Test
    public void shouldCreateParamsObjectWithNullBeforeParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSize(true)
                                                   .withSince(TEXT)
                                                   .withFilters(filters)
                                                   .withLimit(LIMIT);
        assertNull(listContainersParams.getBefore(), null);
    }

    @Test
    public void shouldCreateParamsObjectWithNullLimitParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSize(true)
                                                   .withSince(TEXT)
                                                   .withBefore(TEXT)
                                                   .withFilters(filters);
        assertNull(listContainersParams.getLimit(), null);
    }

    @Test
    public void shouldCreateParamsObjectWithNullFiltersParameter() {
        listContainersParams = ListContainersParams.from()
                                                   .withAll(true)
                                                   .withSize(true)
                                                   .withSince(TEXT)
                                                   .withBefore(TEXT)
                                                   .withLimit(LIMIT);
        assertNull(listContainersParams.getFilters(), null);
    }
}
