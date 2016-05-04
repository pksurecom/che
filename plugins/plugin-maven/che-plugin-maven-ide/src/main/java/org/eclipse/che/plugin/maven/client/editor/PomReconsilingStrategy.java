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
package org.eclipse.che.plugin.maven.client.editor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.text.Region;
import org.eclipse.che.ide.ext.java.client.editor.ProblemRequester;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.reconciler.DirtyRegion;
import org.eclipse.che.ide.jseditor.client.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Reconciling strategy for 'pom.xml'
 *
 * @author Evgen Vidolob
 */
public class PomReconsilingStrategy implements ReconcilingStrategy {

    private final AnnotationModel          annotationModel;
    private final EmbeddedTextEditorPresenter<?>
                                           editor;
    private final MavenServerServiceClient client;
    private       String                   pomPath;
    private       String                   projectPath;

    @Inject
    public PomReconsilingStrategy(@Assisted AnnotationModel annotationModel,
                                  @Assisted @NotNull final EmbeddedTextEditorPresenter<?> editor,
                                  MavenServerServiceClient client) {
        this.annotationModel = annotationModel;
        this.editor = editor;
        this.client = client;
    }

    @Override
    public void setDocument(Document document) {
        pomPath = document.getFile().getPath();
        projectPath = document.getFile().getProject().getProjectConfig().getPath();
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, Region subRegion) {
        doReconcile();
    }

    @Override
    public void reconcile(Region partition) {
        doReconcile();
    }

    public void doReconcile() {
        Promise<List<Problem>> promise = client.reconcilePom(pomPath);
        promise.then(new Operation<List<Problem>>() {
            @Override
            public void apply(List<Problem> arg) throws OperationException {
                doReconcile(arg);
            }
        });
    }

    private void doReconcile(final List<Problem> problems) {

        if (this.annotationModel == null) {
            return;
        }
        ProblemRequester problemRequester;
        if (this.annotationModel instanceof ProblemRequester) {
            problemRequester = (ProblemRequester)this.annotationModel;
            problemRequester.beginReporting();
        } else {
            editor.setErrorState(EditorWithErrors.EditorState.NONE);
            return;
        }
        try {
            boolean error = false;
            boolean warning = false;
            for (Problem problem : problems) {

                if (!error) {
                    error = problem.isError();
                }
                if (!warning) {
                    warning = problem.isWarning();
                }
                problemRequester.acceptProblem(problem);
            }
            if (error) {
                editor.setErrorState(EditorWithErrors.EditorState.ERROR);
            } else if (warning) {
                editor.setErrorState(EditorWithErrors.EditorState.WARNING);
            } else {
                editor.setErrorState(EditorWithErrors.EditorState.NONE);
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            problemRequester.endReporting();
        }
    }

    @Override
    public void closeReconciler() {

    }
}
