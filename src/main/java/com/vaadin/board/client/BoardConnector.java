package com.vaadin.board.client;

import com.google.gwt.user.client.Element;
import com.vaadin.board.Board;
import com.vaadin.board.client.BoardState.RowState;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;

@Connect(Board.class)
public class BoardConnector extends AbstractHasComponentsConnector {

    public BoardConnector() {
    }

    @Override
    public BoardWidget getWidget() {
        return (BoardWidget) super.getWidget();
    }

    @Override
    public BoardState getState() {
        return (BoardState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        // TODO do something useful
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // Captions are not supported
    }

    @Override
    public void onConnectorHierarchyChange(
            ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        // TODO: Dynamic updates
        int childCount = getWidget().getWidgetCount();
        while (childCount-- > 0) {
            getWidget().remove(0);
        }

        for (int i = 0; i < getState().rows.size(); i++) {
            RowState row = getState().rows.get(i);
            BoardRow rowWidget = new BoardRow();
            getWidget().add(rowWidget);

            for (Connector child : row.components) {
                rowWidget.add(((ComponentConnector) child).getWidget());
            }
        }

        for (int i = 0; i < getWidget().getWidgetCount(); i++) {
            forceRowLayout(getWidget().getWidget(i).getElement());
        }
    }

    private native void forceRowLayout(Element element)
    /*-{
        element._onIronResize();
    }-*/;
}
