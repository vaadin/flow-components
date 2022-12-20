/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SheetOverlay extends SimplePanel {

    public static final String SHEET_IMAGE_CLASSNAME = "sheet-image";

    private int col;
    private int row;

    public SheetOverlay(Widget widget, OverlayInfo overlayInfo) {
        widget.setSize("100%", "100%");

        getElement().getStyle().setProperty("pointerEvents", "none");
        widget.getElement().getStyle().setProperty("pointerEvents", "all");

        this.add(widget);

        this.updateSizeLocationPadding(overlayInfo);
    }

    public void updateSizeLocationPadding(OverlayInfo imageInfo) {
        setLocation(imageInfo.col, imageInfo.row);
        setHeight(imageInfo.height);
        setWidth(imageInfo.width);
        setPadding(imageInfo.dx, imageInfo.dy);
    }

    public void setLocation(int col, int row) {
        this.col = col;
        this.row = row;

        setStyleName(SHEET_IMAGE_CLASSNAME);
        String location = "col" + col + " row" + row;
        addStyleName(location);
    }

    public void setWidth(float px) {
        getElement().getStyle().setWidth(px, Unit.PX);
    }

    public void setHeight(float pt) {
        getElement().getStyle().setHeight(pt, Unit.PT);
    }

    public void setPadding(float x, float y) {
        getElement().getStyle().setPaddingLeft(x, Unit.PX);
        getElement().getStyle().setPaddingTop(y, Unit.PT);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
