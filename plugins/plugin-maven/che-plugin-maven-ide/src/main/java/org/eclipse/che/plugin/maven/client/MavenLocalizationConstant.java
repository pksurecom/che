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
package org.eclipse.che.plugin.maven.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Represents the localization constants contained in resource bundle: 'MavenLocalizationConstant.properties'.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public interface MavenLocalizationConstant extends Messages {

    /* Actions */

    @Key("action.createMavenModule.text")
    String actionCreateMavenModuleText();

    @Key("action.effectivePom.title")
    String actionGetEffectivePomTitle();

    @Key("action.effectivePom.description")
    String actionGetEffectivePomDescription();

    @Key("action.createMavenModule.description")
    String actionCreateMavenModuleDescription();


    @Key("maven.build.updateSnapshot")
    @DefaultMessage("Update Snapshot (-U,--update-snapshots)")
    String updateSnapshot();

    @Key("maven.build.updateSnapshot.desc")
    @DefaultMessage("Forces a check for updated releases \n" +
                    "and snapshots on remote repositories")
    String updateSnapshotDesc();


    /* MavenCommandPageView */
    @Key("view.mavenCommandPage.commandLine.text")
    String mavenCommandPageViewCommandLineText();

    @Key("maven.page.artifactIdTooltip")
    String mavenPageArtifactIdTooltip();

    @Key("maven.page.groupIdTooltip")
    String mavenPageGroupIdTooltip();

    @Key("maven.create.module.multy.selection.error")
    String mavenCreateModuleMultySelectionError();

    @Key("maven.class.decompiled")
    String mavenClassDecompiled();

    @Key("maven.class.download.sources")
    String mavenDownloadSources();

    @Key("maven.class.download.failed")
    String mavenClassDownloadFailed(String fqn);

    @Key("loader.action.name")
    String loaderActionName();

    @Key("loader.action.description")
    String loaderActionDescription();

    @Key("window.loader.title")
    String windowLoaderTitle();
}
