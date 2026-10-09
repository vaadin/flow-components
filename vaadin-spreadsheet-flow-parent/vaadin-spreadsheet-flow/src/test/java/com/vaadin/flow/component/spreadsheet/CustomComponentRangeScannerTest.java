/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

/**
 * Collecting the custom components and editors of a cell range with
 * {@link CustomComponentRangeScanner}.
 *
 * <p>
 * Pins the per-cell decision tree:
 *
 * <ul>
 * <li>Merged-region skip: cells inside a merged region (not anchor) are
 * skipped; the loop counter jumps ahead to {@code col2}. The anchor cell IS
 * scanned.</li>
 * <li>Component vs editor: if {@code getCustomComponentForCell} returns
 * non-null → register, add to {@code componentIDtoCellKeysMap}. Else if the
 * cell is NOT locked, try {@code getCustomEditorForCell} → register, add to
 * {@code cellKeyToEditor} (a different map).</li>
 * <li>Locked cells skip the editor path entirely.</li>
 * <li>Existing-components dedup: a component already in
 * {@code existingComponents} is not re-registered.</li>
 * <li>Exceptions from the factory are swallowed and logged — the scan continues
 * to the next cell.</li>
 * </ul>
 *
 * <p>
 * Uses {@link MockedStatic} to stub the static
 * {@code Spreadsheet.getComponentNodeId} call, which keys the component map. It
 * returns the same id for every component, as it is before the spreadsheet is
 * attached.
 */
class CustomComponentRangeScannerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Sheet sheet;
    @Mock
    private MergedRegionContainer regionContainer;
    @Mock
    private SpreadsheetComponentFactory factory;
    @Mock
    private Component customComponent;
    @Mock
    private Component customEditor;

    private CustomComponentRangeScanner scanner;
    private Set<Component> existing;
    private HashSet<Component> newComponents;
    private Set<Integer> rowsWithComponents;
    private HashMap<String, Component> cellKeyToEditor;
    private HashMap<String, String> componentIDtoCellKeysMap;
    private MockedStatic<Spreadsheet> spreadsheetStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scanner = new CustomComponentRangeScanner(spreadsheet);
        existing = new HashSet<>();
        newComponents = new HashSet<>();
        rowsWithComponents = new HashSet<>();
        cellKeyToEditor = new HashMap<>();
        componentIDtoCellKeysMap = new HashMap<>();
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getMergedRegionContainer())
                .thenReturn(regionContainer);
        lenient().when(regionContainer.getMergedRegion(anyInt(), anyInt()))
                .thenReturn(null);
        lenient().when(spreadsheet.isCellLocked(any(CellAddress.class)))
                .thenReturn(false);
        spreadsheetStatic = mockStatic(Spreadsheet.class);
        spreadsheetStatic.when(() -> Spreadsheet.getComponentNodeId(any()))
                .thenReturn("node-id");
    }

    @AfterEach
    void tearDown() {
        spreadsheetStatic.close();
    }

    private void runScan(int r1, int c1, int r2, int c2) {
        scanner.scan(factory, existing, newComponents, rowsWithComponents,
                cellKeyToEditor, componentIDtoCellKeysMap, r1, c1, r2, c2);
    }

    @Test
    void scan_emptyRange_callsFactoryButHarvestsNothing() {
        // No row, no cell. factory still gets called once (the scan
        // iterates the range regardless of row null-ness — it passes a
        // null Cell down).
        runScan(1, 1, 1, 1);

        verify(factory).getCustomComponentForCell(any(), anyInt(), anyInt(),
                any(), any());
        assertTrue(newComponents.isEmpty());
        assertTrue(rowsWithComponents.isEmpty());
    }

    @Test
    void scan_cellReturnsComponent_registers_andAddsToComponentMap() {
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customComponent);

        runScan(1, 1, 1, 1);

        verify(spreadsheet).registerCustomComponent(customComponent);
        assertTrue(newComponents.contains(customComponent));
        assertTrue(rowsWithComponents.contains(0));
        // Component path uses componentIDtoCellKeysMap (NOT
        // cellKeyToEditor).
        assertTrue(componentIDtoCellKeysMap.containsValue("col1 row1"));
        assertTrue(cellKeyToEditor.isEmpty());
    }

    @Test
    void scan_componentAlreadyExisting_skipsRegisterButStillAddsToNewSet() {
        // Pin the dedup: a component already in the existing set is NOT
        // re-registered. It still goes into newCustomComponents because
        // the new set is the "what is now visible" snapshot.
        existing.add(customComponent);
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customComponent);

        runScan(1, 1, 1, 1);

        verify(spreadsheet, never()).registerCustomComponent(customComponent);
        assertTrue(newComponents.contains(customComponent));
    }

    @Test
    void scan_noComponent_butEditorReturned_registersAsEditor() {
        // Pin: component-first dispatch. If the factory returns null for
        // getCustomComponentForCell, the scanner falls through to the
        // editor path (only if the cell isn't locked).
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(null);
        when(factory.getCustomEditorForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customEditor);

        runScan(1, 1, 1, 1);

        verify(spreadsheet).registerCustomComponent(customEditor);
        assertTrue(newComponents.contains(customEditor));
        // Editor path uses cellKeyToEditor (NOT componentID...Map).
        assertSame(customEditor, cellKeyToEditor.get("col1 row1"));
        assertTrue(componentIDtoCellKeysMap.isEmpty());
    }

    @Test
    void scan_cellLocked_skipsEditorPathEntirely() {
        // Pin: locked cells get NO editor lookup even when no component
        // is returned. The factory's getCustomEditorForCell never fires
        // for locked cells.
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(null);
        when(spreadsheet.isCellLocked(any(CellAddress.class))).thenReturn(true);

        runScan(1, 1, 1, 1);

        verify(factory, never()).getCustomEditorForCell(any(), anyInt(),
                anyInt(), any(), any());
        assertTrue(cellKeyToEditor.isEmpty());
        assertTrue(newComponents.isEmpty());
    }

    @Test
    void scan_componentReturned_skipsEditorLookup() {
        // Pin: if the factory returns a component, the editor path is
        // skipped — even if the editor would also return non-null.
        // Component-wins precedence.
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customComponent);

        runScan(1, 1, 1, 1);

        verify(factory, never()).getCustomEditorForCell(any(), anyInt(),
                anyInt(), any(), any());
    }

    @Test
    void scan_cellInsideMergedRegion_notAnchor_skipsCell_andJumpsToCol2() {
        // Pin: a cell at (c, r) inside a merged region whose anchor is
        // NOT (c, r) gets neither component nor editor lookup. AND the
        // for-loop's c counter jumps ahead to region.col2 - 1 (+1 for the
        // c++ at loop iteration) — so the rest of the merge is skipped
        // in one step.
        MergedRegion region = new MergedRegion(1, 1, 3, 1);
        when(regionContainer.getMergedRegion(2, 1)).thenReturn(region);

        runScan(1, 2, 1, 3);

        // For c=1 (POI col 1, 1-based col 2): inside merge, skipped.
        // After the skip, c jumps to 2 (region.col2-1=2), c++ → 3 next.
        // The loop bound is c < c2 = 3, so c=3 exits. No factory calls
        // for cells in [col 2, col 3].
        verify(factory, never()).getCustomComponentForCell(any(), anyInt(),
                anyInt(), any(), any());
    }

    @Test
    void scan_mergedRegionAnchor_isScanned() {
        MergedRegion region = new MergedRegion(1, 1, 3, 1);
        when(regionContainer.getMergedRegion(1, 1)).thenReturn(region);
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customComponent);

        runScan(1, 1, 1, 1);

        verify(spreadsheet).registerCustomComponent(customComponent);
        assertTrue(newComponents.contains(customComponent));
    }

    @Test
    void scan_factoryThrows_isSwallowed_andLoopContinues() {
        // Pin: an exception from getCustomComponentForCell is logged but
        // does not abort the scan. The next cell still gets its lookup.
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenThrow(new RuntimeException("factory boom"))
                .thenReturn(customComponent);

        runScan(1, 1, 1, 2); // 2 cells

        // Second cell should have succeeded.
        assertTrue(newComponents.contains(customComponent));
    }

    @Test
    void scan_nullRow_passesNullCellToFactory() {
        // Pin: when the sheet has no row at r, the scanner doesn't abort —
        // it passes null down as the Cell argument. The factory decides
        // whether to honor that.
        when(sheet.getRow(0)).thenReturn(null);

        runScan(1, 1, 1, 1);

        // Verify the factory got called with a null cell.
        verify(factory).getCustomComponentForCell(
                org.mockito.ArgumentMatchers.isNull(), anyInt(), anyInt(),
                any(), any());
    }

    @Test
    void scan_existingEditor_skipsRegister_butStillAddsToCellKeysMap() {
        // Pin the editor-path dedup: an editor in either `existing` OR
        // already in `newCustomComponents` (within the same scan) is NOT
        // re-registered. But the cellKeyToEditor map is updated either
        // way.
        existing.add(customEditor);
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(null);
        when(factory.getCustomEditorForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customEditor);

        runScan(1, 1, 1, 1);

        verify(spreadsheet, never()).registerCustomComponent(customEditor);
        assertSame(customEditor, cellKeyToEditor.get("col1 row1"));
    }

    @Test
    void scan_cellsThatHaveEditors_eachKeepsItsOwn() {
        // Two cells with an editor each, and both editors with the same
        // node id. Each cell is handed back its own editor, and the factory
        // is not asked for new ones.
        Component otherEditor = mock(Component.class);
        cellKeyToEditor.put("col1 row1", customEditor);
        cellKeyToEditor.put("col2 row1", otherEditor);
        existing.add(customEditor);
        existing.add(otherEditor);

        runScan(1, 1, 1, 2);

        verify(factory, never()).getCustomEditorForCell(any(), anyInt(),
                anyInt(), any(), any());
        assertSame(customEditor, cellKeyToEditor.get("col1 row1"));
        assertSame(otherEditor, cellKeyToEditor.get("col2 row1"));
        assertTrue(newComponents.contains(customEditor));
        assertTrue(newComponents.contains(otherEditor));
    }

    @Test
    void scan_noComponentNoEditor_addsNothing() {
        // Pin: empty cell with no component and no editor → no mutations
        // to any of the four output collections, no registration.
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(null);
        when(factory.getCustomEditorForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(null);

        runScan(1, 1, 1, 1);

        verify(spreadsheet, never()).registerCustomComponent(any());
        assertTrue(newComponents.isEmpty());
        assertTrue(rowsWithComponents.isEmpty());
        assertTrue(cellKeyToEditor.isEmpty());
        assertTrue(componentIDtoCellKeysMap.isEmpty());
    }

    @Test
    void scan_existingComponentsArgumentMustNotBeNull() {
        // Smoke check: pass a non-empty existing set to make sure the
        // contains() check is invoked rather than NPE'ing. This was an
        // earlier concern when wiring the scanner.
        existing.add(mock(Component.class)); // unrelated component
        when(factory.getCustomComponentForCell(any(), anyInt(), anyInt(), any(),
                any())).thenReturn(customComponent);

        runScan(1, 1, 1, 1);

        verify(spreadsheet).registerCustomComponent(customComponent);
        assertFalse(existing.contains(customComponent));
    }
}
