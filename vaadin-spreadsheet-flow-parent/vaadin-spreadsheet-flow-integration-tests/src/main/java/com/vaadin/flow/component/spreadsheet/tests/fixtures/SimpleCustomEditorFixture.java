/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class SimpleCustomEditorFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        // spreadsheet.setSpreadsheetComponentFactory(new ComponentFactory());

        List<Cell> cellsToRefresh = new ArrayList<Cell>();
        cellsToRefresh.add(spreadsheet.createCell(0, 0, 1));

        // this has no effect. sheet must be scrolled to "redisplay" the cell.
        spreadsheet.refreshCells(cellsToRefresh);
    }

    // TODO Re-enable when SpreadsheetComponentFactory is done
    // @SuppressWarnings("serial")
    // public static class ComponentFactory
    // implements SpreadsheetComponentFactory {
    //
    // private final TextField textField = new TextField("");
    //
    // @Override
    // public Component getCustomComponentForCell(Cell cell, int rowIndex,
    // int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
    // return null;
    // }
    //
    // @Override
    // public Component getCustomEditorForCell(Cell cell, int rowIndex,
    // int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
    // if (cell != null) {
    // // textField.focus();
    // return textField;
    // }
    // return null;
    // }
    //
    // @Override
    // public void onCustomEditorDisplayed(Cell cell, int rowIndex,
    // int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
    // Component customEditor) {
    // if (cell != null) {
    // String cellValue = spreadsheet.getCellValue(cell);
    // textField.setValue(cellValue);
    // }
    // }
    // }

}
