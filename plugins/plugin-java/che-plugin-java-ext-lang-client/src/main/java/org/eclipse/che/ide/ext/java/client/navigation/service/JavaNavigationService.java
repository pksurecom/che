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
package org.eclipse.che.ide.ext.java.client.navigation.service;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.Jar;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import java.util.List;

/**
 * Service for the operations of navigation.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public interface JavaNavigationService {

    /**
     * Find declaration of the binding key
     *
     * @param projectPath
     *         path to the project
     * @param callback
     */
    void findDeclaration(String projectPath, String fqn, int offset, AsyncRequestCallback<OpenDeclarationDescriptor> callback);

    /**
     * Receive all jar dependency's
     *
     * @param projectPath
     *         path to the project
     * @param callback
     */
    void getExternalLibraries(String projectPath, AsyncRequestCallback<List<Jar>> callback);

    void getLibraryChildren(String projectPath, int libId, AsyncRequestCallback<List<JarEntry>> callback);

    void getChildren(String projectPath, int libId, String path, AsyncRequestCallback<List<JarEntry>> callback);

    void getEntry(String projectPath, int libId, String path, AsyncRequestCallback<JarEntry> callback);

    void getContent(String projectPath, int libId, String path, AsyncRequestCallback<ClassContent> callback);

    void getContent(String projectPath, String fqn, AsyncRequestCallback<ClassContent> callback);

    /**
     * Get the compilation unit representation of the java file.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     * @param showInherited
     *         <code>true</code> iff inherited members are shown
     */
    Promise<CompilationUnit> getCompilationUnit(String projectPath, String fqn, boolean showInherited);

    /**
     * Get implementations of the selected element.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     * @param offset
     *         cursor position
     * @return descriptors of the implementations
     */
    Promise<ImplementationsDescriptorDTO> getImplementations(String projectPath, String fqn, int offset);

    Promise<List<JavaProject>> getProjectsAndPackages(boolean includePackage);

    /**
     * @param projectPath
     * @param libId
     * @param path
     * @return
     */
    String getContentUrl(String projectPath, int libId, String path);

    /**
     * The method returns list of parameters for particular method or constructor. Parameters represented as string which contains hints
     * separated by comma.
     *
     * @param projectPath
     *         path to current project
     * @param fqn
     *         fqn of file
     * @param offset
     *         cursor position in editor
     * @param lineStartOffset
     *         offset of start line where method or constructor is located
     * @return list of parameters which method or constructor can accept
     */
    Promise<List<MethodParameters>> getMethodParametersHints(String projectPath, String fqn, int offset, int lineStartOffset);
}
