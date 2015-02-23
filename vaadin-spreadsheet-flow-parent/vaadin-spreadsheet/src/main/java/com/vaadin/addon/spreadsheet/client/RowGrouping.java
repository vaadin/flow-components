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
        left = getTotalSize(level);
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
