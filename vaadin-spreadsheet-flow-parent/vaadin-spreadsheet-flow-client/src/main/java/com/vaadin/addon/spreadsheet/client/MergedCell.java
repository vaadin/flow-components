package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
