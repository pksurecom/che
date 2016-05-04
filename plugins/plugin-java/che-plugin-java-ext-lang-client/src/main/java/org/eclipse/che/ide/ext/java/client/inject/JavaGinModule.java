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
package org.eclipse.che.ide.ext.java.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.api.project.node.settings.SettingsProvider;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.ext.java.client.CurrentClassFQNProvider;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.action.OrganizeImportsAction;
import org.eclipse.che.ide.ext.java.client.action.ProposalAction;
import org.eclipse.che.ide.ext.java.client.dependenciesupdater.JavaClasspathServiceClient;
import org.eclipse.che.ide.ext.java.client.dependenciesupdater.JavaClasspathServiceClientImpl;
import org.eclipse.che.ide.ext.java.client.documentation.QuickDocPresenter;
import org.eclipse.che.ide.ext.java.client.documentation.QuickDocumentation;
import org.eclipse.che.ide.ext.java.client.inject.factories.PropertyWidgetFactory;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationServiceImpl;
import org.eclipse.che.ide.ext.java.client.newsourcefile.NewJavaSourceFileView;
import org.eclipse.che.ide.ext.java.client.newsourcefile.NewJavaSourceFileViewImpl;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.libraries.LibEntryPresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources.SourceEntryPresenter;
import org.eclipse.che.ide.ext.java.client.project.interceptor.JavaClassInterceptor;
import org.eclipse.che.ide.ext.java.client.project.interceptor.JavaContentRootInterceptor;
import org.eclipse.che.ide.ext.java.client.project.interceptor.TestContentRootDecorator;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettingsProvider;
import org.eclipse.che.ide.ext.java.client.reference.JavaFqnProvider;
import org.eclipse.che.ide.ext.java.client.search.JavaSearchService;
import org.eclipse.che.ide.ext.java.client.search.JavaSearchServiceWS;
import org.eclipse.che.ide.ext.java.client.search.node.NodeFactory;
import org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorsWarningsPreferenceManager;
import org.eclipse.che.ide.ext.java.client.settings.compiler.JavaCompilerPreferenceManager;
import org.eclipse.che.ide.ext.java.client.settings.compiler.JavaCompilerPreferencePresenter;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidgetImpl;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;

import static org.eclipse.che.ide.ext.java.client.action.OrganizeImportsAction.JAVA_ORGANIZE_IMPORT_ID;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class JavaGinModule extends AbstractGinModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMapBinder<String, ProposalAction> proposalActionMapBinder = GinMapBinder.newMapBinder(binder(), String.class, ProposalAction.class);
        proposalActionMapBinder.addBinding(JAVA_ORGANIZE_IMPORT_ID).to(OrganizeImportsAction.class);

        bind(NewJavaSourceFileView.class).to(NewJavaSourceFileViewImpl.class).in(Singleton.class);
        bind(QuickDocumentation.class).to(QuickDocPresenter.class).in(Singleton.class);
        bind(JavaNavigationService.class).to(JavaNavigationServiceImpl.class);
        bind(JavaClasspathServiceClient.class).to(JavaClasspathServiceClientImpl.class);
        bind(JavaSearchService.class).to(JavaSearchServiceWS.class);

        bind(JavaNodeManager.class);

        GinMapBinder<String, SettingsProvider> mapBinder =
                GinMapBinder.newMapBinder(binder(), String.class, SettingsProvider.class);
        mapBinder.addBinding("java").to(JavaNodeSettingsProvider.class);

        GinMultibinder.newSetBinder(binder(), NodeInterceptor.class).addBinding().to(JavaContentRootInterceptor.class);
        GinMultibinder.newSetBinder(binder(), NodeInterceptor.class).addBinding().to(JavaClassInterceptor.class);
        GinMultibinder.newSetBinder(binder(), NodeInterceptor.class).addBinding().to(TestContentRootDecorator.class);

        GinMapBinder<String, FqnProvider> fqnProviders = GinMapBinder.newMapBinder(binder(), String.class, FqnProvider.class);
        fqnProviders.addBinding("maven").to(JavaFqnProvider.class);

        install(new GinFactoryModuleBuilder().build(JavaNodeFactory.class));
        install(new GinFactoryModuleBuilder().implement(PropertyWidget.class, PropertyWidgetImpl.class)
                                             .build(PropertyWidgetFactory.class));

        install(new GinFactoryModuleBuilder().build(NodeFactory.class));
        install(new GinFactoryModuleBuilder().build(org.eclipse.che.ide.ext.java.client.navigation.factory.NodeFactory.class));

        GinMultibinder<PreferencePagePresenter> settingsBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        settingsBinder.addBinding().to(JavaCompilerPreferencePresenter.class);

        bind(PreferencesManager.class).annotatedWith(JavaCompilerPreferenceManager.class).to(ErrorsWarningsPreferenceManager.class);
        GinMultibinder.newSetBinder(binder(), PreferencesManager.class).addBinding().to(ErrorsWarningsPreferenceManager.class);

        GinMultibinder.newSetBinder(binder(), CommandPropertyValueProvider.class).addBinding().to(CurrentClassFQNProvider.class);
        GinMultibinder.newSetBinder(binder(), ClasspathPagePresenter.class).addBinding().to(LibEntryPresenter.class);
        GinMultibinder.newSetBinder(binder(), ClasspathPagePresenter.class).addBinding().to(SourceEntryPresenter.class);
    }

    @Provides
    @Singleton
    @Named("JavaFileType")
    protected FileType provideJavaFile() {
        return new FileType("Java", JavaResources.INSTANCE.javaFile(), MimeType.TEXT_X_JAVA, "java");
    }

    @Provides
    @Singleton
    @Named("JavaClassFileType")
    protected FileType provideJavaClassFile() {
        return new FileType("Java Class", JavaResources.INSTANCE.javaFile(), MimeType.APPLICATION_JAVA_CLASS, "class");
    }

    @Provides
    @Singleton
    @Named("JspFileType")
    protected FileType provideJspFile() {
        return new FileType("Jsp", JavaResources.INSTANCE.jspFile(), MimeType.APPLICATION_JSP, "jsp");
    }


    @Provides
    @Singleton
    @Named("JsfFileType")
    protected FileType provideJsfFile() {
        return new FileType("Jsf", JavaResources.INSTANCE.jsfFile(), MimeType.TEXT_X_JAVA, "jsf");
    }
}
