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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Detecting an arithmetic sequence in a row or column with
 * {@link RangeSequenceDetector}.
 *
 * <p>
 * Pins the row / column dispatch logic that combines
 * {@link SequenceValueExtractor} + {@link SequenceIncrement}. Three branches
 * per direction: leading-cell-is-STRING, leading-cell-is-NUMERIC, anything else
 * returns {@code null}.
 *
 * <p>
 * Uses an in-memory {@link XSSFWorkbook} fixture — the detector reads cells via
 * POI, so a real workbook is the natural test substrate.
 */
class RangeSequenceDetectorTest {

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

    // ----- getRowIncrement --------------------------------------------------

    @Test
    void getRowIncrement_numericSequence_returnsCommonDifference() {
        sheet.createRow(0).createCell(0).setCellValue(1d);
        sheet.getRow(0).createCell(1).setCellValue(2d);
        sheet.getRow(0).createCell(2).setCellValue(3d);

        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);

        assertEquals(Double.valueOf(1), inc);
    }

    @Test
    void getRowIncrement_stringSequence_returnsCommonDifference() {
        sheet.createRow(0).createCell(0).setCellValue("Item 1");
        sheet.getRow(0).createCell(1).setCellValue("Item 2");
        sheet.getRow(0).createCell(2).setCellValue("Item 3");

        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);

        assertEquals(Double.valueOf(1), inc);
    }

    @Test
    void getRowIncrement_missingRow_returnsNull() {
        // Pin the early-out: no row at the target index means no sequence.
        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);
        assertNull(inc);
    }

    @Test
    void getRowIncrement_missingLeadingCell_returnsNull() {
        sheet.createRow(0); // row exists, but no cells
        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);
        assertNull(inc);
    }

    @Test
    void getRowIncrement_leadingCellIsFormula_returnsNull() {
        // Pin the type-gate: only STRING and NUMERIC leading cells qualify;
        // FORMULA / BOOLEAN / etc. fall through to null even if the formula's
        // cached value would be numeric.
        sheet.createRow(0).createCell(0).setCellFormula("1+1");
        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);
        assertNull(inc);
    }

    @Test
    void getRowIncrement_nonConstantDifferences_returnsNull() {
        sheet.createRow(0).createCell(0).setCellValue(1d);
        sheet.getRow(0).createCell(1).setCellValue(2d);
        sheet.getRow(0).createCell(2).setCellValue(5d); // breaks the
                                                        // progression

        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 3);

        assertNull(inc);
    }

    // ----- getColumnIncrement -----------------------------------------------

    @Test
    void getColumnIncrement_numericSequence_returnsCommonDifference() {
        sheet.createRow(0).createCell(0).setCellValue(10d);
        sheet.createRow(1).createCell(0).setCellValue(20d);
        sheet.createRow(2).createCell(0).setCellValue(30d);

        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 3);

        assertEquals(Double.valueOf(10), inc);
    }

    @Test
    void getColumnIncrement_stringSequence_returnsCommonDifference() {
        sheet.createRow(0).createCell(0).setCellValue("A1");
        sheet.createRow(1).createCell(0).setCellValue("A2");
        sheet.createRow(2).createCell(0).setCellValue("A3");

        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 3);

        assertEquals(Double.valueOf(1), inc);
    }

    @Test
    void getColumnIncrement_missingStartRow_returnsNull() {
        // Pin: if the row at r1 doesn't exist, no sequence regardless of
        // later rows. Mirrors getRowIncrement's missing-row behavior.
        sheet.createRow(2).createCell(0).setCellValue(30d);
        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 3);
        assertNull(inc);
    }

    @Test
    void getColumnIncrement_missingLeadingCell_returnsNull() {
        sheet.createRow(0); // r1 exists but the target column is empty
        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 3);
        assertNull(inc);
    }

    @Test
    void getColumnIncrement_decreasingNumericSequence_returnsNegative() {
        // Pin negative-increment semantics: the detector reports the signed
        // difference, not the absolute value.
        sheet.createRow(0).createCell(0).setCellValue(30d);
        sheet.createRow(1).createCell(0).setCellValue(20d);
        sheet.createRow(2).createCell(0).setCellValue(10d);

        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 3);

        assertEquals(Double.valueOf(-10), inc);
    }

    @Test
    void getColumnIncrement_singleStringCell_returnsOne() {
        // Pin Excel-autofill default: a single string-with-digits cell is
        // treated as the start of an arithmetic-progression-by-1.
        sheet.createRow(0).createCell(0).setCellValue("A1");

        Double inc = RangeSequenceDetector.getColumnIncrement(sheet, 1, 1, 1);

        assertEquals(Double.valueOf(1), inc);
    }

    // ----- 1-based indexing pin ---------------------------------------------

    @Test
    void getRowIncrement_oneBasedColumnRange_alignsWithPOIZeroBased() {
        // 1-based: getRowIncrement(sheet, rIndex=1, c1=2, c2=3) reads POI
        // columns 1 + 2 (zero-based), not 2 + 3. The first cell at POI
        // column 0 is outside the range.
        sheet.createRow(0).createCell(0).setCellValue(99d); // outside
        sheet.getRow(0).createCell(1).setCellValue(5d);
        sheet.getRow(0).createCell(2).setCellValue(10d);

        Double inc = RangeSequenceDetector.getRowIncrement(sheet, 1, 2, 3);

        assertEquals(Double.valueOf(5), inc);
    }
}
