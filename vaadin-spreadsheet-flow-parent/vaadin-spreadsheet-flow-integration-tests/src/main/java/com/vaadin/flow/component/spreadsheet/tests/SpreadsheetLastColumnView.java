/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

/**
 * View reproducing #9294: hiding the last column while a freeze pane is active
 * left the column's frozen-row cell visible (its content leaking past the grid
 * edge).
 */
@Route("spreadsheet-last-column")
public class SpreadsheetLastColumnView extends VerticalLayout {

    static final int LAST_COLUMN = 3;

    public SpreadsheetLastColumnView() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        createDemoData(sheet);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSizeFull();
        spreadsheet.setMaxRows(20);
        spreadsheet.setMaxColumns(LAST_COLUMN + 1);
        spreadsheet.createFreezePane(1, 1);

        NativeButton hideLastColumn = new NativeButton("Hide last column",
                event -> {
                    spreadsheet.setColumnHidden(LAST_COLUMN, true);
                    spreadsheet.refreshAllCellValues();
                });
        hideLastColumn.setId("hide-last-column");

        setSizeFull();
        add(hideLastColumn, spreadsheet);
        expand(spreadsheet);
    }

    private void createDemoData(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Department");
        header.createCell(2).setCellValue("Salary");
        header.createCell(3).setCellValue("Country");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John");
        row1.createCell(1).setCellValue("IT");
        row1.createCell(2).setCellValue(5000);
        row1.createCell(3).setCellValue("Canada");

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 5000);
    }
}
