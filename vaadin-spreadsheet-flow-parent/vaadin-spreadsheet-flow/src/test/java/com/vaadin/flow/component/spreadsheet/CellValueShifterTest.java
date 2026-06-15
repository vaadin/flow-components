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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link CellValueShifter}.
 *
 * <p>
 * Pins the type-dispatch behavior of the Excel-autofill cell writer. Each
 * cell-type branch is tested with and without a sequence increment where
 * applicable. {@code FORMULA} is exercised separately because it requires
 * mocking the formula-evaluator chain.
 *
 * <p>
 * Uses an in-memory {@link XSSFWorkbook} fixture for the cells themselves (real
 * POI types are part of the contract — POI's {@code setBlank} and
 * {@code setCellValue} side effects are exactly what we want to pin). Mocks the
 * parent {@link Spreadsheet} because the shifter notifies it via
 * {@code getSpreadsheetStyleFactory().cellStyleUpdated} and
 * {@code getCellValueManager().cellUpdated} as side effects.
 */
class CellValueShifterTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SpreadsheetStyleFactory styleFactory;
    @Mock
    private CellValueManager valueManager;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private CellValueShifter shifter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        when(spreadsheet.getSpreadsheetStyleFactory()).thenReturn(styleFactory);
        when(spreadsheet.getCellValueManager()).thenReturn(valueManager);
        shifter = new CellValueShifter(spreadsheet);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void shift_numericCell_withoutIncrement_copiesValue() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(42.0);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        assertEquals(CellType.NUMERIC, dst.getCellType());
        assertEquals(42.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_numericCell_withIncrement_alongRow_appliesScaledIncrement() {
        // Pin: row-axis shift uses column delta, increment * (newCol - oldCol)
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(10.0);
        Cell dst = row.createCell(3); // 3 columns to the right
        Double increment = 2.0;

        shifter.shift(src, dst, false, increment);

        // 10 + 2 * (3 - 0) = 16
        assertEquals(16.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_numericCell_withIncrement_alongColumn_appliesScaledIncrement() {
        // Pin: column-axis shift uses row delta (when source and destination
        // share the column).
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellValue(10.0);
        Cell dst = sheet.createRow(4).createCell(0); // 4 rows below
        Double increment = 1.5;

        shifter.shift(src, dst, false, increment);

        // 10 + 1.5 * (4 - 0) = 16.0
        assertEquals(16.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_stringCell_withIncrement_incrementsTrailingDigits() {
        // Pin: STRING cells whose value ends in digits get the digits
        // incremented along the shift axis. "A1" + col-shift 2 → "A3".
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("Item1");
        Cell dst = row.createCell(2); // 2 columns right

        shifter.shift(src, dst, false, 1.0);

        // "Item" + abs(1 + 1*2) = "Item3"
        assertEquals("Item3", dst.getStringCellValue());
    }

    @Test
    void shift_stringCell_noTrailingDigits_withIncrement_copiesUnchanged() {
        // Pin: a STRING cell without trailing digits ignores the increment
        // and just gets copied. "foo" stays "foo" no matter the shift.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("foo");
        Cell dst = row.createCell(2);

        shifter.shift(src, dst, false, 1.0);

        assertEquals("foo", dst.getStringCellValue());
    }

    @Test
    void shift_stringCell_withoutIncrement_copiesValue() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("hello");
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        assertEquals("hello", dst.getStringCellValue());
    }

    @Test
    void shift_booleanCell_copiesValue_ignoringIncrement() {
        // Pin: BOOLEAN cells copy as-is; the increment parameter is ignored.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(true);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, 5.0);

        assertEquals(CellType.BOOLEAN, dst.getCellType());
        assertEquals(true, dst.getBooleanCellValue());
    }

    @Test
    void shift_blankCell_writesBlank() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        // src is default BLANK
        Cell dst = row.createCell(1);
        dst.setCellValue("non-blank initially");

        shifter.shift(src, dst, false, null);

        assertEquals(CellType.BLANK, dst.getCellType());
    }

    @Test
    void shift_notifiesStyleFactoryAndCellValueManager() {
        // Pin the two parent-side notifications: style-factory's
        // cellStyleUpdated(newCell, true) and value-manager's
        // cellUpdated(newCell) — both fire on every shift, regardless of
        // type.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(1.0);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        verify(styleFactory).cellStyleUpdated(dst, true);
        verify(valueManager).cellUpdated(dst);
    }

    @Test
    void shift_withRemoveShifted_clearsSourceAndNotifiesDeletion() {
        // Pin: removeShifted=true clears the source cell's value and
        // notifies the value-manager via cellDeleted. The destination still
        // gets its normal cellUpdated notification.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("payload");
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, true, null);

        // Source is cleared.
        assertEquals("", src.getStringCellValue());
        verify(valueManager).cellDeleted(src);
        verify(valueManager).cellUpdated(dst);
    }
}
