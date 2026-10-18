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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * What typing a value does to the style of its cell: a percentage gives it a
 * percentage format, and a leading apostrophe marks it as text, on that cell
 * only. A style is shared by every cell that wears it, so the cell is given a
 * style of its own rather than changing the one it shares.
 */
class TypedValueStyleTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private Spreadsheet spreadsheet;
    /** A bold style that A1 and A2 share. */
    private CellStyle shared;

    @BeforeEach
    void show() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        shared = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        shared.setFont(bold);
        for (int row = 0; row < 2; row++) {
            Cell cell = sheet.createRow(row).createCell(0);
            cell.setCellValue("v" + row);
            cell.setCellStyle(shared);
        }
        sheet.createRow(2).createCell(2).setCellValue("default");
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    /** Types the value into the cell, which the client numbers from 1. */
    private void type(String address, String value) {
        spreadsheet.setSelection(address);
        CellReference cell = new CellReference(address);
        spreadsheet.getCellValueManager().onCellValueChange(cell.getCol() + 1,
                cell.getRow() + 1, value);
    }

    private CellStyle styleOf(String address) {
        return spreadsheet.getCell(address).getCellStyle();
    }

    @Test
    void aPercentageTyped_formatsItsCellOnly() {
        type("A1", "5%");

        assertTrue(styleOf("A1").getDataFormatString().contains("%"));
        assertEquals("General", styleOf("A2").getDataFormatString());
        assertEquals(shared.getIndex(), styleOf("A2").getIndex());
    }

    @Test
    void aPercentageTyped_keepsTheRestOfTheStyleOfItsCell() {
        type("A1", "5%");

        assertNotEquals(shared.getIndex(), styleOf("A1").getIndex());
        assertTrue(workbook.getFontAt(styleOf("A1").getFontIndex()).getBold());
        assertEquals(0.05, spreadsheet.getCell("A1").getNumericCellValue(),
                1e-9);
    }

    @Test
    void aPercentageTypedIntoADefaultCell_leavesTheDefaultStyleAlone() {
        type("C3", "5%");

        assertTrue(styleOf("C3").getDataFormatString().contains("%"));
        assertEquals("General",
                workbook.getCellStyleAt(0).getDataFormatString());
    }

    @Test
    void aLeadingApostropheTypedIntoANewCell_leavesTheDefaultStyleAlone() {
        type("D4", "'abc");

        assertTrue(styleOf("D4").getQuotePrefixed());
        assertEquals("abc", spreadsheet.getCell("D4").getStringCellValue());
        assertFalse(workbook.getCellStyleAt(0).getQuotePrefixed());
        assertFalse(styleOf("C3").getQuotePrefixed());
    }

    @Test
    void aLeadingApostropheTyped_marksItsCellOnly() {
        type("A1", "'abc");

        assertTrue(styleOf("A1").getQuotePrefixed());
        assertFalse(styleOf("A2").getQuotePrefixed());
        assertTrue(workbook.getFontAt(styleOf("A1").getFontIndex()).getBold());
    }

    @Test
    void aValueTypedIntoAMarkedCell_leavesTheOthersOfItsStyleMarked() {
        // Every edit of an existing cell takes the mark off it: it used to
        // take it off the style every cell of it shared.
        shared.setQuotePrefixed(true);

        type("A1", "plain");

        assertFalse(styleOf("A1").getQuotePrefixed());
        assertTrue(styleOf("A2").getQuotePrefixed());
    }
}
