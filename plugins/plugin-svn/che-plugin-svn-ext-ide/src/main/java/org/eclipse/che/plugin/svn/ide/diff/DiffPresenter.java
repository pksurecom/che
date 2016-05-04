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
package org.eclipse.che.plugin.svn.ide.diff;

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
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.DiffAction} action.
 */
@Singleton
public class DiffPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;
    private final StatusColors                             statusColors;

    @Inject
    protected DiffPresenter(final AppContext appContext,
                            final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            final NotificationManager notificationManager,
                            final SubversionOutputConsoleFactory consoleFactory,
                            final ConsolesPanelPresenter consolesPanelPresenter,
                            final SubversionClientService service,
                            final SubversionExtensionLocalizationConstants constants,
                            final ProjectExplorerPresenter projectExplorerPart,
                            final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);

        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.statusColors = statusColors;
    }

    public void showDiff() {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        service.showDiff(projectPath, getSelectedPaths(), "HEAD",
                         new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                             @Override
                             protected void onSuccess(CLIOutputResponse result) {
                                 printResponse(result.getCommand(), result.getOutput(), result.getErrOutput(), constants.commandDiff());
                             }

                             @Override
                             protected void onFailure(Throwable exception) {
                                 notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                             }
                         });
    }

}
