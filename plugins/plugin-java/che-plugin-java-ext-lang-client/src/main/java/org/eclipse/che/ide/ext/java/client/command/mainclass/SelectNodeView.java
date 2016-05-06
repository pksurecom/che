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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.project.node.Node;

import java.util.List;

/**
 * Represents the structure of the project for choosing Main class.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SelectNodeViewImpl.class)
public interface SelectNodeView extends View<SelectNodeView.ActionDelegate> {
    /** Needs for delegate some function into SelectPath view. */
    interface ActionDelegate {
        /** Sets selected node. */
        void setSelectedNode(String path, String fqn);
    }

    /**
     * Show structure of the tree.
     *
     * @param nodes
     *         list of the project root nodes
     */
    void setStructure(List<Node> nodes);

    /**
     * Show dialog.
     */
    void show();
}
