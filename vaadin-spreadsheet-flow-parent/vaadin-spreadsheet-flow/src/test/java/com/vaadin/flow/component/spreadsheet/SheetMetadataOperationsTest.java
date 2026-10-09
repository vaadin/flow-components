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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Renaming sheets with {@link SheetMetadataOperations}.
 *
 * <p>
 * Pins the validation cascade for sheet-rename and the conditional
 * sheet-names-array update (skipped for hidden / very-hidden sheets so the
 * visible-names mirror only tracks visible sheets).
 *
 * <p>
 * {@code spreadsheet.workbook} is package-private — assigned directly from the
 * test setUp.
 */
class SheetMetadataOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;

    private SheetMetadataOperations ops;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ops = new SheetMetadataOperations(spreadsheet);
        spreadsheet.workbook = workbook;
        lenient().when(spreadsheet.getSheetNames())
                .thenReturn(new String[] { "Sheet1", "Sheet2", "Sheet3" });
        lenient().when(workbook.getNumberOfSheets()).thenReturn(3);
        lenient().when(spreadsheet.isSheetNameExisting(anyString()))
                .thenReturn(false);
    }

    @Test
    void setSheetName_negativeIndex_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetName(-1, "name"));
    }

    @Test
    void setSheetName_indexAtOrAboveLength_throwsIllegalArgument() {
        // sheetNames.length == 3 → index 3 is invalid (boundary).
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetName(3, "name"));
    }

    @Test
    void setSheetName_validIndex_delegatesToPOIIndexSetter() {
        // Pin: setSheetName translates the visible-sheet index via
        // getVisibleSheetPOIIndex, then calls the component's
        // setSheetNameWithPOIIndex, so an override of it applies.
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(2);

        ops.setSheetName(1, "Renamed");

        verify(spreadsheet).setSheetNameWithPOIIndex(2, "Renamed");
    }

    @Test
    void setSheetNameWithPOIIndex_negativePoiIndex_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetNameWithPOIIndex(-1, "name"));
    }

    @Test
    void setSheetNameWithPOIIndex_poiIndexAtOrAboveLength_throws() {
        // numberOfSheets == 3 → index 3 is invalid.
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetNameWithPOIIndex(3, "name"));
    }

    @Test
    void setSheetNameWithPOIIndex_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetNameWithPOIIndex(0, null));
    }

    @Test
    void setSheetNameWithPOIIndex_emptyName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ops.setSheetNameWithPOIIndex(0, ""));
    }

    @Test
    void setSheetNameWithPOIIndex_duplicateName_throws() {
        when(spreadsheet.isSheetNameExisting("DupeName")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ops.setSheetNameWithPOIIndex(0, "DupeName"));
        assertEquals("Sheet name must be unique within the workbook.",
                ex.getMessage());
    }

    @Test
    void setSheetNameWithPOIIndex_visibleSheet_updatesSheetNamesArray() {
        // Pin: a visible (non-hidden, non-very-hidden) sheet has its rename
        // propagated to the sheet-names mirror via setSheetNames.
        when(workbook.isSheetHidden(1)).thenReturn(false);
        when(workbook.isSheetVeryHidden(1)).thenReturn(false);
        when(spreadsheet.getSpreadsheetSheetIndex(1)).thenReturn(1);

        ops.setSheetNameWithPOIIndex(1, "NewName");

        verify(workbook).setSheetName(1, "NewName");
        ArgumentCaptor<String[]> captor = ArgumentCaptor
                .forClass(String[].class);
        verify(spreadsheet).setSheetNames(captor.capture());
        assertArrayEquals(new String[] { "Sheet1", "NewName", "Sheet3" },
                captor.getValue());
    }

    @Test
    void setSheetNameWithPOIIndex_hiddenSheet_skipsSheetNamesArrayUpdate() {
        // Pin: hidden sheets are NOT in the visible sheet-names mirror.
        // The POI rename still fires, but the spreadsheet's setSheetNames
        // is NOT called.
        when(workbook.isSheetHidden(1)).thenReturn(true);

        ops.setSheetNameWithPOIIndex(1, "NewName");

        verify(workbook).setSheetName(1, "NewName");
        verify(spreadsheet, never())
                .setSheetNames(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setSheetNameWithPOIIndex_veryHiddenSheet_skipsSheetNamesArrayUpdate() {
        when(workbook.isSheetVeryHidden(1)).thenReturn(true);

        ops.setSheetNameWithPOIIndex(1, "NewName");

        verify(workbook).setSheetName(1, "NewName");
        verify(spreadsheet, never())
                .setSheetNames(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setSheetNameWithPOIIndex_buildsFreshArray_doesNotMutatePrior() {
        // Pin defensive-copy: setSheetNames receives a fresh array (via
        // Arrays.copyOf), so the original sheet-names array is untouched.
        String[] original = { "Sheet1", "Sheet2", "Sheet3" };
        when(spreadsheet.getSheetNames()).thenReturn(original);
        when(workbook.isSheetHidden(1)).thenReturn(false);
        when(workbook.isSheetVeryHidden(1)).thenReturn(false);
        when(spreadsheet.getSpreadsheetSheetIndex(1)).thenReturn(1);

        ops.setSheetNameWithPOIIndex(1, "NewName");

        // Original array unchanged.
        assertArrayEquals(new String[] { "Sheet1", "Sheet2", "Sheet3" },
                original);
    }
}
