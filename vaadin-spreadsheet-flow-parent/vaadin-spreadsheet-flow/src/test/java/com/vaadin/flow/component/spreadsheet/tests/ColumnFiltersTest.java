package com.vaadin.flow.component.spreadsheet.tests;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;

public class ColumnFiltersTest {

    final String TABLE1_RANGE = "B2:B4";
    final String TABLE2_RANGE = "B6:B8";

    private XSSFWorkbook workbook;
    private Spreadsheet spreadsheet;

    @Before
    public void init() {
        workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // create a column with some data and set it as a sheet filter
        Row row1 = sheet.createRow(1);
        row1.createCell(1).setCellValue("col1");

        Row row2 = sheet.createRow(2);
        row2.createCell(1).setCellValue(1);

        Row row3 = sheet.createRow(3);
        row3.createCell(1).setCellValue(2);

        sheet.setAutoFilter(new CellRangeAddress(1, 3, 1, 1));

        // create another column (to be added)as a table
        Row r1 = sheet.createRow(5);
        r1.createCell(1).setCellValue("col");

        Row r2 = sheet.createRow(6);
        r2.createCell(1).setCellValue(1);

        Row r3 = sheet.createRow(7);
        r3.createCell(1).setCellValue(2);

        spreadsheet = new Spreadsheet(workbook);
    }

    @Test
    public void sheetWithFilters_loadWorkbook_filtersPreserved() {
        Assert.assertNotNull(spreadsheet.getTables());
        Assert.assertEquals(1, spreadsheet.getTables().size());
        Assert.assertEquals(CellRangeAddress.valueOf(TABLE1_RANGE),
                spreadsheet.getTables().iterator().next().getFullTableRegion());
    }

    @Test
    public void sheetWithTables_loadWorkbook_tablesPreserved() {
        workbook.getSheetAt(0)
                .createTable(new AreaReference(TABLE2_RANGE, null));

        spreadsheet.setWorkbook(workbook);

        Assert.assertNotNull(spreadsheet.getTables());
        Assert.assertEquals(2, spreadsheet.getTables().size());

        final Iterator<SpreadsheetTable> iterator = spreadsheet.getTables()
                .iterator();

        final CellRangeAddress table1 = iterator.next().getFullTableRegion();

        final CellRangeAddress table2 = iterator.next().getFullTableRegion();

        Assert.assertTrue(CellRangeAddress.valueOf(TABLE1_RANGE).equals(table1)
                || CellRangeAddress.valueOf(TABLE1_RANGE).equals(table2));

        Assert.assertTrue(CellRangeAddress.valueOf(TABLE2_RANGE).equals(table1)
                || CellRangeAddress.valueOf(TABLE2_RANGE).equals(table2));
    }

    @Test
    public void loadFile_filteredColumnsLoadedAsActive() {
        Spreadsheet spr = TestHelper
                .createSpreadsheet("autofilter_with_active_column.xlsx");

        final SpreadsheetTable table = spr.getTables().iterator().next();

        Assert.assertTrue(table.getPopupButton(1).isActive());
        Assert.assertFalse(table.getPopupButton(2).isActive());
    }

    @Test
    public void loadFile_switchSheets_tablesRegisteredOnce() {
        Spreadsheet spreadsheet = TestHelper
                .createSpreadsheet("tables_on_multiple_sheets.xlsx");

        // Go to Sheet2 and back to Sheet1
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.setActiveSheetIndex(0);

        Assert.assertEquals(2, spreadsheet.getTables().size()); // just 2
                                                                // tables, one
                                                                // table per
                                                                // sheet
    }

    @Test
    public void loadFile_deleteTables_switchSheets_tablesStillDeleted() {
        Spreadsheet spreadsheet = TestHelper
                .createSpreadsheet("tables_on_multiple_sheets.xlsx");

        // delete all tables
        var tables = new HashSet<>(spreadsheet.getTables());
        tables.forEach(spreadsheet::deleteTable);

        // Go to Sheet2 and back to Sheet1
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.setActiveSheetIndex(0);

        Assert.assertTrue(spreadsheet.getTables().isEmpty()); // still no tables
    }
}
