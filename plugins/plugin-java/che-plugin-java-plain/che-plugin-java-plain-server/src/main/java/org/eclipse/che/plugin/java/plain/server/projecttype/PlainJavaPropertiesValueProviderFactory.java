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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

/**
 * {@link ValueProviderFactory} for Plain Java project type.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaPropertiesValueProviderFactory implements ValueProviderFactory {

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new JavaPropertiesValueProvider();
    }

    private class JavaPropertiesValueProvider implements ValueProvider {
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            //TODO need to determine source folders
            if (SOURCE_FOLDER.equals(attributeName)) {
                return singletonList(DEFAULT_SOURCE_FOLDER_VALUE);
            }
            return null;
        }
    }
}
