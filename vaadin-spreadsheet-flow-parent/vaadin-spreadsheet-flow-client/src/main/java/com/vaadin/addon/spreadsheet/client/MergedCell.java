/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

public class MergedCell extends Cell {

    public MergedCell(SheetWidget sheetWidget, int col, int row) {
        super(sheetWidget, col, row);
    }

    @Override
    protected void updateClassName() {
        getElement().setClassName(SheetWidget.toKey(getCol(), getRow())
                + " cell " + getCellStyle() + " "
                + SheetWidget.MERGED_CELL_CLASSNAME);
    }

    @Override
    protected int getCellWidth() {
        // could potentially be 0 (see comment in WrongHashesOnScrollTest.java),
        // but I found no easy reliable way to get the width of a merged cell,
        // could be rethought later
        return getElement().getClientWidth();
    }
}
