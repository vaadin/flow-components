package com.vaadin.board.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.StyleInjector;
import com.vaadin.board.Board;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

@Connect(Board.class)
public class BoardConnector extends AbstractHasComponentsConnector {

    private static Set<String> loaddedCss = new HashSet<>();
    public BoardConnector() {
        final String HIDE_LICENSE_CSS = "vaadin-license-dialog{ display:none}\n vaadin-license-box{ display:none}";
        if(!loaddedCss.contains(HIDE_LICENSE_CSS)) {
            StyleInjector.inject(HIDE_LICENSE_CSS);
            loaddedCss.add(HIDE_LICENSE_CSS);
        }
    }

    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getWidget().getElement(), e-> {
            getWidget().redraw();
        });
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
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
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

        // Force shady DOM to distribute the child elements immediately so e.g.
        // a Grid child can correctly calculated its height
        flushShadyDOM();

        // Board does not layout immediately on initial paint
        Scheduler.get().scheduleDeferred(() -> LayoutManager.get(getConnection()).setNeedsMeasureRecursively(this));
    }

    /**
     * Calls window.ShadyDOM.flush, if it is available.
     */
    public static native void flushShadyDOM()
    /*-{
        if ($wnd.ShadyDOM) {
            $wnd.ShadyDOM.flush();
        }
    }-*/;

}