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
package org.eclipse.che.ide.ext.java.shared.dto.classpath;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about classpath of the project.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ClasspathEntry {
    int LIBRARY   = 1;
    int PROJECT   = 2;
    int SOURCE    = 3;
    int VARIABLE  = 4;
    int CONTAINER = 5;

    /** Returns type of the entry. */
    int getEntryKind();

    void setEntryKind(int kind);

    ClasspathEntry withEntryKind(int kind);

    /** Returns path to the entry. */
    String getPath();

    void setPath(String path);

    ClasspathEntry withPath(String path);

}
