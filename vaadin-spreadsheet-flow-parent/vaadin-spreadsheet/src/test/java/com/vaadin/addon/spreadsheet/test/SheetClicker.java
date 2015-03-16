package com.vaadin.addon.spreadsheet.test;

public interface SheetClicker {

    public void clickCell(String cell);

    public void clickRow(int row);

    public void clickColumn(String column);
}
