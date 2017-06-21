package com.vaadin.board.client;

/*
 * #%L
 * Vaadin Board
 * %%
 * Copyright (C) 2017 Vaadin Ltd
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.vaadin.board.Row;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.StyleConstants;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;

@Connect(Row.class)
public class RowConnector extends AbstractHasComponentsConnector {

    public RowConnector() {
        addIronResizeListener(getWidget().getElement(),
                              () -> LayoutManager.get(RowConnector.this.getConnection()).setNeedsMeasureRecursively(RowConnector.this));
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
        for (Connector connector : getState().cols.keySet()) {
            int colsValue = getState().cols.get(connector);
            String strValue = "" + colsValue;
            ((ComponentConnector) connector)
                .getWidget()
                .getElement()
                .setAttribute("board-cols", strValue);
        }
        getWidget().redraw(getWidget().getElement());
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

        // Force shady DOM to distribute the child elements immediately so e.g.
        // a Grid child can correctly calculated its height
        BoardConnector.flushShadyDOM();

    }
}
