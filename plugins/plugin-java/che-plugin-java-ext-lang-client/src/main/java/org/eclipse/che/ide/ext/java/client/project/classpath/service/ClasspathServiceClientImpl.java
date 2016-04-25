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
package org.eclipse.che.ide.ext.java.client.project.classpath.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntry;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import java.util.List;

/**
 * The implementation of {@link ClasspathServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathServiceClientImpl implements ClasspathServiceClient {

    private final String              pathToService;
    private final MessageLoader       loader;
    private final AsyncRequestFactory asyncRequestFactory;
    private final DtoUnmarshallerFactory unmarshallerFactory;

    @Inject
    public ClasspathServiceClientImpl(AsyncRequestFactory asyncRequestFactory,
                                      DtoUnmarshallerFactory unmarshallerFactory,
                                      @Named("cheExtensionPath") String extPath,
                                      AppContext appContext,
                                      LoaderFactory loaderFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.unmarshallerFactory = unmarshallerFactory;
        this.loader = loaderFactory.newLoader();

        this.pathToService = extPath + "/jdt/" + appContext.getWorkspace().getId() + "/classpath";
    }

    @Override
    public Promise<List<ClasspathEntry>> getClasspath(String projectPath) {
        final String url = pathToService + "?projectpath=" + projectPath;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(unmarshallerFactory.newListUnmarshaller(ClasspathEntry.class));
    }

}
