package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.PopupButton;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

@SuppressWarnings("serial")
@Connect(value = PopupButton.class, loadStyle = LoadStyle.DEFERRED)
public class PopupButtonConnector extends AbstractHasComponentsConnector
        implements ClickHandler, CloseHandler<PopupPanel> {

    public PopupButtonClientRpc rpc = new PopupButtonClientRpc() {

        @Override
        public void openPopup() {
            getWidget().openPopup();
        }

        @Override
        public void closePopup() {
            getWidget().closePopup();
        }

    };

    @Override
    public void init() {
        super.init();
        registerRpc(PopupButtonClientRpc.class, rpc);
        getWidget().addClickHandler(this);
        getWidget().addCloseHandler(this);
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(PopupButtonWidget.class);
    }

    @Override
    public PopupButtonWidget getWidget() {
        return (PopupButtonWidget) super.getWidget();
    }

    @Override
    public PopupButtonState getState() {
        return (PopupButtonState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        final PopupButtonWidget widget = getWidget();
        final PopupButtonState state = getState();
        if (stateChangeEvent.hasPropertyChanged("col")
                || stateChangeEvent.hasPropertyChanged("row")) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    widget.setRowCol(state.row, state.col);
                }
            });
        }
        if (stateChangeEvent.hasPropertyChanged("active")) {
            widget.markActive(state.active);
        }
        if (stateChangeEvent.hasPropertyChanged("popupHeight")) {
            widget.setPopupHeight(state.popupHeight);
        }
        if (stateChangeEvent.hasPropertyChanged("popupWidth")) {
            widget.setPopupWidth(state.popupWidth);
        }
        if (stateChangeEvent.hasPropertyChanged("headerHidden")) {
            widget.setPopupHeaderHidden(state.headerHidden);
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public void onConnectorHierarchyChange(
            ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        List<ComponentConnector> oldChildren = connectorHierarchyChangeEvent
                .getOldChildren();
        List<ComponentConnector> newChildren = getChildComponents();
        PopupButtonWidget widget = getWidget();
        for (ComponentConnector old : oldChildren) {
            if (!newChildren.contains(old)) {
                widget.removePopupComponent(old.getWidget());
            }
        }
        for (ComponentConnector cc : newChildren) {
            if (!oldChildren.contains(cc)) {
                widget.addPopupComponent(cc.getWidget());
            }
        }
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        if (getWidget().isAttached()) {
            getRpcProxy(PopupButtonServerRpc.class).onPopupClose();
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        getRpcProxy(PopupButtonServerRpc.class).onPopupButtonClick();
    }
}
