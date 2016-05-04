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

/**
 * Contains util methods for {@code *Params} classes.
 */
public class ParamsUtils {

    /**
     * Checks is given array non empty.<br/>
     * Throws {@link IllegalArgumentException} if array doesn't contain elements.
     *
     * @param array
     *         array for check
     * @throws IllegalArgumentException
     *         if given array is empty
     */
    public static void requireNonEmptyArray(Object[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException();
        }
    }

}
