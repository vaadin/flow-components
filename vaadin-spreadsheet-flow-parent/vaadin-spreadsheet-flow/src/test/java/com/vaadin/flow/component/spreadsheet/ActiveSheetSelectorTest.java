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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link ActiveSheetSelector}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Index translation between the UI-visible index (sheetIndex - 1) and the
 * POI index (via getVisibleSheetPOIIndex).</li>
 * <li>Validation cascade: invalid index throws, hidden / very-hidden sheets
 * cannot be activated.</li>
 * <li>The reload sequence on successful activation: setActiveSheet +
 * setSelectedTab + clear formula caches + reload sheet data + reload styles +
 * load popup buttons.</li>
 * </ul>
 *
 * <p>
 * Uses {@link MockedStatic} for
 * {@code SpreadsheetFactory.reloadSpreadsheetData}.
 */
class ActiveSheetSelectorTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;
    @Mock
    private FormulaEvaluator formulaEvaluator;
    @Mock
    private ConditionalFormattingEvaluator condFormattingEvaluator;
    @Mock
    private Sheet sheet;

    private ActiveSheetSelector selector;
    private MockedStatic<SpreadsheetFactory> factoryStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        selector = new ActiveSheetSelector(spreadsheet);
        spreadsheet.workbook = workbook;
        lenient().when(spreadsheet.getSheetNames())
                .thenReturn(new String[] { "S1", "S2", "S3" });
        lenient().when(spreadsheet.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        lenient().when(spreadsheet.getConditionalFormattingEvaluator())
                .thenReturn(condFormattingEvaluator);
        lenient().when(workbook.getNumberOfSheets()).thenReturn(3);
        lenient()
                .when(workbook
                        .getSheetAt(org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(sheet);
        factoryStatic = mockStatic(SpreadsheetFactory.class);
    }

    @AfterEach
    void tearDown() {
        factoryStatic.close();
    }

    @Test
    void getActiveSheetIndex_returnsSheetIndexMinus1() {
        // Pin: Spreadsheet's sheetIndex is 1-based; the selector
        // translates by subtracting 1.
        when(spreadsheet.getSheetIndex()).thenReturn(3);

        assertEquals(2, selector.getActiveSheetIndex());
    }

    @Test
    void getActiveSheetPOIIndex_translatesViaVisibleSheetPOIIndex() {
        // Pin: getActiveSheetPOIIndex composes getActiveSheetIndex with
        // the visible-to-POI mapping (which can differ when there are
        // hidden sheets).
        when(spreadsheet.getSheetIndex()).thenReturn(2);
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(3);

        assertEquals(3, selector.getActiveSheetPOIIndex());
    }

    @Test
    void setActiveSheetIndex_negativeIndex_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetIndex(-1));
    }

    @Test
    void setActiveSheetIndex_indexAtOrAboveLength_throws() {
        // sheetNames.length == 3 → index 3 invalid.
        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetIndex(3));
    }

    @Test
    void setActiveSheetIndex_valid_delegatesToPOIIndexSetter() {
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(2);

        selector.setActiveSheetIndex(1);

        // Through the component, so an override of it applies.
        verify(spreadsheet).setActiveSheetWithPOIIndex(2);
    }

    @Test
    void setActiveSheetWithPOIIndex_negativePoiIndex_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetWithPOIIndex(-1));
    }

    @Test
    void setActiveSheetWithPOIIndex_poiIndexAtOrAboveNumberOfSheets_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetWithPOIIndex(3));
    }

    @Test
    void setActiveSheetWithPOIIndex_hiddenSheet_throws() {
        when(workbook.isSheetHidden(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> selector.setActiveSheetWithPOIIndex(1));
        assertEquals(
                "Cannot set a hidden or very hidden sheet as the active sheet. Given index: 1",
                ex.getMessage());
    }

    @Test
    void setActiveSheetWithPOIIndex_veryHiddenSheet_throws() {
        when(workbook.isSheetVeryHidden(1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetWithPOIIndex(1));
    }

    @Test
    void setActiveSheetWithPOIIndex_valid_setsActiveSheetAndSelectedTab() {
        selector.setActiveSheetWithPOIIndex(2);

        verify(workbook).setActiveSheet(2);
        verify(workbook).setSelectedTab(2);
    }

    @Test
    void setActiveSheetWithPOIIndex_valid_clearsBothFormulaCaches() {
        // Pin: formulas may need recalculation against the new active
        // sheet. Both evaluator caches are cleared.
        selector.setActiveSheetWithPOIIndex(0);

        verify(formulaEvaluator).clearAllCachedResultValues();
        verify(condFormattingEvaluator).clearAllCachedValues();
    }

    @Test
    void setActiveSheetWithPOIIndex_valid_callsReloadSequence() {
        selector.setActiveSheetWithPOIIndex(0);

        verify(spreadsheet).reloadActiveSheetData();
        factoryStatic.verify(() -> SpreadsheetFactory
                .reloadSpreadsheetData(spreadsheet, sheet));
        verify(spreadsheet).reloadActiveSheetStyles();
        verify(spreadsheet).loadPopupButtons();
    }

    @Test
    void setActiveSheetWithPOIIndex_invalidIndex_doesNotTouchCaches() {
        // Pin: when the validation rejects, no side effects fire.
        assertThrows(IllegalArgumentException.class,
                () -> selector.setActiveSheetWithPOIIndex(-1));

        verify(formulaEvaluator, org.mockito.Mockito.never())
                .clearAllCachedResultValues();
        verify(workbook, org.mockito.Mockito.never())
                .setActiveSheet(org.mockito.ArgumentMatchers.anyInt());
        factoryStatic.verifyNoInteractions();
    }
}
