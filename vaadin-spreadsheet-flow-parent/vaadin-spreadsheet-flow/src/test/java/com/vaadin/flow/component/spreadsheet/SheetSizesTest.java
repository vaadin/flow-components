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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.dom.Element;

/**
 * The heights and widths the rows and columns of a sheet are shown at, and
 * which are hidden, as the sheet gives them.
 */
class SheetSizesTest {

    @Test
    void aSheetOfMoreRowsAndColumnsThanTheDefaults_isShownWithAllOfThem() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(299).createCell(69);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(300, spreadsheet.getRows());
        assertEquals(300, spreadsheet.getRowH().length);
        assertEquals(70, spreadsheet.getColumns());
        assertEquals(70, spreadsheet.getColW().length);
    }

    @Test
    void aMillionRowsOfOneHeight_goToTheClientInAFewBytes() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(1_048_575);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(1_048_576, spreadsheet.getRowH().length);
        String rowH = spreadsheet.getElement().getProperty("rowH");
        assertTrue(rowH.length() < 100, rowH);
    }

    @Test
    void aSheetOfNoDefaultRowHeight_isGivenTheComponentsDefault() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().setDefaultRowHeightInPoints(0);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        float componentsDefault = SpreadsheetFactory.DEFAULT_ROW_HEIGHT_POINTS;
        assertEquals(componentsDefault, spreadsheet.getDefaultRowHeight());
        assertEquals(componentsDefault, spreadsheet.getRowH()[0]);
    }

    @Test
    void theRowsAndColumnsHiddenOnASheet_areNotHiddenOnTheNext() {
        // Hidden past the rows and columns the next sheet has.
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet first = workbook.createSheet();
        first.createRow(300).createCell(60);
        first.getRow(300).setZeroHeight(true);
        first.setColumnHidden(60, true);
        workbook.createSheet();
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        assertEquals(List.of(301), spreadsheet.getHiddenRowIndexes());
        assertEquals(List.of(61), spreadsheet.getHiddenColumnIndexes());

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of(), spreadsheet.getHiddenRowIndexes());
        assertEquals(List.of(), spreadsheet.getHiddenColumnIndexes());
    }

    @Test
    void theSizes_goToTheClient() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).setHeightInPoints(20);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        spreadsheet.setColumnWidth(1, 100);
        spreadsheet.setColumnHidden(2, true);
        spreadsheet.setRowHidden(3, true);

        Element element = spreadsheet.getElement();
        assertEquals("{\"runs\":[70,1,100,1,0,1,70,49]}",
                element.getProperty("colW"));
        assertEquals("{\"runs\":[20.0,1,15.0,2,0.0,1,15.0,196]}",
                element.getProperty("rowH"));
        assertEquals("[3]", element.getProperty("hiddenColumnIndexes"));
        assertEquals("[4]", element.getProperty("hiddenRowIndexes"));
        assertEquals(70, element.getProperty("defColW", 0));
        assertEquals(15.0, element.getProperty("defRowH", 0.0));
    }

    @Test
    void theOnlyRow_isShownAtItsOwnHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).setHeightInPoints(20);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(20f, spreadsheet.getRowH()[0]);
    }

    @Test
    void theOnlyRow_hidden_staysHidden() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).setZeroHeight(true);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(0f, spreadsheet.getRowH()[0]);
        assertTrue(spreadsheet.getHiddenRowIndexes().contains(1));
    }

    @Test
    void aSheetWithoutRows_showsTheFirstAtTheDefaultHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().setDefaultRowHeightInPoints(18);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(18f, spreadsheet.getRowH()[0]);
    }

    @Test
    void aSheetWithoutRows_ofAnOldWorkbook_showsTheFirstAtTheDefaultHeight() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet().setDefaultRowHeightInPoints(18);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(18f, spreadsheet.getRowH()[0]);
    }
}
