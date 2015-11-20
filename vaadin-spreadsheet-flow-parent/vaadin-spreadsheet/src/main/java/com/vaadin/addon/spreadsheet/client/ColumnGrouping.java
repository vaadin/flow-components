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
        top = 9 + level * GroupingWidget.SINGLE_ITEM_SIZE_HEIGHT;
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
