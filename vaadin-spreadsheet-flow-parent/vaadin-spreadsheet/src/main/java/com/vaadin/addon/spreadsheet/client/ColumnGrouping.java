package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Class that represents a single column grouping indicator.
 * 
 * @author Thomas Mattsson / Vaadin LTD
 *
 */
public class ColumnGrouping extends GroupingWidget {

    /**
     * @see GroupingWidget#GroupingWidget(int, GroupingHandler)
     */
    public ColumnGrouping(final int colIndex, final GroupingHandler handler) {
        super(colIndex, handler);
    }

    @Override
    public void setPos(int offset, int level) {
        top = getTotalSize(level);
        left = offset;
        getElement().getStyle().setTop(top, Unit.PX);
        getElement().getStyle().setLeft(offset, Unit.PX);
    }

    @Override
    protected void setSize(double size) {
        getElement().getStyle().setWidth(size, Unit.PX);
        width = size;
    }

    @Override
    protected void setMargin(double size) {
        getElement().getStyle().setMarginLeft(size, Unit.PX);
        marginLeft = size;
    }

    @Override
    protected boolean isCols() {
        return true;
    }

    @Override
    public GroupingWidget cloneWidget() {
        ColumnGrouping w = new ColumnGrouping(getIndex(), handler);
        copyfields(w);
        return w;
    }
}
