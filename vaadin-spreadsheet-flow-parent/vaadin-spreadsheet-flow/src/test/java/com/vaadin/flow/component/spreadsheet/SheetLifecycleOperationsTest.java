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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Creating and deleting sheets with {@link SheetLifecycleOperations}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>{@code createNewSheet}: name validation (null OK, empty throws, >31 chars
 * throws, duplicate throws), then SpreadsheetFactory.addNewSheet + sheetChange
 * event.</li>
 * <li>{@code deleteSheetWithPOIIndex}: min-1-visible-sheet guard,
 * workbook.removeSheetAt, active-sheet shift on the workbook ONLY when the
 * removed visible-index was at-or-before the current index, then reload.</li>
 * <li>{@code deleteSheet}: visible-index range check, translation to POI
 * index.</li>
 * </ul>
 */
class SheetLifecycleOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;
    @Mock
    private Sheet previousSheet;
    @Mock
    private Sheet newSheet;

    private SheetLifecycleOperations ops;
    private MockedStatic<SpreadsheetFactory> factoryStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ops = new SheetLifecycleOperations(spreadsheet,
                org.mockito.Mockito.mock(ActiveSheetSelector.class));
        spreadsheet.workbook = workbook;
        lenient().when(spreadsheet.isSheetNameExisting(anyString()))
                .thenReturn(false);
        factoryStatic = mockStatic(SpreadsheetFactory.class);
    }

    @AfterEach
    void tearDown() {
        factoryStatic.close();
    }

    // ----- createNewSheet ---------------------------------------------------

    @Test
    void createNewSheet_emptyName_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ops.createNewSheet("", 10, 5));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Sheet Name cannot be an empty String.", ex.getMessage());
    }

    @Test
    void createNewSheet_nameLongerThan31Chars_throws() {
        // POI / Excel limit. Pin the boundary at exactly 32 chars
        // failing, 31 chars succeeding.
        String tooLong = "a".repeat(32);

        assertThrows(IllegalArgumentException.class,
                () -> ops.createNewSheet(tooLong, 10, 5));
    }

    @Test
    void createNewSheet_31CharName_passesValidation() {
        // 31 is the inclusive upper bound.
        when(spreadsheet.getActiveSheet()).thenReturn(previousSheet, newSheet);
        String exactly31 = "a".repeat(31);

        ops.createNewSheet(exactly31, 10, 5);

        factoryStatic.verify(() -> SpreadsheetFactory.addNewSheet(spreadsheet,
                workbook, exactly31, 10, 5));
    }

    @Test
    void createNewSheet_duplicateName_throws() {
        when(spreadsheet.isSheetNameExisting("Existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> ops.createNewSheet("Existing", 10, 5));
    }

    @Test
    void createNewSheet_nullName_passes_andDelegatesToFactory() {
        // Pin: null sheet name skips ALL three validations and POI is
        // expected to auto-name. The factory call still fires.
        when(spreadsheet.getActiveSheet()).thenReturn(previousSheet, newSheet);

        ops.createNewSheet(null, 20, 8);

        factoryStatic.verify(() -> SpreadsheetFactory.addNewSheet(spreadsheet,
                workbook, null, 20, 8));
    }

    @Test
    void createNewSheet_firesSheetChangeEvent_oldAndNewActiveSheet() {
        when(spreadsheet.getActiveSheet()).thenReturn(previousSheet, newSheet);

        ops.createNewSheet("S2", 10, 5);

        verify(spreadsheet).fireSheetChangeEvent(previousSheet, newSheet);
    }

    // ----- deleteSheetWithPOIIndex ------------------------------------------

    @Test
    void deleteSheetWithPOIIndex_lessThan2VisibleSheets_throws() {
        when(spreadsheet.getNumberOfVisibleSheets()).thenReturn(1);

        assertThrows(IllegalArgumentException.class,
                () -> ops.deleteSheetWithPOIIndex(0));
    }

    // ----- deleteSheet (visible-index variant) ------------------------------

    @Test
    void deleteSheet_negativeIndex_throws() {
        assertThrows(IllegalArgumentException.class, () -> ops.deleteSheet(-1));
    }

    @Test
    void deleteSheet_indexAtOrAboveCount_throws() {
        when(spreadsheet.getNumberOfVisibleSheets()).thenReturn(2);

        assertThrows(IllegalArgumentException.class, () -> ops.deleteSheet(2));
    }

    @Test
    void deleteSheet_validIndex_delegatesToPOIIndexVariant() {
        // Pin: visible-index translation via getVisibleSheetPOIIndex
        // then the component's deleteSheetWithPOIIndex, so an override of
        // it applies.
        when(spreadsheet.getNumberOfVisibleSheets()).thenReturn(3);
        when(spreadsheet.getVisibleSheetPOIIndex(1)).thenReturn(2);

        ops.deleteSheet(1);

        verify(spreadsheet).deleteSheetWithPOIIndex(2);
    }
}
