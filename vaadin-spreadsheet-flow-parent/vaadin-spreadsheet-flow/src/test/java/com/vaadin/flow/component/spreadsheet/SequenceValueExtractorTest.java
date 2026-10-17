/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reading a run of numbers or texts out of a row or column with
 * {@link SequenceValueExtractor}.
 *
 * <p>
 * Pins the "read a contiguous run of typed cells, stop at the first mismatch"
 * behavior used to feed {@link SequenceIncrement}. The pinned invariants are:
 *
 * <ul>
 * <li>Indexing is 1-based at the public boundary; internal POI calls subtract
 * one.</li>
 * <li>The returned array length is fixed at {@code end - start + 1}.</li>
 * <li>Iteration aborts (and leaves remaining entries as {@code null}) on the
 * first cell of the wrong type, a {@code null} cell, or a {@code null}
 * row.</li>
 * </ul>
 *
 * <p>
 * Uses a real in-memory {@link XSSFWorkbook} (the same pattern as
 * {@code SpreadsheetFactoryTest}) rather than mocks — the extractor is a thin
 * wrapper over POI and the round-trip through POI is what we want to pin.
 */
class SequenceValueExtractorTest {

    private XSSFWorkbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    // ----- row extraction ----------------------------------------------------

    @Test
    void extractRowNumeric_allNumeric_returnsAllValues() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1d);
        row.createCell(1).setCellValue(2d);
        row.createCell(2).setCellValue(3d);

        Double[] actual = SequenceValueExtractor.extractRowNumeric(row, 0, 2);

        assertArrayEquals(new Double[] { 1d, 2d, 3d }, actual);
    }

    @Test
    void extractRowNumeric_stopsAtFirstStringCell() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1d);
        row.createCell(1).setCellValue("oops");
        row.createCell(2).setCellValue(3d);

        Double[] actual = SequenceValueExtractor.extractRowNumeric(row, 0, 2);

        assertArrayEquals(new Double[] { 1d, null, null }, actual);
    }

    @Test
    void extractRowNumeric_stopsAtMissingCell() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1d);
        // index 1 deliberately left empty
        row.createCell(2).setCellValue(3d);

        Double[] actual = SequenceValueExtractor.extractRowNumeric(row, 0, 2);

        assertArrayEquals(new Double[] { 1d, null, null }, actual);
    }

    @Test
    void extractRowString_allString_returnsAllValues() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("a");
        row.createCell(1).setCellValue("b");
        row.createCell(2).setCellValue("c");

        String[] actual = SequenceValueExtractor.extractRowString(row, 0, 2);

        assertArrayEquals(new String[] { "a", "b", "c" }, actual);
    }

    @Test
    void extractRowString_stopsAtFirstNumericCell() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("a");
        row.createCell(1).setCellValue(2d);
        row.createCell(2).setCellValue("c");

        String[] actual = SequenceValueExtractor.extractRowString(row, 0, 2);

        assertArrayEquals(new String[] { "a", null, null }, actual);
    }

    // ----- column extraction -------------------------------------------------

    @Test
    void extractColumnNumeric_allNumeric_returnsAllValues() {
        sheet.createRow(0).createCell(0).setCellValue(10d);
        sheet.createRow(1).createCell(0).setCellValue(20d);
        sheet.createRow(2).createCell(0).setCellValue(30d);

        Double[] actual = SequenceValueExtractor.extractColumnNumeric(sheet, 0,
                0, 2);

        assertArrayEquals(new Double[] { 10d, 20d, 30d }, actual);
    }

    @Test
    void extractColumnNumeric_stopsAtMissingRow() {
        sheet.createRow(0).createCell(0).setCellValue(10d);
        // row index 1 deliberately not created
        sheet.createRow(2).createCell(0).setCellValue(30d);

        Double[] actual = SequenceValueExtractor.extractColumnNumeric(sheet, 0,
                0, 2);

        assertArrayEquals(new Double[] { 10d, null, null }, actual);
    }

    @Test
    void extractColumnNumeric_stopsAtFirstStringCell() {
        sheet.createRow(0).createCell(0).setCellValue(10d);
        sheet.createRow(1).createCell(0).setCellValue("twenty");
        sheet.createRow(2).createCell(0).setCellValue(30d);

        Double[] actual = SequenceValueExtractor.extractColumnNumeric(sheet, 0,
                0, 2);

        assertArrayEquals(new Double[] { 10d, null, null }, actual);
    }

    @Test
    void extractColumnString_allString_returnsAllValues() {
        sheet.createRow(0).createCell(0).setCellValue("x");
        sheet.createRow(1).createCell(0).setCellValue("y");
        sheet.createRow(2).createCell(0).setCellValue("z");

        String[] actual = SequenceValueExtractor.extractColumnString(sheet, 0,
                0, 2);

        assertArrayEquals(new String[] { "x", "y", "z" }, actual);
    }

    @Test
    void extractColumnString_stopsAtFirstNumericCell() {
        sheet.createRow(0).createCell(0).setCellValue("x");
        sheet.createRow(1).createCell(0).setCellValue(2d);
        sheet.createRow(2).createCell(0).setCellValue("z");

        String[] actual = SequenceValueExtractor.extractColumnString(sheet, 0,
                0, 2);

        assertArrayEquals(new String[] { "x", null, null }, actual);
    }

    @Test
    void extractColumnString_stopsAtMissingRow() {
        sheet.createRow(0).createCell(0).setCellValue("x");
        // row index 1 deliberately not created
        sheet.createRow(2).createCell(0).setCellValue("z");

        String[] actual = SequenceValueExtractor.extractColumnString(sheet, 0,
                0, 2);

        assertArrayEquals(new String[] { "x", null, null }, actual);
    }

    // ----- a range not starting at the first column
    // ---------------------------

    @Test
    void extractRowNumeric_rangeAfterTheFirstColumn_readsItsOwnCells() {
        // 0-based, as POI: extractRowNumeric(row, 1, 3) reads columns 1..3.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(99d); // outside range
        row.createCell(1).setCellValue(2d);
        row.createCell(2).setCellValue(3d);
        row.createCell(3).setCellValue(4d);
        row.createCell(4).setCellValue(99d); // outside range

        Double[] actual = SequenceValueExtractor.extractRowNumeric(row, 1, 3);

        assertArrayEquals(new Double[] { 2d, 3d, 4d }, actual);
    }

    @Test
    void extractRowNumeric_singleCell_returnsLengthOneArray() {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(42d);

        Double[] actual = SequenceValueExtractor.extractRowNumeric(row, 0, 0);

        assertArrayEquals(new Double[] { 42d }, actual);
    }

    @Test
    void extractColumnString_emptySheet_returnsAllNulls() {
        // No rows created at all — first iteration hits null row, breaks
        // immediately. Use assertArrayEquals so a regression that returns
        // a shorter array (e.g. only the null prefix) still fails — the
        // three-assertNull form would silently pass if length dropped to 1.
        String[] actual = SequenceValueExtractor.extractColumnString(sheet, 0,
                0, 2);

        assertArrayEquals(new String[] { null, null, null }, actual);
    }
}
