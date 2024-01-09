/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Class that represents a single row grouping indicator.
 *
 * @author Thomas Mattsson / Vaadin LTD
 *
 */
public class RowGrouping extends GroupingWidget {

    /**
     * @see GroupingWidget#GroupingWidget(int, GroupingHandler)
     */
    public RowGrouping(final int rowIndex, final GroupingHandler handler) {
        super(rowIndex, handler);
    }

    @Override
    public void setPos(int offset, int level) {
        left = 6 + level * GroupingWidget.SINGLE_ITEM_SIZE_WIDTH;
        top = offset;

        getElement().getStyle().setLeft(left, Unit.PX);
        getElement().getStyle().setTop(offset, Unit.PX);
    }

    @Override
    protected void setSize(double size) {
        getElement().getStyle().setHeight(size, Unit.PX);
        height = size;
    }

    @Override
    protected void setMargin(double size) {
        getElement().getStyle().setMarginTop(size, Unit.PX);
        marginTop = size;
    }

    @Override
    protected boolean isCols() {
        return false;
    }

    @Override
    public GroupingWidget cloneWidget() {
        RowGrouping w = new RowGrouping(getIndex(), handler);
        copyfields(w);
        return w;
    }

}
