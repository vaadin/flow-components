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

public class MergedCell extends Cell {

    public MergedCell(SheetWidget sheetWidget, int col, int row) {
        super(sheetWidget, col, row);
    }

    @Override
    protected void updateClassName() {
        getElement().setClassName(
                SheetWidget.toKey(getCol(), getRow()) + " cell "
                        + getCellStyle() + " "
                        + SheetWidget.MERGED_CELL_CLASSNAME);
    }
}
