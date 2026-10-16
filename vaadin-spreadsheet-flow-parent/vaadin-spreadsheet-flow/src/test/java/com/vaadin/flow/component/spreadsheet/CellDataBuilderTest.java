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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Locale;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * Building the data sent to the client for a cell with {@link CellDataBuilder}.
 *
 * <p>
 * The {@code createCellDataForCell} method has eight major branches plus the
 * {@code handleIsDisplayZeroPreference} private helper. This file pins the
 * BRANCH DISPATCH shape rather than every formatting detail — the formatting is
 * delegated to {@code numericFormatter} (covered by
 * {@link CellNumericFormatterTest}), and full end-to-end format fidelity is
 * covered by the integration suite. The tests below focus on what the builder
 * OWNS: branch selection, cellData population, and the post-processing
 * (zero-display preference, " cf<N>" append, "#VALUE!" fallback).
 *
 * <p>
 * Uses a real {@link XSSFWorkbook} for the cells (real POI cells avoid massive
 * Cell-API mocking) and mocks the {@link CellValueManager} +
 * {@link Spreadsheet} chain.
 */
class CellDataBuilderTest {

    @Mock
    private CellValueManager manager;
    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private ConditionalFormatter conditionalFormatter;
    @Mock
    private FormulaEvaluator formulaEvaluator;
    @Mock
    private ConditionalFormattingEvaluator condFmtEvaluator;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private CellNumericFormatter numericFormatter;
    private FormulaFormatter formulaFormatter;
    private CellFitCalculator fitCalculator;
    private ClientCellCache clientCache;
    private CellDataBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();

        // Use real instances for the formatter chain — mocking
        // DataFormatter and friends is impractical here.
        numericFormatter = new CellNumericFormatter();
        numericFormatter.updateLocale(Locale.US);
        formulaFormatter = new FormulaFormatter();
        fitCalculator = new CellFitCalculator();
        clientCache = new ClientCellCache();

        // Wire the package-private fields the builder reads on the
        // manager via reflection (Mockito mocks have the fields but
        // they're null by default).
        wireField("spreadsheet", spreadsheet);
        wireField("numericFormatter", numericFormatter);
        wireField("formulaFormatter", formulaFormatter);
        wireField("clientCache", clientCache);

