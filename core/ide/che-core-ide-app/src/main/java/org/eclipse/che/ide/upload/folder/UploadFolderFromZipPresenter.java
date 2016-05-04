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
package org.eclipse.che.ide.upload.folder;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The purpose of this class is upload folder from zip
 *
 * @author Roman Nikitenko.
 */
public class UploadFolderFromZipPresenter implements UploadFolderFromZipView.ActionDelegate {

    private final UploadFolderFromZipView  view;
    private final ProjectExplorerPresenter projectExplorer;
    private final CoreLocalizationConstant locale;
    private final EditorAgent              editorAgent;
    private final EventBus                 eventBus;
    private final NotificationManager      notificationManager;
    private final AppContext               appContext;

    @Inject
    public UploadFolderFromZipPresenter(UploadFolderFromZipView view,
                                        AppContext appContext,
                                        EditorAgent editorAgent,
                                        EventBus eventBus,
                                        NotificationManager notificationManager,
                                        ProjectExplorerPresenter projectExplorer,
                                        CoreLocalizationConstant locale) {
        this.appContext = appContext;
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.view = view;
        this.view.setDelegate(this);
        this.view.setEnabledUploadButton(false);
        this.projectExplorer = projectExplorer;
        this.locale = locale;
        this.notificationManager = notificationManager;
    }

    /** Show dialog. */
    public void showDialog() {
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onSubmitComplete(String result) {
        view.setLoaderVisibility(false);
        projectExplorer.reloadChildren(getResourceBasedNode());

        if (result != null && !result.isEmpty()) {
            view.closeDialog();
            notificationManager.notify(locale.failedToUploadFilesFromZip(), parseMessage(result), FAIL, FLOAT_MODE);
            return;
        }

        if (view.isOverwriteFileSelected()) {
            updateOpenedEditors();
        }
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onUploadClicked() {
        view.setLoaderVisibility(true);
        view.setEncoding(FormPanel.ENCODING_MULTIPART);
        view.setAction(appContext.getDevMachine().getWsAgentBaseUrl() + "/project/" + appContext.getDevMachine().getWorkspace() + "/upload/zipfolder/" +
                       ((HasStorablePath)getResourceBasedNode()).getStorablePath());
        view.submit();
    }

    /** {@inheritDoc} */
    @Override
    public void onFileNameChanged() {
        String fileName = view.getFileName();
        boolean enabled = !fileName.isEmpty() && fileName.contains(".zip");
        view.setEnabledUploadButton(enabled);
    }

    protected ResourceBasedNode<?> getResourceBasedNode() {
        List<?> selection = projectExplorer.getSelection().getAllElements();
        //we should be sure that user selected single element to work with it
        if (selection != null && selection.isEmpty() || selection.size() > 1) {
            return null;
        }

        Object o = selection.get(0);

        if (o instanceof ResourceBasedNode<?>) {
            ResourceBasedNode<?> node = (ResourceBasedNode<?>)o;
            //it may be file node, so we should take parent node
            if (node.isLeaf() && isResourceAndStorableNode(node.getParent())) {
                return (ResourceBasedNode<?>)node.getParent();
            }

            return isResourceAndStorableNode(node) ? node : null;
        }

        return null;
    }

    protected boolean isResourceAndStorableNode(@Nullable Node node) {
        return node != null && node instanceof ResourceBasedNode<?> && node instanceof HasStorablePath;
    }

    private String parseMessage(String message) {
        int startIndex = 0;
        int endIndex = -1;

        if (message.contains("<pre>message:")) {
            startIndex = message.indexOf("<pre>message:") + "<pre>message:".length();
        } else if (message.contains("<pre>")) {
            startIndex = message.indexOf("<pre>") + "<pre>".length();
        }

        if (message.contains("</pre>")) {
            endIndex = message.indexOf("</pre>");
        }
        return (endIndex != -1) ? message.substring(startIndex, endIndex) : message.substring(startIndex);
    }

    private void updateOpenedEditors() {
        for (EditorPartPresenter partPresenter : editorAgent.getOpenedEditors()) {
            String filePath = partPresenter.getEditorInput().getFile().getPath();
            String path = ((HasStorablePath)getResourceBasedNode()).getStorablePath();
            if (filePath.contains(path)) {
                eventBus.fireEvent(new FileContentUpdateEvent(filePath));
            }
        }
    }
}
