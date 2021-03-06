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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.inject.Provider;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.util.input.KeyMapUtil;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.util.dom.Elements.disableTextSelection;

/**
 * PopupMenu is visual component represents all known Popup Menu.
 *
 * @author Vitaliy Gulyy
 * @author Oleksii Orel
 */

public class PopupMenu extends Composite {

    private static final PopupResources POPUP_RESOURCES = GWT.create(PopupResources.class);

    static {
        POPUP_RESOURCES.popup().ensureInjected();
    }

    private final ActionManager                actionManager;
    private final Provider<PerspectiveManager> managerProvider;
    /** Working variable is needs to indicate when PopupMenu has at list one MenuItem with selected state. */
    private       boolean                      hasCheckedItems;
    /** Callback uses for notify Parent Menu when menu item is selecred. */
    private       ActionSelectedHandler        actionSelectedHandler;
    /**
     * Lock layer uses as root for displaying this PopupMenu and uses for locking screen and hiding menu when user just clicked outside
     * menu.
     */
    private       MenuLockLayer                lockLayer;

    /** Contains opened sub Popup Menu. */
    private PopupMenu openedSubPopup;
    private Element   subPopupAnchor;

    /** Contains HTML element ( <TR> ) which is hovered for the current time. */
    private Element hoveredTR;

    /**
     * Working variable.
     * PopupMenu panel.
     */
    private SimplePanel         popupMenuPanel;
    /** Working variable. Special table uses for handling mouse events. */
    private PopupMenuTable      table;
    private PresentationFactory presentationFactory;
    private KeyBindingAgent     keyBindingAgent;
    /**
     * Prefix to be appended to the ID for each menu item.
     * This is debug feature.
     */
    private String              itemIdPrefix;
    private List<Action>        list;

    private Timer openSubPopupTimer = new Timer() {
        @Override
        public void run() {
            openSubPopup(hoveredTR);
        }
    };

    private Timer closeSubPopupTimer = new Timer() {
        @Override
        public void run() {
            if (openedSubPopup != null) {
                openedSubPopup.closePopup();
                openedSubPopup = null;

                Element e = subPopupAnchor;
                subPopupAnchor = null;
                setStyleNormal(e);
            }
        }
    };


