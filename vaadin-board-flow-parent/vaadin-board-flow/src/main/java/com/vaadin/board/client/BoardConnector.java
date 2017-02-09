package com.vaadin.board.client;

import java.util.List;

import com.vaadin.board.Board;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
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
    public void updateCaption(ComponentConnector connector) {
        // Captions are not supported
    }

    @Override
    public void onConnectorHierarchyChange(
            ConnectorHierarchyChangeEvent event) {
        List<ComponentConnector> previousChildren = event.getOldChildren();
        int currentIndex = 0;
        BoardWidget board = getWidget();

        for (ComponentConnector child : getChildComponents()) {
            board.insert(child.getWidget(), currentIndex++);
        }

        for (ComponentConnector child : previousChildren) {
            if (child.getParent() != this) {
                board.remove(child.getWidget());
            }
        }

        // Hack, hack
        for (ComponentConnector child : getChildComponents()) {
            ((RowConnector) child).forceLayout();
        }
    }
}
