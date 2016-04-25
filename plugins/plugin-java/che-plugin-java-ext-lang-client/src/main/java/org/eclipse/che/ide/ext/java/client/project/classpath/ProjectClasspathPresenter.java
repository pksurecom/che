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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for managing classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathPresenter implements ProjectClasspathView.ActionDelegate, ClasspathPagePresenter.DirtyStateListener {
    private final ProjectClasspathView        view;
    private final Set<ClasspathPagePresenter> classpathPages;
    private final ClasspathServiceClient      service;
    private final AppContext                  appContext;
    private final JavaLocalizationConstant    locale;
    private final DialogFactory               dialogFactory;
    private final NotificationManager         notificationManager;
    private final ClasspathResolver           classpathResolver;

    private Map<String, Set<ClasspathPagePresenter>> propertiesMap;

    @Inject
    protected ProjectClasspathPresenter(ProjectClasspathView view,
                                        Set<ClasspathPagePresenter> classpathPages,
                                        ClasspathServiceClient service,
                                        AppContext appContext,
                                        JavaLocalizationConstant locale,
                                        DialogFactory dialogFactory,
                                        NotificationManager notificationManager,
                                        ClasspathResolver classpathResolver) {
        this.view = view;
        this.classpathPages = classpathPages;
        this.service = service;
        this.appContext = appContext;
        this.locale = locale;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.classpathResolver = classpathResolver;
        this.view.setDelegate(this);
        for (ClasspathPagePresenter property : classpathPages) {
            property.setUpdateDelegate(this);
        }
    }

    @Override
    public void onCloseClicked() {
        view.hideWindow();
        boolean haveUnsavedData = false;
        for (ClasspathPagePresenter property : classpathPages) {
            haveUnsavedData |= property.isDirty();
        }
        if (haveUnsavedData) {
            dialogFactory.createConfirmDialog(locale.unsavedChangesTitle(),
                                              locale.messagesPromptSaveChanges(),
                                              locale.buttonSave(),
                                              locale.buttonSaveChangesDiscard(),
                                              getConfirmCallback(),
                                              getCancelCallback()).show();
        } else {
            clearData();
        }
    }

    @Override
    public void onSaveClicked() {
        for (ClasspathPagePresenter property : classpathPages) {
            if (property.isDirty()) {
                property.storeChanges();
            }
        }
        classpathResolver.updateClasspath();
    }


    @Override
    public void onCancelClicked() {
        for (ClasspathPagePresenter property : classpathPages) {
            if (property.isDirty()) {
                property.revertChanges();
            }
        }
    }

    @Override
    public void onEnterClicked() {
        if (view.isCancelButtonInFocus()) {
            onCancelClicked();
            return;
        }

        if (view.isCloseButtonInFocus()) {
            onCloseClicked();
            return;
        }
        onSaveClicked();
    }

    @Override
    public void clearData() {
        for (ClasspathPagePresenter property : classpathPages) {
            property.clearData();
        }
    }

    @Override
    public void onConfigurationSelected(ClasspathPagePresenter pagePresenter) {
        pagePresenter.go(view.getConfigurationsContainer());
    }

    /** Show dialog. */
    public void show() {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return;
        }

        setReadOnlyMod(currentProject);

        String projectPath = currentProject.getProjectConfig().getPath();
        service.getClasspath(projectPath).then(new Operation<List<ClasspathEntry>>() {
            @Override
            public void apply(List<ClasspathEntry> arg) throws OperationException {
                classpathResolver.resolveClasspathEntries(arg);
                if (propertiesMap == null) {
                    propertiesMap = new HashMap<>();
                    for (ClasspathPagePresenter page : classpathPages) {
                        Set<ClasspathPagePresenter> pages = propertiesMap.get(page.getCategory());
                        if (pages == null) {
                            pages = new HashSet<>();
                            propertiesMap.put(page.getCategory(), pages);
                        }
                        pages.add(page);
                    }

                    view.setPages(propertiesMap);
                }
                view.show();
                view.setSaveButtonState(false);
                view.selectPage(propertiesMap.entrySet().iterator().next().getValue().iterator().next());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Problems with getting classpath", arg.getMessage(), FAIL, true);
            }
        });

    }

    @Override
    public void onDirtyChanged() {
        for (ClasspathPagePresenter p : classpathPages) {
            if (p.isDirty()) {
                view.setSaveButtonState(true);
                view.setCancelButtonState(true);
                return;
            }
        }
        view.setCancelButtonState(false);
        view.setSaveButtonState(false);
    }


    private ConfirmCallback getConfirmCallback() {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                onSaveClicked();
                view.hideWindow();
                clearData();
            }
        };
    }

    private CancelCallback getCancelCallback() {
        return new CancelCallback() {
            @Override
            public void cancelled() {
                onCancelClicked();
                view.hideWindow();
                clearData();
            }
        };
    }

    private void setReadOnlyMod(CurrentProject currentProject) {
        if ("maven".equals(currentProject.getProjectConfig().getType())) {
            view.setSaveButtonState(false);
            view.setCancelButtonState(false);
        }
    }

}