        // Mocked methods the builder calls back through.
        org.mockito.Mockito.lenient().when(manager.fitCalculator())
                .thenReturn(fitCalculator);
        org.mockito.Mockito.lenient().when(manager.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        org.mockito.Mockito.lenient()
                .when(manager.getConditionalFormattingEvaluator())
                .thenReturn(condFmtEvaluator);
        org.mockito.Mockito.lenient()
                .when(manager.doesValueFit(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        org.mockito.Mockito.lenient()
                .when(manager.getCellWidth(org.mockito.ArgumentMatchers.any()))
                .thenReturn(100);
        org.mockito.Mockito.lenient().when(manager
                .getOriginalCellValue(org.mockito.ArgumentMatchers.any()))
                .thenReturn("");

        // Spreadsheet defaults: not locked, not hidden, not invalid,
        // CF returns null (no CF rules).
        org.mockito.Mockito.lenient()
                .when(spreadsheet
                        .isCellLocked(org.mockito.ArgumentMatchers.any()))
                .thenReturn(false);
        org.mockito.Mockito.lenient()
                .when(spreadsheet
                        .isCellHidden(org.mockito.ArgumentMatchers.any()))
                .thenReturn(false);
        org.mockito.Mockito.lenient()
                .when(spreadsheet.isMarkedAsInvalidFormula(
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(false);
        org.mockito.Mockito.lenient().when(spreadsheet.getLocale())
                .thenReturn(Locale.US);
        org.mockito.Mockito.lenient()
                .when(spreadsheet.getConditionalFormatter())
                .thenReturn(conditionalFormatter);
        org.mockito.Mockito.lenient().when(conditionalFormatter
                .getCellFormattingIndex(org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        builder = new CellDataBuilder(manager);
    }

    private void wireField(String name, Object value) throws Exception {
        java.lang.reflect.Field f;
        try {
            f = CellValueManager.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            // Field on parent class - rare here, but handle.
            throw e;
        }
        f.setAccessible(true);
        f.set(manager, value);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    private Cell newStringCell(int row, int col, String value) {
        org.apache.poi.ss.usermodel.Row poiRow = sheet.getRow(row);
        if (poiRow == null) {
            poiRow = sheet.createRow(row);
        }
        Cell cell = poiRow.createCell(col);
        cell.setCellValue(value);
        return cell;
    }

    private Cell newNumericCell(int row, int col, double value) {
        org.apache.poi.ss.usermodel.Row poiRow = sheet.getRow(row);
        if (poiRow == null) {
            poiRow = sheet.createRow(row);
        }
        Cell cell = poiRow.createCell(col);
        cell.setCellValue(value);
        return cell;
    }

    // ----- happy path: basic CellData population --------------------------

    @Test
    void createCellDataForCell_stringCell_populatesRowColCellStyleAndValue() {
        // Pin: the basic invariant — row/col are 1-based, cellStyle is
        // "cs<index>", value is the formatted string. A regression that
        // forgot the +1 row/col offset would slip past integration tests
        // that don't compare exact coordinates.
        Cell cell = newStringCell(2, 3, "hello");

        CellData result = builder.createCellDataForCell(cell);

        assertEquals(3, result.row, "Row MUST be 1-based (POI 2 → 3)");
        assertEquals(4, result.col, "Col MUST be 1-based (POI 3 → 4)");
        assertTrue(result.cellStyle.startsWith("cs"),
                "cellStyle MUST start with 'cs' + style index");
        assertEquals("hello", result.value);
    }

    @Test
    void createCellDataForCell_lockedCell_propagatesLockedFlag() {
        // Pin: cellData.locked mirrors spreadsheet.isCellLocked. A
        // regression that ignored the lock state would let read-only
        // cells be edited on the client.
        Cell cell = newStringCell(0, 0, "x");
        when(spreadsheet.isCellLocked(cell.getAddress())).thenReturn(true);

        CellData result = builder.createCellDataForCell(cell);

        assertTrue(result.locked);
    }

    // ----- conditional-formatting branch ----------------------------------

    @Test
    void createCellDataForCell_withConditionalFormatting_appendsCfIndexAndMarksForUpdate() {
        // Pin: when ConditionalFormatter returns non-empty indexes, each
        // index is appended to cellStyle as " cf<N>" AND the clientCache
        // is marked for update with the cell key. A regression skipping
        // either part would break CF rendering on the client.
        Cell cell = newStringCell(0, 0, "v");
        HashSet<Integer> cfIndexes = new HashSet<>();
        cfIndexes.add(7);
        when(conditionalFormatter.getCellFormattingIndex(cell))
                .thenReturn(cfIndexes);

        CellData result = builder.createCellDataForCell(cell);

        assertTrue(result.cellStyle.contains(" cf7"),
                "cellStyle MUST contain ' cf7'; got: " + result.cellStyle);
        // Default style has index 0 → key = "col1 row1"; the value
        // gate is the non-empty cellStyle OR style index 0; with a
        // non-empty value AND style index 0, both paths enter the
        // value block, and the CF branch fires markForUpdate.
        // Pin the markForUpdate side effect via the clientCache's
        // observable state (clientCache.isMarkedForUpdate returns true).
        assertTrue(clientCache.isMarkedForUpdate(SpreadsheetUtil.toKey(cell)),
                "clientCache MUST hold the cell key for CF-marked cells");
    }

    @Test
    void createCellDataForCell_noConditionalFormatting_doesNotAppendCfOrMarkForUpdate() {
        // Pin the negative branch: with no CF indexes, the CF append
        // is skipped and the cell key is not added to the flush queue.
        Cell cell = newStringCell(0, 0, "v");

        CellData result = builder.createCellDataForCell(cell);

        assertFalse(result.cellStyle.contains(" cf"),
                "cellStyle MUST NOT contain ' cf' without CF indexes; got: "
                        + result.cellStyle);
        assertFalse(clientCache.isMarkedForUpdate(SpreadsheetUtil.toKey(cell)));
    }

    // ----- display-zero preference (private helper exposed indirectly) ----

    @Test
    void createCellDataForCell_zeroNumericWithDisplayZerosFalse_clearsValue() {
        // Pin handleIsDisplayZeroPreference: NUMERIC + value=="0" +
        // sheet.isDisplayZeros() false → cellData.value cleared to "".
        // The default Excel "show zero values" sheet preference is true,
        // so we need to override it on the real sheet.
        Cell cell = newNumericCell(0, 0, 0);
        sheet.setDisplayZeros(false);

        CellData result = builder.createCellDataForCell(cell);

        assertEquals("", result.value,
                "Zero value MUST be cleared when displayZeros=false");
    }

    @Test
    void createCellDataForCell_zeroNumericWithDisplayZerosTrue_keepsZeroValue() {
        // Pin the inverse branch: same cell with default
        // (displayZeros=true) keeps the "0" value.
        Cell cell = newNumericCell(0, 0, 0);
        assertTrue(sheet.isDisplayZeros());

        CellData result = builder.createCellDataForCell(cell);

        assertEquals("0", result.value,
                "Zero value MUST be kept when displayZeros=true");
    }

    @Test
    void createCellDataForCell_nonZeroNumericWithDisplayZerosFalse_keepsValue() {
        // Pin: only the EXACT string "0" triggers the clear. "0.5" or
        // "-0" are kept. A regression that triggered on any "starts
        // with 0" would clear too aggressively.
        Cell cell = newNumericCell(0, 0, 5.0);
        sheet.setDisplayZeros(false);

        CellData result = builder.createCellDataForCell(cell);

        assertEquals("5", result.value,
                "Non-zero numeric MUST be kept regardless of displayZeros");
    }

    // ----- runtime-exception fallback -------------------------------------

    @Test
    void createCellDataForCell_runtimeExceptionInBody_fallsBackToValueErrorString() {
        // Pin the catch-all: if anything in the try block throws (e.g.
        // a malformed conditional-formatting lookup), cellData.value is
        // set to "#VALUE!" and the method returns a populated CellData
        // rather than propagating the exception.
        Cell cell = newStringCell(0, 0, "x");
        when(conditionalFormatter.getCellFormattingIndex(cell))
                .thenThrow(new RuntimeException("forced for test"));

        CellData result = builder.createCellDataForCell(cell);

        assertNotNull(result, "MUST return a CellData even on inner throw");
        assertEquals("#VALUE!", result.value,
                "Value MUST fall back to '#VALUE!' on exception");
    }

    // ----- alignment branch ------------------------------------------------

    @Test
    void createCellDataForCell_rightAlignedStyle_appendsSpaceR() {
        // Pin: an explicit RIGHT alignment on the cell style appends
        // " r" to cellStyle. The client uses this marker for layout.
        org.apache.poi.ss.usermodel.CellStyle rightStyle = workbook
                .createCellStyle();
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);
        Cell cell = newStringCell(0, 0, "x");
        cell.setCellStyle(rightStyle);

        CellData result = builder.createCellDataForCell(cell);

        assertTrue(result.cellStyle.endsWith(" r"),
                "Right-aligned cellStyle MUST end with ' r'; got: "
                        + result.cellStyle);
    }

    @Test
    void createCellDataForCell_leftAlignedStyle_doesNotAppendSpaceR() {
        // Pin the negative branch.
        org.apache.poi.ss.usermodel.CellStyle leftStyle = workbook
                .createCellStyle();
        leftStyle.setAlignment(HorizontalAlignment.LEFT);
        Cell cell = newStringCell(0, 0, "x");
        cell.setCellStyle(leftStyle);

        CellData result = builder.createCellDataForCell(cell);

        assertFalse(result.cellStyle.endsWith(" r"),
                "Left-aligned cellStyle MUST NOT end with ' r'; got: "
                        + result.cellStyle);
    }

    // ----- invalid-formula marker ------------------------------------------

    @Test
    void createCellDataForCell_markedInvalidFormula_emitsValueErrorString() {
        // Pin: when spreadsheet.isMarkedAsInvalidFormula returns true,
        // cellData.value is replaced with "#VALUE!" regardless of the
        // actual formatted value. The string must start with '=' or
        // '+' for the formula-value extraction to fire — the test uses
        // "=BADFORMULA" to satisfy that precondition.
        Cell cell = newStringCell(0, 0, "=BADFORMULA");
        when(spreadsheet.isMarkedAsInvalidFormula(1, 1)).thenReturn(true);

        CellData result = builder.createCellDataForCell(cell);

        assertEquals("#VALUE!", result.value);
        assertEquals("BADFORMULA", result.formulaValue,
                "formulaValue MUST strip the leading '=' or '+'");
    }

    // ----- empty value + empty style guard --------------------------------

    @Test
    void createCellDataForCell_emptyValueAndZeroStyleIndex_skipsValueBlock() {
        // Pin: the value-block guard is `formattedCellValue != null &&
        // !isEmpty() || cellStyle.getIndex() != 0`. An empty string
        // value AND default style index 0 means the value block does
        // NOT execute — cellData.value stays null. A regression that
        // executed the block unconditionally would emit "" rather than
        // null, which the client distinguishes.
        Cell cell = newStringCell(0, 0, "");

        CellData result = builder.createCellDataForCell(cell);

        // Default style is index 0; value is empty; guard fails.
        assertNull(result.value,
                "Empty value + style index 0 MUST leave value null");
    }
}
