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

import com.google.gwt.event.shared.EventBus;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.Event;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry;
import org.eclipse.che.plugin.java.plain.client.service.ClasspathUpdaterServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry.CONTAINER;
import static org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry.LIBRARY;
import static org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry.SOURCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ClasspathResolverTest {
    private static final String PATH_TO_PROJECT   = "/pкщоусе";
    private static final String PATH_TO_LIB1      = "/path_lib1";
    private static final String PATH_TO_LIB2      = "/path_lib1";
    private static final String PATH_TO_SOURCE    = "/path_source";
    private static final String PATH_TO_CONTAINER = "/path_container";

    @Mock
    private ClasspathUpdaterServiceClient classpathUpdater;
    @Mock
    private NotificationManager           notificationManager;
    @Mock
    private ProjectServiceClient          projectServiceClient;
    @Mock
    private EventBus                      eventBus;
    @Mock
    private AppContext                    appContext;
    @Mock
    private DtoFactory                    dtoFactory;

    @InjectMocks
    private ClasspathResolver         classpathResolver;
    @Mock
    private CurrentProject            currentProject;
    @Mock
    private ProjectConfigDto          projectConfig;
    @Mock
    private Promise<Void>             classpathPromise;
    @Mock
    private Promise<ProjectConfigDto> getProjectPromise;
    @Mock
    private PromiseError              promiseError;

    @Mock
    private ClasspathEntry entry;
    @Mock
    private ClasspathEntry lib1;
    @Mock
    private ClasspathEntry lib2;
    @Mock
    private ClasspathEntry source;
    @Mock
    private ClasspathEntry container;

    @Captor
    private ArgumentCaptor<Operation<Void>>             updateOperation;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>     updateOperationError;
    @Captor
    private ArgumentCaptor<Operation<ProjectConfigDto>> getProjectOperation;

    private List<ClasspathEntry> entries = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        entries.add(lib1);
        entries.add(source);
        entries.add(container);

        when(lib1.getEntryKind()).thenReturn(LIBRARY);
        when(source.getEntryKind()).thenReturn(SOURCE);
        when(container.getEntryKind()).thenReturn(CONTAINER);

        when(lib1.getPath()).thenReturn(PATH_TO_LIB1);
        when(lib2.getPath()).thenReturn(PATH_TO_LIB2);
        when(container.getPath()).thenReturn(PATH_TO_CONTAINER);
        when(source.getPath()).thenReturn(PATH_TO_SOURCE);
    }

    @Test
    public void classpathShouldBeResolved() throws Exception {
        classpathResolver.resolveClasspathEntries(entries);

        verify(lib1).getEntryKind();
        verify(lib1).getPath();
        verify(source).getEntryKind();
        verify(source).getPath();
        verify(container).getEntryKind();
        verify(container).getPath();
    }

    @Test
    public void classpathShouldBeUpdated() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(dtoFactory.createDto(ClasspathEntry.class)).thenReturn(entry);
        when(entry.withPath(PATH_TO_LIB1)).thenReturn(lib1);
        when(entry.withPath(PATH_TO_LIB2)).thenReturn(lib2);
        when(entry.withPath(PATH_TO_CONTAINER)).thenReturn(container);
        when(entry.withPath(PATH_TO_SOURCE)).thenReturn(source);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(projectConfig.getPath()).thenReturn(PATH_TO_PROJECT);
        when(appContext.getWorkspaceId()).thenReturn("ws_id");

        when(classpathUpdater.setRawClasspath(anyString(), anyObject())).thenReturn(classpathPromise);
        when(classpathPromise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(classpathPromise);
        when(projectServiceClient.getProject("ws_id", PATH_TO_PROJECT)).thenReturn(getProjectPromise);

        classpathResolver.resolveClasspathEntries(entries);
        classpathResolver.updateClasspath();

        verify(classpathPromise).then(updateOperation.capture());
        updateOperation.getValue().apply(null);

        verify(getProjectPromise).then(getProjectOperation.capture());
        getProjectOperation.getValue().apply(projectConfig);

        verify(eventBus).fireEvent(Matchers.<Event>anyObject());
    }

    @Test
    public void showErrorIfClasspathDoesNotUpdate() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(dtoFactory.createDto(ClasspathEntry.class)).thenReturn(entry);
        when(entry.withPath(PATH_TO_LIB1)).thenReturn(lib1);
        when(entry.withPath(PATH_TO_LIB2)).thenReturn(lib2);
        when(entry.withPath(PATH_TO_CONTAINER)).thenReturn(container);
        when(entry.withPath(PATH_TO_SOURCE)).thenReturn(source);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(projectConfig.getPath()).thenReturn(PATH_TO_PROJECT);
        when(promiseError.getMessage()).thenReturn("message");

        when(classpathUpdater.setRawClasspath(anyString(), anyObject())).thenReturn(classpathPromise);
        when(classpathPromise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(classpathPromise);
        when(classpathPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(classpathPromise);

        classpathResolver.resolveClasspathEntries(entries);
        classpathResolver.updateClasspath();

        verify(classpathPromise).catchError(updateOperationError.capture());
        updateOperationError.getValue().apply(promiseError);

        verify(notificationManager).notify("Problems with updating classpath", "message", FAIL, true);
    }

    @Test
    public void setOfLibsShouldBeReturned() throws Exception {
        classpathResolver.resolveClasspathEntries(entries);
        Set<String> libs = classpathResolver.getLibs();
        assertFalse(libs.isEmpty());
        assertEquals(1, libs.size());
        assertEquals(PATH_TO_LIB1, libs.iterator().next());
    }

    @Test
    public void setOfContainersShouldBeReturned() throws Exception {
        classpathResolver.resolveClasspathEntries(entries);
        Set<String> containers = classpathResolver.getContainers();
        assertFalse(containers.isEmpty());
        assertEquals(1, containers.size());
        assertEquals(PATH_TO_CONTAINER, containers.iterator().next());
    }

    @Test
    public void setOfSourcesShouldBeReturned() throws Exception {
        classpathResolver.resolveClasspathEntries(entries);
        Set<String> sources = classpathResolver.getSources();
        assertFalse(sources.isEmpty());
        assertEquals(1, sources.size());
        assertEquals(PATH_TO_SOURCE, sources.iterator().next());
    }

}
