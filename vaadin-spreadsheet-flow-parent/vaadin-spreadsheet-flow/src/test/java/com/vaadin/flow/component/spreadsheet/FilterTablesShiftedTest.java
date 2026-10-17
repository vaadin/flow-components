/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.tests.MockUIExtension;

/**
 * Filter tables over rows that are shifted, as inserting and deleting rows
 * shifts them: the table, its pop-up buttons and its filters move with the
 * rows, the rows a filter hides stay hidden where they went, and the workbook
 * has the filter and its tables there too. A table shrunk keeps the rows it
 * needs, so that the workbook loads again.
 */
class FilterTablesShiftedTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    /**
     * B2:D2 hold "Column 1" to "Column 3", and rows 3 to 5 hold their row
     * number times ten plus their column: B3 holds 21, D5 holds 43.
     */
    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        for (int column = 1; column <= 3; column++) {
            spreadsheet.createCell(1, column, "Column " + column);
            for (int row = 2; row <= 4; row++) {
                spreadsheet.createCell(row, column, row * 10 + column);
            }
        }
        ui.add(spreadsheet);
    }

    /** A filter table over B2:D5, registered. */
    private SpreadsheetFilterTable filterTable() {
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                new CellRangeAddress(1, 4, 1, 3));
        spreadsheet.registerTable(table);
        return table;
    }

    private static ItemFilter itemFilter(SpreadsheetFilterTable table,
            int column) {
        return (ItemFilter) table.getPopupButton(column).getContent()
                .getChildren().findFirst().get();
    }

    @SuppressWarnings("unchecked")
    private static CheckboxGroup<String> values(SpreadsheetFilterTable table,
            int column) {
        return (CheckboxGroup<String>) itemFilter(table, column).getChildren()
                .filter(component -> component instanceof CheckboxGroup)
                .findFirst().get();
    }

    /** A sheet whose B2:F2 hold "Head 1" to "Head 5", and B3:F6 values. */
    private static XSSFSheet sheetWithValues(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet();
        for (int row = 1; row <= 5; row++) {
            XSSFRow poiRow = sheet.createRow(row);
            for (int column = 1; column <= 5; column++) {
                poiRow.createCell(column).setCellValue(
                        row == 1 ? "Head " + column : "v" + row + column);
            }
        }
        return sheet;
    }

    private static XSSFTable table(XSSFSheet sheet, String range) {
        return sheet.createTable(
                new AreaReference(range, SpreadsheetVersion.EXCEL2007));
    }

    private static void assertLoadsAgain(XSSFWorkbook workbook)
            throws IOException {
        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        workbook.write(saved);
        assertDoesNotThrow(() -> new Spreadsheet(
                new ByteArrayInputStream(saved.toByteArray())));
    }

    @Test
    void rowsInsertedAbove_moveTheTable_andItsButtons() {
        SpreadsheetFilterTable table = filterTable();

        spreadsheet.shiftRows(0, 10, 1);

        assertEquals("B3:D6", table.getFullTableRegion().formatAsString());
        assertEquals("B4:D6", table.getFilteringRegion().formatAsString());
        assertNotNull(table.getPopupButton(
                new CellReference(spreadsheet.getActiveSheet().getSheetName(),
                        2, 1, true, true)));
        assertEquals(3, table.getPopupButtons().size());
    }

    @Test
    void aFilter_offersTheValuesOfItsRowsWhereTheyWent() {
        SpreadsheetFilterTable table = filterTable();

        spreadsheet.shiftRows(0, 10, 1);
        itemFilter(table, 1).updateOptions();

        assertEquals(Set.of("21", "31", "41"), values(table, 1)
                .getListDataView().getItems().collect(Collectors.toSet()));
    }

    @Test
    void theRowsFiltersHide_stayHiddenWhereTheyWent() {
        // B=31 hidden, rows moved down by one, then C=42 hidden: the rows of
        // 31 and of 42 are hidden, not that of 21.
        SpreadsheetFilterTable table = filterTable();
        values(table, 1).deselect("31");
        assertTrue(spreadsheet.isRowHidden(3));

        spreadsheet.shiftRows(0, 10, 1);
        values(table, 2).deselect("42");

        assertFalse(spreadsheet.isRowHidden(3));
        assertTrue(spreadsheet.isRowHidden(4));
        assertTrue(spreadsheet.isRowHidden(5));
    }

    @Test
    void aFilterCleared_afterTheRowsMoved_showsTheRowItHid() {
        SpreadsheetFilterTable table = filterTable();
        values(table, 1).deselect("31");

        spreadsheet.shiftRows(0, 10, 1);
        values(table, 2).deselect("42");
        values(table, 1).select("31");

        assertFalse(spreadsheet.isRowHidden(4));
        assertTrue(spreadsheet.isRowHidden(5));
    }

    @Test
    void aRowAFilterHid_deleted_isNoLongerFiltered() {
        // B=31 hidden, then rows from 5 up by one, as deleting row 4 moves
        // them.
        SpreadsheetFilterTable table = filterTable();
        values(table, 1).deselect("31");

        spreadsheet.shiftRows(4, 10, -1);

        assertEquals(Set.of(), itemFilter(table, 1).getFilteredRows());
    }

    @Test
    void aRowInsertedUnderTheHeader_isFiltered() {
        // Rows from 3 down by one, and 99 typed into the new B3.
        SpreadsheetFilterTable table = filterTable();

        spreadsheet.shiftRows(2, 10, 1);
        spreadsheet.createCell(2, 1, 99);
        itemFilter(table, 1).updateOptions();
        values(table, 1).deselect("99");

        assertEquals("B2:D6", table.getFullTableRegion().formatAsString());
        assertEquals("B3:D6", table.getFilteringRegion().formatAsString());
        assertTrue(spreadsheet.isRowHidden(2));
    }

    @Test
    void theRowsUnderTheHeaderDeleted_leaveOneToFilter() {
        // Rows from 6 up by three, as deleting rows 3 to 5 moves them.
        SpreadsheetFilterTable table = filterTable();

        spreadsheet.shiftRows(5, 10, -3);

        assertEquals("B2:D3", table.getFullTableRegion().formatAsString());
        assertEquals("B3:D3", table.getFilteringRegion().formatAsString());
    }

    @Test
    void aTableTheRowsMovedOverWhole_keepsItsPlace() {
        // Rows from 6 up by four, over rows 2 to 5, the table's.
        SpreadsheetFilterTable table = filterTable();

        spreadsheet.shiftRows(5, 10, -4);

        assertEquals("B2:D5", table.getFullTableRegion().formatAsString());
    }

    @Test
    void aTableOfOneRow_staysOneRow() {
        SpreadsheetTable table = new SpreadsheetTable(spreadsheet,
                CellRangeAddress.valueOf("B2:D2"));
        spreadsheet.registerTable(table);

        spreadsheet.shiftRows(0, 10, 1);

        assertEquals("B3:D3", table.getFullTableRegion().formatAsString());
    }

    @Test
    void aTableOfAnotherSheet_staysWhereItIs() {
        spreadsheet.createNewSheet("Second", 10, 10);
        spreadsheet.setActiveSheetIndex(0);
        SpreadsheetTable table = new SpreadsheetTable(spreadsheet,
                spreadsheet.getWorkbook().getSheetAt(1),
                CellRangeAddress.valueOf("B2:D5"));
        spreadsheet.registerTable(table);

        spreadsheet.shiftRows(0, 10, 1);

        assertEquals("B2:D5", table.getFullTableRegion().formatAsString());
    }

    @Test
    void aTableOfAnHssfSheet_movesWithItsRows() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet();
        Spreadsheet shown = new Spreadsheet(workbook);
        SpreadsheetTable table = new SpreadsheetTable(shown,
                CellRangeAddress.valueOf("B2:D5"));
        shown.registerTable(table);

        shown.shiftRows(0, 10, 1);

        assertEquals("B3:D6", table.getFullTableRegion().formatAsString());
    }

    @Test
    void theWorkbook_hasItsFilterAndTablesWhereTheRowsWent() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = sheetWithValues(workbook);
        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:C4"));
        XSSFTable table = table(sheet, "E2:F4");
        Spreadsheet shown = new Spreadsheet(workbook);

        shown.shiftRows(0, 10, 2);

        assertEquals("B4:C6", sheet.getCTWorksheet().getAutoFilter().getRef());
        assertEquals("E4:F6", table.getCTTable().getRef());
    }

    @Test
    void theLastRowUnderTheHeadersDeleted_leavesOneInTheWorkbook()
            throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = sheetWithValues(workbook);
        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:C3"));
        XSSFTable table = table(sheet, "E2:F3");
        Spreadsheet shown = new Spreadsheet(workbook);

        // Rows from 4 up by one, as deleting row 3 moves them.
        shown.shiftRows(3, 10, -1);

        assertEquals("B2:C3", sheet.getCTWorksheet().getAutoFilter().getRef());
        assertEquals("E2:F3", table.getCTTable().getRef());
        assertLoadsAgain(workbook);
    }

    @Test
    void aTableWithATotalsRow_keepsTheRowsPoiRequires() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFTable table = table(sheetWithValues(workbook), "E2:F4");
        table.getCTTable().setTotalsRowCount(1);
        Spreadsheet shown = new Spreadsheet(workbook);

        // Rows from 4 up by one, as deleting row 3 moves them.
        shown.shiftRows(3, 10, -1);

        assertEquals("E2:F4", table.getCTTable().getRef());
        assertEquals("E2:F4", shown.getTables().iterator().next()
                .getFullTableRegion().formatAsString());
    }

    @Test
    void aTableWithoutAHeaderRow_keepsTwoRows() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFTable table = table(sheetWithValues(workbook), "E2:F3");
        table.getCTTable().setHeaderRowCount(0);
        Spreadsheet shown = new Spreadsheet(workbook);

        // Rows from 4 up by one, as deleting row 3 moves them.
        shown.shiftRows(3, 10, -1);

        assertEquals("E2:F3", table.getCTTable().getRef());
        assertLoadsAgain(workbook);
    }
}
