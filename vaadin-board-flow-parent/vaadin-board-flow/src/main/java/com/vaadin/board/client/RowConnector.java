package com.vaadin.board.client;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.vaadin.board.Row;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.StyleConstants;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

@Connect(Row.class)
public class RowConnector extends AbstractHasComponentsConnector {

    public RowConnector() {

        addIronResizeListener(getWidget().getElement(), () -> {
            LayoutManager.get(getConnection()).setNeedsMeasureRecursively(this);
        });
    }

    private native void addIronResizeListener(Element element, Command listener)
    /*-{
        element.addEventListener("iron-resize", $entry(function() {
            listener.@Command::execute()();
        }));
    }-*/;

    @Override
    public RowWidget getWidget() {
        return (RowWidget) super.getWidget();
    }

    @Override
    public RowState getState() {
        return (RowState) super.getState();
    }

    @Override
    protected void updateWidgetStyleNames() {
        super.updateWidgetStyleNames();

        // Setting v-widget sets display: inline-block which is not wanted
        setWidgetStyleName(StyleConstants.UI_WIDGET, false);
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
}
