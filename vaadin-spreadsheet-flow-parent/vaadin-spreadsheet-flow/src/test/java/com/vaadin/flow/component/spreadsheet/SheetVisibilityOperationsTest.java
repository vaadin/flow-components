/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Hiding, showing and protecting sheets with {@link SheetVisibilityOperations}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>{@code setSheetHidden}: the "at least one visible sheet" guard, the no-op
 * short-circuit when visibility doesn't actually change, the active-sheet shift
 * when the active sheet itself is hidden.</li>
 * <li>{@code setSheetProtected}: range validation, protectSheet call,
 * active-sheet-only side effect (loadCustomComponents +
 * reSelectSelectedCell).</li>
 * </ul>
 */
class SheetVisibilityOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;
    @Mock
    private Sheet sheet;
    @Mock
    private CellSelectionManager selectionManager;

    private SheetVisibilityOperations ops;
    private MockedStatic<SpreadsheetUtil> utilStatic;
    private MockedStatic<SpreadsheetFactory> factoryStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ops = new SheetVisibilityOperations(spreadsheet);
        spreadsheet.workbook = workbook;
        lenient().when(workbook.getSheetAt(anyInt())).thenReturn(sheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        utilStatic = mockStatic(SpreadsheetUtil.class);
        factoryStatic = mockStatic(SpreadsheetFactory.class);
    }

    @AfterEach
    void tearDown() {
        utilStatic.close();
        factoryStatic.close();
    }

    // ----- setSheetHidden — guards ------------------------------------------

    @Test
    void setSheetHidden_lastVisibleSheetCannotBeHidden_throws() {
        // Pin: POI allows hiding all sheets; this component does not. If
        // only one visible sheet remains AND it's not already hidden,
        // attempting to hide it throws.
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(1);
        when(workbook.isSheetHidden(0)).thenReturn(false);
        when(workbook.isSheetVeryHidden(0)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetHidden(0, SheetVisibility.HIDDEN));
    }

    @Test
    void setSheetHidden_alreadyHiddenSheetBeingChanged_okEvenIfSingleVisible() {
        // Pin: the single-visible guard skips when the target sheet is
        // already hidden/very-hidden (i.e. you're un-hiding it or going
        // hidden→very-hidden, both fine).
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(1);
        when(workbook.isSheetHidden(0)).thenReturn(true);
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        when(workbook.getNumberOfSheets()).thenReturn(2);
        when(spreadsheet.getSheetIndex()).thenReturn(1);
        when(spreadsheet.getVisibleSheetPOIIndex(0)).thenReturn(1);

        ops.setSheetHidden(0, SheetVisibility.VERY_HIDDEN);

        verify(workbook).setSheetVisibility(0, SheetVisibility.VERY_HIDDEN);
    }

    // ----- setSheetHidden — no-op short-circuit -----------------------------

    @Test
    void setSheetHidden_visibleToVisible_skipsReload() {
        // Pin: visibility==VISIBLE && already-visible → setSheetVisibility
        // still fires (POI tracks it) but the spreadsheet-side reload
        // cascade is skipped because nothing actually changed.
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(2);
        when(workbook.isSheetHidden(1)).thenReturn(false);
        when(workbook.isSheetVeryHidden(1)).thenReturn(false);
        when(workbook.getActiveSheetIndex()).thenReturn(0);

        ops.setSheetHidden(1, SheetVisibility.VISIBLE);

        verify(workbook).setSheetVisibility(1, SheetVisibility.VISIBLE);
        verify(spreadsheet, never()).reloadSheetNames();
        verify(spreadsheet, never()).reloadActiveSheetData();
    }

    @Test
    void setSheetHidden_hiddenToHidden_skipsReload() {
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(2);
        when(workbook.isSheetHidden(1)).thenReturn(true);
        when(workbook.getActiveSheetIndex()).thenReturn(0);

        ops.setSheetHidden(1, SheetVisibility.HIDDEN);

        verify(workbook).setSheetVisibility(1, SheetVisibility.HIDDEN);
        verify(spreadsheet, never()).reloadSheetNames();
    }

    // ----- setSheetHidden — non-active-sheet path ---------------------------

    @Test
    void setSheetHidden_nonActiveSheet_reloadsSheetNames_andUpdatesIndex() {
        // Pin: when the target sheet is NOT the active one, the side
        // effects are reloadSheetNames + setSheetIndex (to keep the
        // current active sheet pointed at correctly after the visibility
        // shift).
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(3);
        when(workbook.isSheetHidden(1)).thenReturn(false);
        when(workbook.isSheetVeryHidden(1)).thenReturn(false);
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        when(spreadsheet.getSpreadsheetSheetIndex(0)).thenReturn(0);

        ops.setSheetHidden(1, SheetVisibility.HIDDEN);

        verify(spreadsheet).reloadSheetNames();
        verify(spreadsheet).setSheetIndex(1); // (0 + 1)
    }

    // ----- setSheetHidden — active-sheet path -------------------------------

    @Test
    void setSheetHidden_activeSheet_hidesAndShiftsToPreviousSheet() {
        // Pin the active-sheet hide path: when the active sheet is being
        // hidden, the active sheet shifts. Active sheet is the LAST one
        // → oldVisibleSheetIndex decrements by 1.
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(3);
        when(workbook.isSheetHidden(2)).thenReturn(false);
        when(workbook.isSheetVeryHidden(2)).thenReturn(false);
        when(workbook.getActiveSheetIndex()).thenReturn(2);
        when(workbook.getNumberOfSheets()).thenReturn(3);
        when(spreadsheet.getSheetIndex()).thenReturn(3); // oldVisible = 2
        // Sheet is last: visibility != VISIBLE && active == count-1 →
        // decrement to oldVisibleSheetIndex = 1.
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(1);

        ops.setSheetHidden(2, SheetVisibility.HIDDEN);

        verify(workbook).setActiveSheet(1);
        verify(spreadsheet).reloadActiveSheetData();
        factoryStatic.verify(() -> SpreadsheetFactory
                .reloadSpreadsheetData(spreadsheet, sheet));
        verify(spreadsheet).reloadSheetStyles(false, false);
    }

    @Test
    void setSheetHidden_activeSheetNotLast_stayAtSameVisibleIndex() {
        // Pin: when hiding the active sheet but it's NOT the last one,
        // oldVisibleSheetIndex stays put — the next sheet "slides into"
        // the current visible position.
        utilStatic
                .when(() -> SpreadsheetUtil.getNumberOfVisibleSheets(workbook))
                .thenReturn(3);
        when(workbook.isSheetHidden(1)).thenReturn(false);
        when(workbook.isSheetVeryHidden(1)).thenReturn(false);
        when(workbook.getActiveSheetIndex()).thenReturn(1);
        when(workbook.getNumberOfSheets()).thenReturn(3); // not last
        when(spreadsheet.getSheetIndex()).thenReturn(2); // visible = 1
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(2);

        ops.setSheetHidden(1, SheetVisibility.HIDDEN);

        verify(workbook).setActiveSheet(2);
    }

    // ----- setSheetProtected -----------------------------------------------

    @Test
    void setSheetProtected_negativeIndex_throws() {
        when(workbook.getNumberOfSheets()).thenReturn(3);

        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetProtected(-1, "pw"));
    }

    @Test
    void setSheetProtected_indexAtOrAboveCount_throws() {
        when(workbook.getNumberOfSheets()).thenReturn(3);

        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetProtected(3, "pw"));
    }

    @Test
    void setSheetProtected_validNonActiveIndex_protectsSheet_butSkipsActiveSideEffects() {
        // Pin: protectSheet fires + setSheetProtected on the spreadsheet
        // (mirror state). The custom-component reload + cell-reselect
        // are skipped when the protected sheet is NOT the active one.
        when(workbook.getNumberOfSheets()).thenReturn(3);
        when(sheet.getProtect()).thenReturn(true);
        when(workbook.getActiveSheetIndex()).thenReturn(0);

        ops.setSheetProtected(1, "pw");

        verify(sheet).protectSheet("pw");
        verify(spreadsheet).setSheetProtected(true);
        verify(spreadsheet, never()).loadCustomComponents();
        verify(selectionManager, never()).reSelectSelectedCell();
    }

    @Test
    void setSheetProtected_activeSheet_triggersComponentReload_andReselect() {
        // Pin: when the protected sheet IS the active one, the post-mirror
        // side effects fire (loadCustomComponents + reSelectSelectedCell).
        // Required because the protection change may flip a cell from
        // editable to locked or vice versa.
        when(workbook.getNumberOfSheets()).thenReturn(3);
        when(sheet.getProtect()).thenReturn(false);
        when(workbook.getActiveSheetIndex()).thenReturn(1);

        ops.setSheetProtected(1, null);

        verify(sheet).protectSheet(null);
        verify(spreadsheet).loadCustomComponents();
        verify(selectionManager).reSelectSelectedCell();
    }

    @Test
    void setActiveSheetProtected_delegatesToActiveSheetIndexVariant() {
        // Pin: convenience method routes through the component's
        // setSheetProtected with the workbook's current active index, so an
        // override of it applies.
        when(workbook.getActiveSheetIndex()).thenReturn(2);

        ops.setActiveSheetProtected("secret");

        verify(spreadsheet).setSheetProtected(2, "secret");
    }
}
