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
package org.eclipse.che.ide.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.gwt.client.QueryExpression;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;

import java.util.List;

/**
 * Presenter for full text search.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FullTextSearchPresenter implements FullTextSearchView.ActionDelegate {

    private final FullTextSearchView   view;
    private final FindResultPresenter  findResultPresenter;
    private final DtoFactory           dtoFactory;
    private final AppContext           appContext;
    private final ProjectServiceClient projectServiceClient;

    @Inject
    public FullTextSearchPresenter(FullTextSearchView view,
                                   FindResultPresenter findResultPresenter,
                                   DtoFactory dtoFactory,
                                   AppContext appContext,
                                   ProjectServiceClient projectServiceClient) {
        this.view = view;
        this.findResultPresenter = findResultPresenter;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;

        this.view.setDelegate(this);
    }

    /** Show dialog with view for searching. */
    public void showDialog() {
        view.showDialog();
        view.clearInput();
        if (appContext.getCurrentProject() != null) {
            view.setPathDirectory(appContext.getCurrentProject().getProjectConfig().getPath());
        } else {
            view.setPathDirectory("/");
        }
    }

    @Override
    public void search(final String text) {
        QueryExpression queryExpression = new QueryExpression();
        final String preparedTextToSearch = prepareTextToSearch(text);
        queryExpression.setText(preparedTextToSearch);
        if (!view.getFileMask().isEmpty()) {
            queryExpression.setName(view.getFileMask());
        }
        if (!view.getPathToSearch().isEmpty()) {
            queryExpression.setPath(view.getPathToSearch());
        }

        projectServiceClient.search(appContext.getDevMachine(), queryExpression).then(new Operation<List<ItemReference>>() {
            @Override
            public void apply(List<ItemReference> result) throws OperationException {
                view.close();
                findResultPresenter.handleResponse(result, text);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                view.showErrorMessage(dtoFactory.createDtoFromJson(arg.getMessage(), ServiceError.class).getMessage());
            }
        });
    }

    /**
     * Escapes characters that QueryParser expects to be escaped by a preceding <code>\</code>
     * and adds <code>*</code> at the end of the text to search.
     */
    private String prepareTextToSearch(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('*');
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (isEscapedCharacter(character)) {
                stringBuilder.append("%5C");
            }
            stringBuilder.append(character);
        }
        stringBuilder.append('*');
        return stringBuilder.toString();
    }

    private boolean isEscapedCharacter(char character) {
        return character == '\\' ||
               character == '+' ||
               character == '-' ||
               character == '!' ||
               character == '(' ||
               character == ')' ||
               character == ':' ||
               character == '^' ||
               character == '[' ||
               character == ']' ||
               character == '\"' ||
               character == '{' ||
               character == '}' ||
               character == '~' ||
               character == '*' ||
               character == '?' ||
               character == '|' ||
               character == '&' ||
               character == '/';
    }

    @Override
    public void setPathDirectory(String path) {
        view.setPathDirectory(path);
    }

    @Override
    public void setFocus() {
        view.setFocus();
    }

    @Override
    public void onEnterClicked() {
        if (view.isAcceptButtonInFocus()) {
            String searchText = view.getSearchText();
            if (!searchText.isEmpty()) {
                search(searchText);
            }
            return;
        }

        if (view.isCancelButtonInFocus()) {
            view.close();
            return;
        }

        if (view.isSelectPathButtonInFocus()) {
            view.showSelectPathDialog();
        }
    }
}
