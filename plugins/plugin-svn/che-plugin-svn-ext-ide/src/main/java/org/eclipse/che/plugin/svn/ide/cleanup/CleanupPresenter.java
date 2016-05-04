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
package org.eclipse.che.plugin.svn.ide.cleanup;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.PathTypeFilter;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.CleanupAction} action.
 */
@Singleton
public class CleanupPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    protected CleanupPresenter(final AppContext appContext,
                               final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               final NotificationManager notificationManager,
                               final SubversionOutputConsoleFactory consoleFactory,
                               final ConsolesPanelPresenter consolesPanelPresenter,
                               final SubversionExtensionLocalizationConstants constants,
                               final SubversionClientService service,
                               final ProjectExplorerPresenter projectExplorerPart,
                               final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);

        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.constants = constants;
    }

    public void cleanup() {

        final String projectPath = getCurrentProjectPath();
        if (projectPath == null) {
            return;
        }

        final List<PathTypeFilter> filters = new ArrayList<>();
        filters.add(PathTypeFilter.FOLDER);
        filters.add(PathTypeFilter.PROJECT);
        final List<String> selectedPaths = getSelectedPaths(filters);

        this.service.cleanup(projectPath, selectedPaths,
                             new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                                 @Override
                                 protected void onSuccess(final CLIOutputResponse result) {

                                     printResponse(result.getCommand(), result.getOutput(), result.getErrOutput(), constants.commandCleanup());

                                     notificationManager.notify(constants.cleanupSuccessful());
                                 }

                                 @Override
                                 protected void onFailure(final Throwable exception) {
                                     final String errorMessage = exception.getMessage();
                                     notificationManager.notify(constants.cleanupFailed() + ": " + errorMessage, FAIL, FLOAT_MODE);
                                 }
                             });
    }

}
