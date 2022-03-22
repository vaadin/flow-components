package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
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
