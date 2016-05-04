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
package org.eclipse.che.plugin.svn.ide.log;

import com.google.inject.Inject;

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
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.SubversionItem;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Manages the displaying commit log messages for specified period.
 */
public class ShowLogPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final SubversionClientService                  subversionClientService;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    private final ShowLogsView view;

    /**
     * Creates an instance of this presenter.
     */
    @Inject
    protected ShowLogPresenter(final AppContext appContext,
                               final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               final SubversionOutputConsoleFactory consoleFactory,
                               final ConsolesPanelPresenter consolesPanelPresenter,
                               final SubversionClientService subversionClientService,
                               final NotificationManager notificationManager,
                               final ProjectExplorerPresenter projectExplorerPart,
                               final SubversionExtensionLocalizationConstants constants,
                               final ShowLogsView view,
                               final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);

        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.subversionClientService = subversionClientService;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view = view;

        view.setDelegate(new ShowLogsView.Delegate() {
            @Override
            public void logClicked() {
                String range = view.rangeField().getValue();
                if (range != null && !range.trim().isEmpty()) {
                    view.hide();
                    showLogs(range);
                }
            }

            @Override
            public void cancelClicked() {
                view.hide();
            }
        });
    }

    /**
     * Fetches the count of revisions and opens the popup.
     */
    public void showLog() {
        if (getActiveProject() == null) {
            return;
        }

        subversionClientService.info(getActiveProject().getRootProject().getPath(), getSelectedPaths().get(0), "HEAD", false,
                new AsyncRequestCallback<InfoResponse>(dtoUnmarshallerFactory.newUnmarshaller(InfoResponse.class)) {
                    @Override
                    protected void onSuccess(InfoResponse result) {
                        if (result.getErrorOutput() != null && !result.getErrorOutput().isEmpty()) {
                            printErrors(result.getErrorOutput(), constants.commandInfo());
                            notificationManager.notify("Unable to execute subversion command", FAIL, FLOAT_MODE);
                            return;
                        }

                        SubversionItem subversionItem = result.getItems().get(0);
                        view.setRevisionCount(subversionItem.getRevision());
                        view.rangeField().setValue("1:" + subversionItem.getRevision());
                        view.show();
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                    }
                });

    }

    /**
     * Fetches and displays commit log messages for specified range.
     *
     * @param range range to be logged
     */
    private void showLogs(String range) {
        subversionClientService.showLog(getActiveProject().getRootProject().getPath(), getSelectedPaths(), range,
                new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                    @Override
                    protected void onSuccess(CLIOutputResponse result) {
                        printResponse(result.getCommand(), result.getOutput(), null, constants.commandLog());
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                    }
                });
    }

}
