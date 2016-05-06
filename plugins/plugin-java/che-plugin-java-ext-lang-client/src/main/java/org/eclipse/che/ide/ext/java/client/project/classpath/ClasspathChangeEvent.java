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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;

import java.util.List;

/**
 * This event should be fired when classpath is changed.
 *
 * @author Valeriy Svydenko
 */
public class ClasspathChangeEvent extends GwtEvent<ClasspathChangeEvent.ClasspathChangeHandler> {

    /** Type class used to register this event. */
    public static Type<ClasspathChangeHandler> TYPE = new Type<>();

    private final List<ClasspathEntryDTO> entries;
    private final String                 projectPath;

    /**
     * Creates an event to initiate changing of classpath.
     *
     * @param projectPath
     *         path to the project
     * @param entries
     *         classpath entries
     */
    public ClasspathChangeEvent(String projectPath, List<ClasspathEntryDTO> entries) {
        this.projectPath = projectPath;
        this.entries = entries;
    }

    @Override
    public Type<ClasspathChangeHandler> getAssociatedType() {
        return TYPE;
    }

    /** Returns a path of the project. */
    public String getPath() {
        return projectPath;
    }

    /** Returns classpath entries. */
    public List<ClasspathEntryDTO> getEntries() {
        return entries;
    }

    @Override
    protected void dispatch(ClasspathChangeHandler handler) {
        handler.onClasspathChanged(this);
    }

    /**
     * Special handler which is called when classpath is changed
     */
    public interface ClasspathChangeHandler extends EventHandler {

        /**
         * Performs some actions when classpath is changed.
         *
         * @param event
         *         contains information about project which was selected
         */
        void onClasspathChanged(ClasspathChangeEvent event);
    }
}
