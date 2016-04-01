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
package org.eclipse.che.env.local.client;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_ABNORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_FAILURE_TLS_HANDSHAKE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_GOING_AWAY;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_INCONSISTENT_DATA;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NEGOTIATE_EXTENSION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NO_STATUS;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_PROTOCOL_ERROR;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_TOO_LARGE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNEXPECTED_CONDITION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNSUPPORTED;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_VIOLATE_POLICY;

/**
 * Notify that WebSocket connection was closed.
 *
 * @author Roman Nikitenko
 */
public class CheConnectionClosedInformer implements ConnectionClosedInformer {


    private DialogFactory        dialogFactory;
    private LocalizationConstant localizationConstant;

    @Inject
    CheConnectionClosedInformer(DialogFactory dialogFactory,
                                LocalizationConstant localizationConstant) {
        this.dialogFactory = dialogFactory;
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void onConnectionClosed(WebSocketClosedEvent event) {
        switch (event.getCode()) {
            case WebSocketClosedEvent.CLOSE_ABNORMAL:
                String reason = event.getReason();
                if (reason == null || reason.isEmpty()) {
                    break;
                }
            case WebSocketClosedEvent.CLOSE_NORMAL:
            case WebSocketClosedEvent.CLOSE_GOING_AWAY:
            case WebSocketClosedEvent.CLOSE_PROTOCOL_ERROR:
            case WebSocketClosedEvent.CLOSE_UNSUPPORTED:
            case WebSocketClosedEvent.CLOSE_NO_STATUS:
            case WebSocketClosedEvent.CLOSE_INCONSISTENT_DATA:
            case WebSocketClosedEvent.CLOSE_VIOLATE_POLICY:
            case WebSocketClosedEvent.CLOSE_TOO_LARGE:
            case WebSocketClosedEvent.CLOSE_NEGOTIATE_EXTENSION:
            case WebSocketClosedEvent.CLOSE_UNEXPECTED_CONDITION:
            case WebSocketClosedEvent.CLOSE_FAILURE_TLS_HANDSHAKE:
                showMessageDialog(localizationConstant.connectionClosedDialogTitle(), localizationConstant.messagesServerFailure());
        }
    }

    /**
     * Displays dialog using title and message.
     */
    private void showMessageDialog(String title, String message) {
        dialogFactory.createMessageDialog(title, message, null).show();
    }
}
