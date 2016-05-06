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
package org.eclipse.che.ide.extension.machine.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;

import javax.validation.constraints.NotNull;

/**
 * Provides current project's path.
 * Path means full absolute path to project on the FS, e.g. /projects/project_name
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CurrentProjectPathProvider implements CommandPropertyValueProvider,
                                                   CurrentProjectChangedHandler,
                                                   WsAgentStateHandler {

    private static final String KEY = "${current.project.path}";

    private final AppContext appContext;

    private String value;

    @Inject
    public CurrentProjectPathProvider(EventBus eventBus, AppContext appContext) {
        this.appContext = appContext;
        value = "";

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, this);
    }

    @NotNull
    @Override
    public String getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Promise<String> getValue() {
        return Promises.resolve(value);
    }

    private void updateValue() {
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return;
        }
        value = appContext.getProjectsRoot() + currentProject.getProjectConfig().getPath();
    }

    @Override
    public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
        updateValue();
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        updateValue();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        value = "";
    }
}
