package com.vaadin.addon.spreadsheet.client;

public class MergedCell extends Cell {

    public MergedCell(int col, int row) {
        super(col, row);
    }

    @Override
    protected void updateClassName() {
        getElement().setClassName(
                SheetWidget.toKey(getCol(), getRow()) + " cell "
                        + getCellStyle() + " "
                        + SheetWidget.MERGED_CELL_CLASSNAME);
    }
}
