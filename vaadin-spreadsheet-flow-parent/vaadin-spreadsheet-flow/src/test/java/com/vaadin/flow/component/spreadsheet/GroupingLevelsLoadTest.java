/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * How many outline levels the client draws buttons for, read from the groups of
 * the file: one more for each group nested in another.
 */
class GroupingLevelsLoadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    /**
     * Ten rows of ten values, made before grouping: a row made again loses its
     * group.
     */
    private final XSSFSheet sheet = filled(workbook.createSheet());

    private static XSSFSheet filled(XSSFSheet sheet) {
        for (int r = 0; r < 10; r++) {
            var row = sheet.createRow(r);
            for (int c = 0; c < 10; c++) {
                row.createCell(c).setCellValue(r * 10 + c);
            }
        }
        return sheet;
    }

    private Spreadsheet show() {
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        return spreadsheet;
    }

    @Test
    void groupsInsideGroups_giveTwoLevels() {
        sheet.groupRow(1, 6);
        sheet.groupRow(2, 4);
        sheet.groupColumn(1, 6);
        sheet.groupColumn(2, 4);

        Spreadsheet spreadsheet = show();

        assertEquals(2, spreadsheet.getRowGroupingMax());
        assertEquals(2, spreadsheet.getColGroupingMax());
    }

    @Test
    void groupsSideBySide_giveOneLevel() {
        sheet.groupRow(1, 2);
        sheet.groupRow(4, 5);
        sheet.groupColumn(1, 2);
        sheet.groupColumn(4, 5);

        Spreadsheet spreadsheet = show();

        assertEquals(1, spreadsheet.getRowGroupingMax());
        assertEquals(1, spreadsheet.getColGroupingMax());
    }

    @Test
    void noGroups_giveNoLevels() {
        Spreadsheet spreadsheet = show();

        assertEquals(0, spreadsheet.getRowGroupingMax());
        assertEquals(0, spreadsheet.getColGroupingMax());
    }
}
