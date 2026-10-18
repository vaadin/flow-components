/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;

class GroupingTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = TestHelper.createSpreadsheet("Groupingtest.xlsx");
    }

    @Test
    void expandRow_alreadyExpanded_shouldNotThrow() {
        setActiveSheet(SHEET9);

        expandRow(SHEET9_ROW_GROUP);
    }

    @Test
    void collapseRow_rowHidden() {
        setActiveSheet(SHEET9);

        collapseRow(SHEET9_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET9_ROW));
    }

    @Test
    void collapseRow_onASheetWithPropertiesButNoOutlineSettings_rowHidden() {
        XSSFSheet sheet = (XSSFSheet) spreadsheet.getWorkbook()
                .getSheetAt(SHEET9);
        CTWorksheet worksheet = sheet.getCTWorksheet();
        CTSheetPr properties = worksheet.isSetSheetPr() ? worksheet.getSheetPr()
                : worksheet.addNewSheetPr();
        if (properties.isSetOutlinePr()) {
            properties.unsetOutlinePr();
        }
        setActiveSheet(SHEET9);

        collapseRow(SHEET9_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET9_ROW));
    }

    @Test
    void collapseRow_expandRow_rowVisible() {
        setActiveSheet(SHEET9);

        collapseRow(SHEET9_ROW_GROUP);
        expandRow(SHEET9_ROW_GROUP);
        Assertions.assertFalse(spreadsheet.isRowHidden(SHEET9_ROW));
    }

    @Test
    void collapseRow_collapseParentRow_expandRow_rowHidden() {
        setActiveSheet(SHEET4);
        // Collapse child row group
        collapseRow(SHEET4_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);

        // Expand child row group
        expandRow(SHEET4_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    void collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        setActiveSheet(SHEET4);
        // Collapse child row group
        collapseRow(SHEET4_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);

        // Expand parent row group
        expandRow(SHEET4_PARENT_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    void collapseParentRow_expandParentRow_rowVisible() {
        setActiveSheet(SHEET4);
        // Collapse parent row group
        collapseRow(SHEET4_PARENT_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET4_ROW));

        // Expand Parent row group
        expandRow(SHEET4_PARENT_ROW_GROUP);
        Assertions.assertFalse(spreadsheet.isRowHidden(SHEET4_ROW));
    }

    @Test
    void inverted_collapseRow_rowHidden() {
        setActiveSheet(SHEET7);

        collapseRow(SHEET7_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    void inverted_collapseRow_expandRow_rowVisible() {
        setActiveSheet(SHEET7);

        collapseRow(SHEET7_ROW_GROUP);
        expandRow(SHEET7_ROW_GROUP);
        Assertions.assertFalse(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    void inverted_collapseRow_collapseParentRow_expandRow_rowHidden() {
        setActiveSheet(SHEET7);

        // Collapse child row group
        collapseRow(SHEET7_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);

        // Expand child row group
        expandRow(SHEET7_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    void inverted_collapseRow_collapseParentRow_expandParentRow_rowHidden() {
        setActiveSheet(SHEET7);

        // Collapse child row group
        collapseRow(SHEET7_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);

        // Expand parent row group
        expandRow(SHEET7_PARENT_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    void inverted_collapseParentRow_expandParentRow_rowVisible() {
        setActiveSheet(SHEET7);

        // Collapse parent row group
        collapseRow(SHEET7_PARENT_ROW_GROUP);
        Assertions.assertTrue(spreadsheet.isRowHidden(SHEET7_ROW));

        // Expand parent row group
        expandRow(SHEET7_PARENT_ROW_GROUP);
        Assertions.assertFalse(spreadsheet.isRowHidden(SHEET7_ROW));
    }

    @Test
    void expandColumn_alreadyExpanded_shouldNotThrow() {
        setActiveSheet(SHEET9);

        expandColumn(SHEET9_COLUMN_GROUP);
    }

    @Test
    void collapseColumn_columnHidden() {
        setActiveSheet(SHEET9);

        collapseColumn(SHEET9_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET9_COLUMN));
    }

    @Test
    void collapseColumn_expandColumn_columnVisible() {
        setActiveSheet(SHEET9);

        collapseColumn(SHEET9_COLUMN_GROUP);
        expandColumn(SHEET9_COLUMN_GROUP);
        Assertions.assertFalse(spreadsheet.isColumnHidden(SHEET9_COLUMN));
    }

    @Test
    void collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        setActiveSheet(SHEET6);
        // Collapse child column group
        collapseColumn(SHEET6_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);

        // Expand child column group
        expandColumn(SHEET6_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Test
    void collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        setActiveSheet(SHEET6);
        // Collapse child column group
        collapseColumn(SHEET6_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);

        // Expand parent column group
        expandColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Disabled("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    @Test
    void collapseParentColumn_expandParentColumn_columnVisible() {
        setActiveSheet(SHEET6);
        // Collapse parent column group
        collapseColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET6_COLUMN));

        // Expand Parent column group
        expandColumn(SHEET6_COLUMN_PARENT_GROUP);
        Assertions.assertFalse(spreadsheet.isColumnHidden(SHEET6_COLUMN));
    }

    @Test
    void inverted_collapseColumn_columnHidden() {
        setActiveSheet(SHEET7);

        collapseColumn(SHEET7_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    void inverted_collapseColumn_expandColumn_columnVisible() {
        setActiveSheet(SHEET7);

        collapseColumn(SHEET7_COLUMN_GROUP);
        expandColumn(SHEET7_COLUMN_GROUP);
        Assertions.assertFalse(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    void inverted_collapseColumn_collapseParentColumn_expandColumn_columnHidden() {
        setActiveSheet(SHEET7);

        // Collapse child column group
        collapseColumn(SHEET7_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);

        // Expand child column group
        expandColumn(SHEET7_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    void inverted_collapseColumn_collapseParentColumn_expandParentColumn_columnHidden() {
        setActiveSheet(SHEET7);

        // Collapse child column group
        collapseColumn(SHEET7_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);

        // Expand parent column group
        expandColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    @Disabled("bug in GroupingUtil, collapsing and expanding parent column should not result with collapsed child columns")
    void inverted_collapseParentColumn_expandParentColumn_columnVisible() {
        setActiveSheet(SHEET7);

        // Collapse parent column group
        collapseColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assertions.assertTrue(spreadsheet.isColumnHidden(SHEET7_COLUMN));

        // Expand parent column group
        expandColumn(SHEET7_PARENT_COLUMN_GROUP);
        Assertions.assertFalse(spreadsheet.isColumnHidden(SHEET7_COLUMN));
    }

    @Test
    void clickRowLevelHeader_shouldNotHidePrecedingGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        Assertions.assertFalse(spreadsheet.isRowHidden(6));
    }

    @Test
    void clickRowLevelHeader_shouldHideFollowingGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        Assertions.assertTrue(spreadsheet.isRowHidden(4));
    }

    @Test
    void clickRowLevelHeader_clickLastLevelHeader_shouldUnhideGroups() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);
        clickRowLevelHeader(8);
        Assertions.assertFalse(spreadsheet.isRowHidden(4));
    }

    @Test
    void clickColumnLevelHeader_shouldNotHidePrecedingGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        Assertions.assertFalse(spreadsheet.isColumnHidden(6));
    }

    @Test
    void clickColumnLevelHeader_shouldHideFollowingGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        Assertions.assertTrue(spreadsheet.isColumnHidden(4));
    }

    @Test
    void clickColumnLevelHeader_clickLastLevelHeader_shouldUnhideGroups() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);
        clickColumnLevelHeader(8);
        Assertions.assertFalse(spreadsheet.isColumnHidden(4));
    }

    /** Types into A1, as the user does: the history then holds an edit. */
    private void typeIntoA1() {
        spreadsheet.setSelection("A1");
        spreadsheet.getCellValueManager().onCellValueChange(1, 1, "typed");
    }

    @Test
    void clickRowLevelHeader_keepsTheUndoHistory() {
        // The click used to load the whole workbook again, which emptied the
        // history.
        setActiveSheet(SHEET6);
        typeIntoA1();

        clickRowLevelHeader(4);

        Assertions.assertTrue(
                spreadsheet.getSpreadsheetHistoryManager().canUndo());
    }

    @Test
    void clickColumnLevelHeader_keepsTheUndoHistory() {
        setActiveSheet(SHEET6);
        typeIntoA1();

        clickColumnLevelHeader(4);

        Assertions.assertTrue(
                spreadsheet.getSpreadsheetHistoryManager().canUndo());
    }

    @Test
    void clickRowLevelHeader_keepsATableTheApplicationRegistered() {
        setActiveSheet(SHEET6);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                CellRangeAddress.valueOf("J1:K5"));
        spreadsheet.registerTable(table);

        clickRowLevelHeader(4);

        Assertions.assertTrue(spreadsheet.getTables().contains(table));
    }

    @Test
    void clickRowLevelHeader_tellsTheClientWhichRowsAreHidden() {
        setActiveSheet(SHEET6);

        clickRowLevelHeader(4);

        // The client numbers rows from 1: row 4 hidden, row 6 shown.
        String hidden = spreadsheet.getElement()
                .getProperty("hiddenRowIndexes");
        Assertions.assertTrue(hidden.matches(".*[\\[,]5[,\\]].*"), hidden);
        Assertions.assertFalse(hidden.matches(".*[\\[,]7[,\\]].*"), hidden);
    }

    @Test
    void clickColumnLevelHeader_tellsTheClientWhichColumnsAreHidden() {
        setActiveSheet(SHEET6);

        clickColumnLevelHeader(4);

        String hidden = spreadsheet.getElement()
                .getProperty("hiddenColumnIndexes");
        Assertions.assertTrue(hidden.matches(".*[\\[,]5[,\\]].*"), hidden);
        Assertions.assertFalse(hidden.matches(".*[\\[,]7[,\\]].*"), hidden);
    }

    @Test
    void groupEndingOnLastRow_groupSpansToLastRow() {
        spreadsheet = createSpreadsheetWithGroupEndingOnLastRow();

        Assertions.assertEquals(
                "[{\"collapsed\":false,\"endIndex\":" + (ROW_COUNT - 1)
                        + ",\"level\":1,\"startIndex\":" + LAST_ROW_GROUP
                        + ",\"uniqueIndex\":" + LAST_ROW_GROUP + "}]",
                spreadsheet.getElement().getProperty("rowGroupingData"));
    }

    @Test
    void groupEndingOnLastRow_clickRowLevelHeaders_rowHiddenAndVisibleAgain() {
        spreadsheet = createSpreadsheetWithGroupEndingOnLastRow();

        clickRowLevelHeader(1);
        Assertions.assertTrue(spreadsheet.isRowHidden(ROW_COUNT - 1));
        clickRowLevelHeader(2);
        Assertions.assertFalse(spreadsheet.isRowHidden(ROW_COUNT - 1));
    }

    /**
     * Creates a spreadsheet with a single row group that ends on the last row
     * of the sheet, which is also the last row of the component.
     */
    private Spreadsheet createSpreadsheetWithGroupEndingOnLastRow() {
        var workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int i = 0; i < ROW_COUNT; i++) {
            sheet.createRow(i).createCell(0).setCellValue("Row " + (i + 1));
        }
        sheet.groupRow(LAST_ROW_GROUP, ROW_COUNT - 1);

        // no empty rows are added after the last row of the sheet
        var result = new Spreadsheet(ROW_COUNT, COLUMN_COUNT);
        result.setWorkbook(workbook);
        TestHelper.fireClientEvent(result, "onSheetScroll", "[1, 1, 1, 1]");
        return result;
    }

    private void setActiveSheet(int sheetIndex) {
        spreadsheet.setActiveSheetIndex(sheetIndex);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 1, \"lastColumn\": 1}");
    }

    private void expandRow(int row) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "{\"cols\": false, \"colIndex\": " + row
                        + ", \"collapsed\": false}");
    }

    private void collapseRow(int row) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "{\"cols\": false, \"colIndex\": " + row
                        + ", \"collapsed\": true}");
    }

    private void expandColumn(int column) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "{\"cols\": true, \"colIndex\": " + column
                        + ", \"collapsed\": false}");
    }

    private void collapseColumn(int column) {
        TestHelper.fireClientEvent(spreadsheet, "groupingCollapsed",
                "{\"cols\": true, \"colIndex\": " + column
                        + ", \"collapsed\": true}");
    }

    private void clickRowLevelHeader(int level) {
        TestHelper.fireClientEvent(spreadsheet, "levelHeaderClicked",
                "{\"cols\": false, \"level\": " + level + "}");
    }

    private void clickColumnLevelHeader(int level) {
        TestHelper.fireClientEvent(spreadsheet, "levelHeaderClicked",
                "{\"cols\": true, \"level\": " + level + "}");
    }

    private final int SHEET4 = 3;
    private final int SHEET4_ROW_GROUP = 1;
    private final int SHEET4_ROW = 2;
    private final int SHEET4_PARENT_ROW_GROUP = 0;

    private final int SHEET6 = 5;
    private final int SHEET6_COLUMN_GROUP = 0;
    private final int SHEET6_COLUMN = 1;
    private final int SHEET6_COLUMN_PARENT_GROUP = 2;

    private final int SHEET7 = 6;
    private final int SHEET7_ROW_GROUP = 3;
    private final int SHEET7_ROW = 3;
    private final int SHEET7_PARENT_ROW_GROUP = 2;
    private final int SHEET7_COLUMN_GROUP = 2;
    private final int SHEET7_COLUMN = 3;
    private final int SHEET7_PARENT_COLUMN_GROUP = 1;

    private final int SHEET9 = 8;
    private final int SHEET9_ROW_GROUP = 1;
    private final int SHEET9_ROW = 3;
    private final int SHEET9_COLUMN_GROUP = 1;
    private final int SHEET9_COLUMN = 3;

    private final int ROW_COUNT = 10;
    private final int COLUMN_COUNT = 5;
    private final int LAST_ROW_GROUP = ROW_COUNT - 4;

}
