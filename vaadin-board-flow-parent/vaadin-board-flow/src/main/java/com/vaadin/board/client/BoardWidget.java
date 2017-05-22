package com.vaadin.board.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class BoardWidget extends FlowPanel {

    public BoardWidget() {
        super("vaadin-board");
    }

    @Override
    protected void add(Widget child, Element container) {
        if (!(child instanceof RowWidget)) {
            throw new IllegalArgumentException(
                    getClass().getName() + " only supports children of type "
                            + RowWidget.class.getName());
        }
        super.add(child, container);
    }

    private native void redraw(Element elem)/*-{
        elem.redraw();
    }-*/;

    /**
     * Calls the redraw of the Web Component board.
     */
    public void redraw() {
        this.redraw(getElement());
    }
}