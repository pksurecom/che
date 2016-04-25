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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.button;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allow change visual representation of buttonName on properties panel.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ButtonWidgetImpl.class)
public interface ButtonWidget extends View<ButtonWidget.ActionDelegate> {

    /**
     * Performs some actions when buttonName is enable or disable.
     *
     * @param isEnable
     *         <code>true</code> buttonName is enable, <code>false</code> buttonName is disable
     */
    void setEnable(boolean isEnable);

    /**
     * Sets visibility of buttons.
     *
     * @param visible
     *         <code>true</code> buttonName is visible, <code>false</code> buttonName isn't visible
     */
    void setVisible(boolean visible);

    interface ActionDelegate {
        /** Performs some actions when user click on buttonName. */
        void onButtonClicked();
    }
}
