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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CSS of the workbook's cell styles reaches the client as the
 * {@code cellStyleToCSSStyle} property: every style's CSS, by style index.
 */
class StyleMapPublishTest {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    /**
     * A workbook with the given number of styles past the default, each filling
     * a cell of row 1 with a colour of its own.
     */
    static XSSFWorkbook workbookWithStyles(int count) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        for (int i = 0; i < count; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(i);
            cell.setCellStyle(filled(workbook, i));
        }
        return workbook;
    }

    static XSSFCellStyle filled(XSSFWorkbook workbook, int colour) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new byte[] {
                (byte) (colour * 7), (byte) (colour * 13), (byte) 200 }, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /** The style map as the client receives it. */
    static Map<Integer, String> published(Spreadsheet spreadsheet) {
        return JSON.readValue(
                spreadsheet.getElement().getProperty("cellStyleToCSSStyle"),
                new TypeReference<Map<Integer, String>>() {
                });
    }

    @Test
    void loadingAWorkbook_publishesTheCssOfEveryStyle() {
        XSSFWorkbook workbook = workbookWithStyles(5);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        Map<Integer, String> published = published(spreadsheet);
        assertEquals(workbook.getNumCellStyles(), published.size());
        assertEquals(spreadsheet.getCellStyleToCSSStyle(), published);
        for (int i = 1; i < workbook.getNumCellStyles(); i++) {
            assertTrue(published.get(i).contains("background-color"),
                    "style " + i + ": " + published.get(i));
        }
    }

    @Test
    void aWorkbookOfMoreStylesThanAShortCounts_publishesTheCssOfEach() {
        // Excel allows 64,000 styles; POI numbers those past 32,767 below 0.
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0);
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            workbook.createCellStyle().setIndention((short) 1);
        }
        CellStyle last = workbook
                .getCellStyleAt(workbook.getNumCellStyles() - 1);
        workbook.getSheetAt(0).getRow(0).getCell(0).setCellStyle(last);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        Map<Integer, String> published = published(spreadsheet);
        assertEquals(workbook.getNumCellStyles(), published.size());
        assertTrue(
                published.get((int) last.getIndex()).contains("padding-left"),
                published.get((int) last.getIndex()));
    }

    @Test
    void reloadingTheStyles_publishesTheSameCss() {
        Spreadsheet spreadsheet = new Spreadsheet(workbookWithStyles(5));
        Map<Integer, String> before = published(spreadsheet);

        spreadsheet.getSpreadsheetStyleFactory().reloadWorkbookStyles();

        assertEquals(before, published(spreadsheet));
    }

    @Test
    void aCellGivenANewStyle_publishesThatStylesCss() {
        XSSFWorkbook workbook = workbookWithStyles(2);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        XSSFCellStyle added = filled(workbook, 99);
        Cell cell = workbook.getSheetAt(0).getRow(0).createCell(5);
        cell.setCellStyle(added);

        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(cell, true);

        Map<Integer, String> published = published(spreadsheet);
        assertTrue(published.containsKey((int) added.getIndex()));
        assertEquals(
                spreadsheet.getCellStyleToCSSStyle()
                        .get((int) added.getIndex()),
                published.get((int) added.getIndex()));
    }

    @Test
    void aStyleChangedInPlace_publishesItsNewCss() {
        XSSFWorkbook workbook = workbookWithStyles(2);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        Cell cell = workbook.getSheetAt(0).getRow(0).getCell(1);
        XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
        String before = published(spreadsheet).get((int) style.getIndex());

        style.setFillForegroundColor(
                new XSSFColor(new byte[] { 1, 2, 3 }, null));
        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(cell, true);

        String after = published(spreadsheet).get((int) style.getIndex());
        assertNotEquals(before, after);
        assertTrue(after.contains("rgba(1, 2, 3, 1.0)"), after);
    }

    // Each send serializes the whole map: a workbook's styles, added one by
    // one and each sent alone, would cost the server half a minute for 20,000.

    @Test
    void reloadingTheStyles_serializesTheMapOnce() {
        Spreadsheet spreadsheet = new Spreadsheet(workbookWithStyles(50));
        HashMap<Integer, String> map = spreadsheet.getCellStyleToCSSStyle();

        try (MockedStatic<Serializer> serializer = mockStatic(Serializer.class,
                CALLS_REAL_METHODS)) {
            spreadsheet.getSpreadsheetStyleFactory().reloadWorkbookStyles();

            serializer.verify(() -> Serializer.toJson(same(map)), times(1));
        }
    }

    @Test
    void cellsGivenAStyleTheClientHasAlready_serializeNothing() {
        XSSFWorkbook workbook = workbookWithStyles(3);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        HashMap<Integer, String> map = spreadsheet.getCellStyleToCSSStyle();
        Sheet sheet = workbook.getSheetAt(0);
        CellStyle shared = sheet.getRow(0).getCell(1).getCellStyle();
        Row row = sheet.createRow(1);

        try (MockedStatic<Serializer> serializer = mockStatic(Serializer.class,
                CALLS_REAL_METHODS)) {
            for (int c = 0; c < 20; c++) {
                Cell cell = row.createCell(c);
                cell.setCellStyle(shared);
                spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(cell,
                        true);
            }

            serializer.verify(() -> Serializer.toJson(same(map)), never());
        }
    }

    @Test
    void aStyleChangedInPlace_serializesTheMapOnce() {
        XSSFWorkbook workbook = workbookWithStyles(3);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        HashMap<Integer, String> map = spreadsheet.getCellStyleToCSSStyle();
        Row row = workbook.getSheetAt(0).getRow(0);
        XSSFCellStyle style = (XSSFCellStyle) row.getCell(1).getCellStyle();
        style.setFillForegroundColor(
                new XSSFColor(new byte[] { 1, 2, 3 }, null));

        try (MockedStatic<Serializer> serializer = mockStatic(Serializer.class,
                CALLS_REAL_METHODS)) {
            // The changed style, then a second cell sharing it.
            Cell second = row.createCell(9);
            second.setCellStyle(style);
            spreadsheet.getSpreadsheetStyleFactory()
                    .cellStyleUpdated(row.getCell(1), true);
            spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(second,
                    true);

            serializer.verify(() -> Serializer.toJson(same(map)), times(1));
        }
    }
}
