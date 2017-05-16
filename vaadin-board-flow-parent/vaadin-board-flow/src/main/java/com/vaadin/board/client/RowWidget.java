package com.vaadin.board.client;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;

public class RowWidget extends FlowPanel {

    public RowWidget() {
        super("vaadin-board-row");
    }

    public native void redraw(Element elem)/*-{
        elem.redraw();
    }-*/;

}
