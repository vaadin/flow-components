/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

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
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.communication.ServerRpc;

@SuppressWarnings("serial")
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
    private PopupButtonServerRpc serverRpc;

    @Override
    public void init() {
        super.init();
        registerRpc(PopupButtonClientRpc.class, rpc);
        getWidget().addClickHandler(this);
        getWidget().addCloseHandler(this);
    }

    public void setPopupButtonServerRpc(PopupButtonServerRpc rpc) {
        this.serverRpc = rpc;
    }

    @Override
    protected <T extends ServerRpc> T getRpcProxy(Class<T> rpcInterface) {
        return (T) serverRpc;
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
        final PopupButtonWidget widget = getWidget();
        if (widget.isAttached()) {
            getRpcProxy(PopupButtonServerRpc.class)
                    .onPopupClose(widget.getRow(), widget.getCol());
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        final PopupButtonWidget widget = getWidget();
        getRpcProxy(PopupButtonServerRpc.class)
                .onPopupButtonClick(widget.getRow(), widget.getCol());
    }

}
