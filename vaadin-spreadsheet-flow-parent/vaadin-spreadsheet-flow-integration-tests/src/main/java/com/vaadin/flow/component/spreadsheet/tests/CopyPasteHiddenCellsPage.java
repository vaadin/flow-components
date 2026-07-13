/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.router.Route;

/**
 * Manual test page for copying, cutting and pasting cells with hidden rows /
 * columns (https://github.com/vaadin/flow-components/issues/9327).
 * <p>
 * Filter the table or hide a row / column with the buttons, then copy a range
 * and paste it elsewhere. Hidden cells should not be copied, and nothing
 * should be pasted into hidden cells.
 */
@Route("vaadin-spreadsheet/copy-paste-hidden-cells")
public class CopyPasteHiddenCellsPage extends Div {

    public CopyPasteHiddenCellsPage() {
        setSizeFull();

        Spreadsheet spreadsheet = new Spreadsheet();

        String[] header = { "Name", "City", "Age" };
        Object[][] data = { { "Alice", "Berlin", 34 },
                { "Bob", "Helsinki", 28 }, { "Carol", "Berlin", 41 },
                { "Dan", "Turku", 25 }, { "Eve", "Helsinki", 37 },
                { "Frank", "Berlin", 30 } };
        for (int col = 0; col < header.length; col++) {
            spreadsheet.createCell(0, col, header[col]);
        }
        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < data[row].length; col++) {
                spreadsheet.createCell(row + 1, col, data[row][col]);
            }
        }

        CellRangeAddress tableRange = new CellRangeAddress(0, data.length, 0,
                header.length - 1);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                spreadsheet.getActiveSheet(), tableRange);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();

        Button toggleRow = new Button("Toggle row 3 hidden",
                e -> spreadsheet.setRowHidden(2,
                        !spreadsheet.isRowHidden(2)));
        toggleRow.setId("toggle-row");
        Button toggleColumn = new Button("Toggle column B hidden",
                e -> spreadsheet.setColumnHidden(1,
                        !spreadsheet.isColumnHidden(1)));
        toggleColumn.setId("toggle-column");

        spreadsheet.setHeight("500px");
        add(new Div(toggleRow, toggleColumn), spreadsheet);
    }
}