    /**
     * Create PopupMenu
     *
     * @param lockLayer
     *         - lock layer, uses as rot for attaching this popup menu.
     * @param actionSelectedHandler
     *         - callback, uses for notifying parent menu when menu item is selected.
     */
    public PopupMenu(ActionGroup actionGroup,
                     ActionManager actionManager,
                     Provider<PerspectiveManager> managerProvider,
                     PresentationFactory presentationFactory,
                     MenuLockLayer lockLayer,
                     ActionSelectedHandler actionSelectedHandler,
                     KeyBindingAgent keyBindingAgent,
                     String itemIdPrefix) {
        this.actionManager = actionManager;
        this.managerProvider = managerProvider;
        this.presentationFactory = presentationFactory;
        this.keyBindingAgent = keyBindingAgent;
        this.itemIdPrefix = itemIdPrefix;
        this.lockLayer = lockLayer;
        this.actionSelectedHandler = actionSelectedHandler;

        List<Utils.VisibleActionGroup> visibleActionGroupList =
                Utils.renderActionGroup(actionGroup, presentationFactory, actionManager, managerProvider.get());

        list = new ArrayList<>();
        for (Utils.VisibleActionGroup groupActions : visibleActionGroupList) {
            list.addAll(groupActions.getActionList());
        }

        popupMenuPanel = new SimplePanel();
        disableTextSelection(popupMenuPanel.getElement(), true);
        initWidget(popupMenuPanel);

        popupMenuPanel.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                closeSubPopupTimer.cancel();

                PopupMenu.this.setStyleNormal(hoveredTR);
                hoveredTR = null;

                if (subPopupAnchor != null) {
                    setStyleHovered(subPopupAnchor);
                }
            }
        }, MouseOutEvent.getType());

        popupMenuPanel.setStyleName(POPUP_RESOURCES.popup().popupMenuMain());

        hasCheckedItems = hasCheckedItems();

        redraw();
    }

    private boolean isRowEnabled(Element tr) {
        if (tr == null) {
            return false;
        }

        String index = tr.getAttribute("item-index");
        if (index == null || "".equals(index)) {
            return false;
        }

        String enabled = tr.getAttribute("item-enabled");
        if (enabled == null || "".equals(enabled) || "false".equals(enabled)) {
            return false;
        }

        int itemIndex = Integer.parseInt(index);
        Action menuItem = list.get(itemIndex);
        return presentationFactory.getPresentation(menuItem).isEnabled();

    }

    /** Close this Popup Menu. */
    public void closePopup() {
        if (openedSubPopup != null) {
            openedSubPopup.closePopup();
        }

        removeFromParent();
    }

    /** Render Popup Menu component. */
    private void redraw() {
        String idPrefix = itemIdPrefix;
        if (idPrefix == null) {
            idPrefix = "";
        } else {
            idPrefix += "/";
        }

        table = new PopupMenuTable();
        table.setStyleName(POPUP_RESOURCES.popup().popupMenuTable());
        table.setCellPadding(0);
        table.setCellSpacing(0);

        for (int i = 0; i < list.size(); i++) {
            Action menuItem = list.get(i);

            if (menuItem instanceof Separator) {
                final String separatorText = ((Separator)menuItem).getText();
                if (separatorText == null) {
                    table.getCellFormatter().setStyleName(i, 0, POPUP_RESOURCES.popup().popupMenuDelimiter());
                } else {
                    table.setWidget(i, 0, new Label(separatorText));
                    table.getCellFormatter().setStyleName(i, 0, POPUP_RESOURCES.popup().popupMenuTextDelimiter());
                }
                table.getFlexCellFormatter().setColSpan(i, 0, hasCheckedItems ? 5 : 4);
            } else {
                Presentation presentation = presentationFactory.getPresentation(menuItem);

                if (presentation.getImageResource() != null) {
                    Image image = new Image(presentation.getImageResource());
                    table.setWidget(i, 0, image);

                } else if (presentation.getSVGResource() != null) {
                    SVGImage image = new SVGImage(presentation.getSVGResource());
                    table.setWidget(i, 0, image);

                } else if (presentation.getHTMLResource() != null) {
                    table.setHTML(i, 0, presentation.getHTMLResource());
                }

                table.getCellFormatter().setStyleName(i, 0, presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuIconField()
                                                                               : POPUP_RESOURCES.popup().popupMenuIconFieldDisabled());

                int work = 1;
                if (hasCheckedItems && menuItem instanceof ToggleAction) {
                    ToggleAction toggleAction = (ToggleAction)menuItem;
                    ActionEvent e = new ActionEvent(presentationFactory.getPresentation(toggleAction),
                                                    actionManager,
                                                    managerProvider.get());

                    if (toggleAction.isSelected(e)) {
                        // Temporary solution
                        table.setHTML(i, work, "<i class=\"fa fa-check\"></i>");
                    }

                    table.getCellFormatter().setStyleName(i, work, presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuCheckField()
                                                                                   : POPUP_RESOURCES.popup().popupMenuCheckFieldDisabled());
                    work++;
                }

                table.setHTML(i, work, "<nobr id=\"" + idPrefix + presentation.getText() + "\">" + presentation.getText() + "</nobr>");
                table.getCellFormatter().setStyleName(i, work, presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuTitleField()
                                                                               : POPUP_RESOURCES.popup().popupMenuTitleFieldDisabled());

                work++;
                String hotKey = KeyMapUtil.getShortcutText(keyBindingAgent.getKeyBinding(actionManager.getId(menuItem)));
                if (hotKey == null) {
                    hotKey = "&nbsp;";
                } else {
                    hotKey =
                            "<nobr>&nbsp;" + hotKey + "&nbsp;</nobr>";
                }

                table.setHTML(i, work, hotKey);
                table.getCellFormatter().setStyleName(i, work,
                                                      presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuHotKeyField()
                                                                               : POPUP_RESOURCES.popup().popupMenuHotKeyFieldDisabled());

                work++;

                if (menuItem instanceof ActionGroup && !(((ActionGroup)menuItem).canBePerformed() &&
                                                         !Utils.hasVisibleChildren((ActionGroup)menuItem,
                                                                                   presentationFactory,
                                                                                   actionManager,
                                                                                   managerProvider.get()))) {
                    table.setWidget(i, work, new SVGImage(POPUP_RESOURCES.subMenu()));
                    table.getCellFormatter().setStyleName(i, work,
                                                          presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuSubMenuField()
                                                                                   : POPUP_RESOURCES.popup()
                                                                                                    .popupMenuSubMenuFieldDisabled());
                } else {
                    table.getCellFormatter().setStyleName(i, work,
                                                          presentation.isEnabled() ? POPUP_RESOURCES.popup().popupMenuSubMenuField()
                                                                                   : POPUP_RESOURCES.popup()
                                                                                                    .popupMenuSubMenuFieldDisabled());
                }

                work++;

                table.getRowFormatter().getElement(i).setAttribute("item-index", Integer.toString(i));
                table.getRowFormatter().getElement(i).setAttribute("item-enabled", Boolean.toString(presentation.isEnabled()));

                String actionId = actionManager.getId(menuItem);
                String debugId;
                if (actionId == null) {
                    debugId = idPrefix + menuItem.getTemplatePresentation().getText();
                } else {
                    debugId = idPrefix + actionId;
                }
                UIObject.ensureDebugId(table.getRowFormatter().getElement(i), debugId);
            }
        }

        popupMenuPanel.add(table);
    }

    /** @return true when at list one item from list of menu items has selected state. */
    private boolean hasCheckedItems() {
        for (int i = 0; i < list.size(); i++) {
            Action action = list.get(i);
            if (action instanceof ToggleAction) {

                ActionEvent e = new ActionEvent(presentationFactory.getPresentation(action), actionManager, managerProvider.get());
                if (((ToggleAction)action).isSelected(e)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handling MouseOut event.
     *
     * @param row
     *         - element to be processed.
     */
    protected void setStyleNormal(Element row) {
        if (row != null) {
            row.removeClassName(POPUP_RESOURCES.popup().popupMenuItemOver());
        }
    }

    private void setStyleHovered(Element tr) {
        tr.setClassName(POPUP_RESOURCES.popup().popupMenuItemOver());
    }

    /**
     * Handling MouseOver event.
     *
     * @param tr
     *         - element to be processed.
     */
    protected void onRowHovered(Element tr) {
        if (tr == hoveredTR) {
            return;
        }

        setStyleNormal(hoveredTR);
        if (subPopupAnchor != null) {
            setStyleHovered(subPopupAnchor);
        }

        if (!isRowEnabled(tr)) {
            hoveredTR = null;
            return;
        }

        hoveredTR = tr;
        setStyleHovered(tr);

        int itemIndex = Integer.parseInt(tr.getAttribute("item-index"));
        Action menuItem = list.get(itemIndex);
        openSubPopupTimer.cancel();
        if (menuItem instanceof ActionGroup && !(((ActionGroup)menuItem).canBePerformed() &&
                                                 !Utils.hasVisibleChildren((ActionGroup)menuItem,
                                                                           presentationFactory,
                                                                           actionManager,
                                                                           managerProvider.get()))) {
            openSubPopupTimer.schedule(300);
        } else {
            closeSubPopupTimer.cancel();
            closeSubPopupTimer.schedule(200);
        }
    }

    /**
     * Handle Mouse Click
     *
     * @param tr
     */
    protected void onRowClicked(Element tr) {
        if (!isRowEnabled(tr) || tr == subPopupAnchor) {
            return;
        }

        int itemIndex = Integer.parseInt(tr.getAttribute("item-index"));
        Action menuItem = list.get(itemIndex);
        if (menuItem instanceof ActionGroup && (!((ActionGroup)menuItem).canBePerformed() &&
                                                Utils.hasVisibleChildren((ActionGroup)menuItem,
                                                                         presentationFactory,
                                                                         actionManager,
                                                                         managerProvider.get()))) {
            openSubPopup(tr);
        } else {
            if (actionSelectedHandler != null) {
                actionSelectedHandler.onActionSelected(menuItem);
            }
            ActionEvent e = new ActionEvent(presentationFactory.getPresentation(menuItem), actionManager, managerProvider.get());
            menuItem.actionPerformed(e);
        }
    }

    private void openSubPopup(final Element tableRowElement) {
        if (tableRowElement == null) {
            return;
        }

        if (openedSubPopup != null) {
            if (tableRowElement == subPopupAnchor) {
                return;
            }

            openedSubPopup.closePopup();
        }

        if (subPopupAnchor != null) {
            Element e = subPopupAnchor;
            subPopupAnchor = null;
            setStyleNormal(e);
        }

        subPopupAnchor = tableRowElement;
        setStyleHovered(subPopupAnchor);

        int itemIndex = Integer.parseInt(tableRowElement.getAttribute("item-index"));
        Action menuItem = list.get(itemIndex);

        String idPrefix = itemIdPrefix;
        if (idPrefix != null) {
            idPrefix += "/" + presentationFactory.getPresentation(menuItem).getText();
        }

        openedSubPopup = new PopupMenu((ActionGroup)menuItem,
                                       actionManager,
                                       managerProvider,
                                       presentationFactory,
                                       lockLayer,
                                       actionSelectedHandler,
                                       keyBindingAgent,
                                       idPrefix);

        final int HORIZONTAL_OFFSET = 3;
        final int VERTICAL_OFFSET = 1;

        openedSubPopup.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        lockLayer.add(openedSubPopup, 0, 0);

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                int left = getAbsoluteLeft() + getOffsetWidth() - HORIZONTAL_OFFSET;
                int top = tableRowElement.getAbsoluteTop() - lockLayer.getTopOffset() - VERTICAL_OFFSET;

                if (left + openedSubPopup.getOffsetWidth() > Window.getClientWidth()) {
                    if (left > openedSubPopup.getOffsetWidth()) {
                        left = getAbsoluteLeft() - openedSubPopup.getOffsetWidth() + HORIZONTAL_OFFSET;
                    } else {
                        int diff = left + openedSubPopup.getOffsetWidth() - Window.getClientWidth();
                        left -= diff;
                    }
                }

                if (top + openedSubPopup.getOffsetHeight() > Window.getClientHeight()) {
                    if (top > openedSubPopup.getOffsetHeight()) {
                        top = tableRowElement.getAbsoluteTop() - openedSubPopup.getOffsetHeight() + VERTICAL_OFFSET;
                    } else {
                        int diff = top + openedSubPopup.getOffsetHeight() - Window.getClientHeight();
                        top -= diff;
                    }
                }

                openedSubPopup.getElement().getStyle().setLeft(left, Unit.PX);
                openedSubPopup.getElement().getStyle().setTop(top, Unit.PX);
                openedSubPopup.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            }
        });
    }

    interface PopupResources extends ClientBundle {

        @Source({"popup-menu.css", "org/eclipse/che/ide/api/ui/style.css"})
        Css popup();

        @Source("org/eclipse/che/ide/menu/submenu.svg")
        SVGResource subMenu();
    }

    interface Css extends CssResource {

        String popupMenuSubMenuFieldDisabled();

        String popupMenuHotKeyFieldDisabled();

        String popupMenuTitleField();

        String popupMenuIconField();

        String popupMenuDelimiter();

        String popupMenuTextDelimiter();

        String popupMenuIconFieldDisabled();

        String popupMenuCheckField();

        String popupMenuTable();

        String popupMenuSubMenuField();

        String popupMenuMain();

        String popupMenuTitleFieldDisabled();

        String popupMenuCheckFieldDisabled();

        String popupMenuHotKeyField();

        String popupMenuItemOver();
    }

    /** This table uses for handling mouse events. */
    private class PopupMenuTable extends FlexTable {

        public PopupMenuTable() {
            sinkEvents(Event.ONMOUSEOVER | Event.ONCLICK);
        }

        @Override
        public void onBrowserEvent(Event event) {
            Element td = getEventTargetCell(event);

            if (td == null) {
                return;
            }
            Element tr = DOM.getParent(td);

            String index = tr.getAttribute("item-index");
            if (index == null || "".equals(index)) {
                return;
            }

            switch (DOM.eventGetType(event)) {
                case Event.ONMOUSEOVER:
                    onRowHovered(tr);
                    break;

                case Event.ONCLICK:
                    onRowClicked(tr);
                    event.preventDefault();
                    event.stopPropagation();
                    break;
            }

        }
    }
}
