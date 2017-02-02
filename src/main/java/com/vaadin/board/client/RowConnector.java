package com.vaadin.board.client;

import java.util.List;

import com.google.gwt.user.client.Element;
import com.vaadin.board.Row;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

@Connect(Row.class)
public class RowConnector extends AbstractHasComponentsConnector {

    public RowConnector() {
    }

    @Override
    public RowWidget getWidget() {
        return (RowWidget) super.getWidget();
    }

    @Override
    public RowState getState() {
        return (RowState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        // TODO Cols support
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
        RowWidget row = getWidget();

        for (ComponentConnector child : getChildComponents()) {
            row.insert(child.getWidget(), currentIndex++);
        }

        for (ComponentConnector child : previousChildren) {
            if (child.getParent() != this) {
                row.remove(child.getWidget());
            }
        }

    }

    public void forceLayout() {
        for (int i = 0; i < getWidget().getWidgetCount(); i++) {
            forceRowLayout(getWidget().getWidget(i).getElement());
        }
    }

    protected native void forceRowLayout(Element element)
    /*-{
        element._onIronResize();
    }-*/;
}
