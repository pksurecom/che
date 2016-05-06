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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangeEvent;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage of the classpath entries.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathContainer implements ClasspathChangeEvent.ClasspathChangeHandler {
    private final ClasspathServiceClient classpathServiceClient;

    private Map<String, Promise<List<ClasspathEntryDTO>>> classpathes;

    @Inject
    public ClasspathContainer(ClasspathServiceClient classpathServiceClient, EventBus eventBus) {
        this.classpathServiceClient = classpathServiceClient;

        classpathes = new HashMap<>();

        eventBus.addHandler(ClasspathChangeEvent.TYPE, this);
    }

    /**
     * Returns list of the classpath entries.
     * If the classpath already exist for this project - returns its otherwise gets classpath from the server.
     *
     * @param projectPath path to the project
     * @return list of the classpath entries
     */
    public Promise<List<ClasspathEntryDTO>> getClasspathEntries(String projectPath) {
        if (classpathes.containsKey(projectPath)) {
            return classpathes.get(projectPath);
        } else {
            Promise<List<ClasspathEntryDTO>> result = classpathServiceClient.getClasspath(projectPath);
            classpathes.put(projectPath, result);
            return result;
        }
    }

    @Override
    public void onClasspathChanged(ClasspathChangeEvent event) {
        classpathes.put(event.getPath(), Promises.resolve(event.getEntries()));
    }
}
