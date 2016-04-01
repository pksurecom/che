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
package org.eclipse.che.ide.ext.maven.shared;

/**
 * @author Evgen Vidolob
 */
public final class MavenAttributes {
    private MavenAttributes() {
    }

    public static String MAVEN_ID   = "maven";
    public static String MAVEN_NAME = "Maven Project";

    public static String GENERATION_STRATEGY_OPTION = "type";

    public static String SIMPLE_GENERATION_STRATEGY    = "simple";
    public static String ARCHETYPE_GENERATION_STRATEGY = "archetype";

    public static String ARCHETYPE_GROUP_ID_OPTION    = "archetypeGroupId";
    public static String ARCHETYPE_ARTIFACT_ID_OPTION = "archetypeArtifactId";
    public static String ARCHETYPE_VERSION_OPTION     = "archetypeVersion";
    public static String ARCHETYPE_REPOSITORY_OPTION  = "archetypeRepository";

    public static String GROUP_ID           = "maven.groupId";
    public static String ARTIFACT_ID        = "maven.artifactId";
    public static String VERSION            = "maven.version";
    public static String PACKAGING          = "maven.packaging";
    public static String PARENT_GROUP_ID    = "maven.parent.groupId";
    public static String PARENT_ARTIFACT_ID = "maven.parent.artifactId";
    public static String PARENT_VERSION     = "maven.parent.version";

    public static String SOURCE_FOLDER      = "maven.source.folder";
    public static String TEST_SOURCE_FOLDER = "maven.test.source.folder";

    public static String RESOURCE_FOLDER = "maven.resource.folder";

    public static String DEFAULT_SOURCE_FOLDER         = "src/main/java";
    public static String DEFAULT_RESOURCES_FOLDER      = "src/main/resources";
    public static String DEFAULT_TEST_SOURCE_FOLDER    = "src/test/java";
    public static String DEFAULT_TEST_RESOURCES_FOLDER = "src/test/resources";
    public static String DEFAULT_VERSION               = "1.0-SNAPSHOT";
    public static String DEFAULT_PACKAGING             = "jar";
}
