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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.vaadin.flow.component.Component;

/**
 * Loading the custom components of the visible panes with
 * {@link CustomComponentLoader}.
 *
 * <p>
 * Three modes pinned:
 *
 * <ol>
 * <li>{@code load()} with {@code factory == null}: clears both client-side
 * maps, unregisters every component, and applies empty heights via the row
 * sizer.</li>
 * <li>{@code load()} with factory: 4-quadrant scan dispatch (same as the
 * comment / hyperlink loaders), reconciliation that keeps editors still mapped
 * + unregisters the rest, fresh map writes, row-sizer call only when at least
 * one component row exists.</li>
 * <li>{@code unloadAll()}: similar to the factory-null path but does NOT touch
 * {@code componentIDtoCellKeysMap} nor the row heights. Used when a new factory
 * is set while the spreadsheet hasn't loaded yet.</li>
 * </ol>
 */
class CustomComponentLoaderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CustomComponentRangeScanner scanner;
    @Mock
    private CustomComponentRowSizer rowSizer;
    @Mock
    private SpreadsheetComponentFactory factory;
    @Mock
    private Component comp1;
    @Mock
    private Component comp2;

    private CustomComponentLoader loader;
    private MockedStatic<Spreadsheet> spreadsheetStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loader = new CustomComponentLoader(spreadsheet, scanner, rowSizer);
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getFirstColumn()).thenReturn(1);
        lenient().when(spreadsheet.getLastColumn()).thenReturn(10);
        lenient().when(spreadsheet.getCellKeyToEditorMap())
                .thenReturn(new HashMap<>());
        spreadsheetStatic = mockStatic(Spreadsheet.class);
        // Default: identity-ish node id based on the mock's hashCode.
        spreadsheetStatic.when(() -> Spreadsheet.getComponentNodeId(any()))
                .thenAnswer((Answer<String>) inv -> "node-"
                        + System.identityHashCode(inv.getArgument(0)));
    }

    @AfterEach
    void tearDown() {
        spreadsheetStatic.close();
    }

    // ----- factory / accessor mechanics -------------------------------------

    @Test
    void factoryAccessors_storeAndReturnTheFactory() {
        assertSame(null, loader.getFactory());
        loader.setFactory(factory);
        assertSame(factory, loader.getFactory());
    }

    @Test
    void getCustomComponents_freshLoader_isEmptyButNotNull() {
        Set<Component> set = loader.getCustomComponents();
        assertSame(set, loader.getCustomComponents(),
                "Same reference across calls until load() swaps it");
        assertTrue(set.isEmpty());
    }

    // ----- load(): factory == null ------------------------------------------

    @Test
    void load_noFactory_clearsBothMaps_andUnregistersAllComponents() {
        // Seed customComponents via a fake first-pass scan that adds comp1.
        // Easier: directly use the factory-set + a load to add it, then
        // null the factory and load again.
        seedOneComponent();
        loader.setFactory(null);

        loader.load();

        verify(spreadsheet).setCellKeyToEditorMap(isNull());
        verify(spreadsheet).setComponentIDtoCellKeysMap(isNull());
        verify(spreadsheet).unRegisterCustomComponent(comp1);
        verify(rowSizer).applyHeightsFor(new HashSet<>());
        assertTrue(loader.getCustomComponents().isEmpty());
    }

    @Test
    void load_noFactory_emptyComponents_stillClearsMapsButSkipsUnregister() {
        // Pin: even with no components to unregister, both setters fire
        // with null. rowSizer still gets the empty-set call.
        loader.setFactory(null);

        loader.load();

        verify(spreadsheet).setCellKeyToEditorMap(isNull());
        verify(spreadsheet).setComponentIDtoCellKeysMap(isNull());
        verify(spreadsheet, never()).unRegisterCustomComponent(any());
        verify(rowSizer).applyHeightsFor(new HashSet<>());
    }

    // ----- load(): factory != null, scan dispatch ---------------------------

    @Test
    void load_withFactory_noFreezePane_onlyArea4Scanned() {
        loader.setFactory(factory);
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner).scan(eq(factory), any(), any(), any(), any(), any(),
                eq(1), eq(1), eq(10), eq(10));
        verify(scanner, times(1)).scan(any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void load_withFactory_bothSplits_allFourAreasScanned() {
        loader.setFactory(factory);
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        verify(scanner).scan(eq(factory), any(), any(), any(), any(), any(),
                eq(1), eq(1), eq(3), eq(2)); // area 1
        verify(scanner).scan(eq(factory), any(), any(), any(), any(), any(),
                eq(1), eq(1), eq(3), eq(10)); // area 2
        verify(scanner).scan(eq(factory), any(), any(), any(), any(), any(),
                eq(1), eq(1), eq(10), eq(2)); // area 3
        verify(scanner).scan(eq(factory), any(), any(), any(), any(), any(),
                eq(1), eq(1), eq(10), eq(10)); // area 4
    }

    // ----- load(): reconciliation -------------------------------------------

    @Test
    void load_oldComponentNotInNewScan_andNotInEditorMap_unregistered() {
        // First load: scanner adds comp1 to nextComponents.
        // Second load: scanner returns nothing for comp1 → comp1 reconciled
        // away (unregister + remove from componentIDtoCellKeysMap).
        seedOneComponent();
        // Second load: scanner is a no-op (default mock behavior). Editor
        // map is empty so comp1's node id won't be found there.
        when(spreadsheet.getCellKeyToEditorMap()).thenReturn(new HashMap<>());

        loader.load();

        verify(spreadsheet).unRegisterCustomComponent(comp1);
        assertFalse(loader.getCustomComponents().contains(comp1));
    }

    @Test
    void load_oldEditorStillInEditorMap_isKept_notUnregistered() {
        // Pin: an editor that is still in the editor map is preserved even
        // when the scan doesn't re-add it. Matches the contract that editor
        // lifetime is keyed off the editor-map values (not the scan output).
        seedOneComponent();
        Map<String, Component> editorMap = new HashMap<>();
        editorMap.put("col9 row9", comp1);
        when(spreadsheet.getCellKeyToEditorMap()).thenReturn(editorMap);

        loader.load();

        verify(spreadsheet, never()).unRegisterCustomComponent(comp1);
        assertTrue(loader.getCustomComponents().contains(comp1));
    }

    // ----- load(): editors of cells no longer loaded ------------------------
    // Keeping the editor of every cell ever loaded made the page grow without
    // bound as the sheet scrolled.

    /** Seeds the loader with one registered editor per cell key given. */
    @SuppressWarnings("unchecked")
    private Map<String, Component> seedEditors(String... keys) {
        Map<String, Component> editors = new HashMap<>();
        for (String key : keys) {
            editors.put(key, mock(Component.class));
        }
        loader.setFactory(factory);
        org.mockito.Mockito.doAnswer((Answer<Void>) inv -> {
            HashSet<Component> next = inv.getArgument(2);
            next.addAll(editors.values());
            return null;
        }).when(scanner).scan(any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyInt(), anyInt());
        loader.load();
        org.mockito.Mockito.reset(scanner);
        when(spreadsheet.getCellKeyToEditorMap())
                .thenReturn(new HashMap<>(editors));
        org.mockito.Mockito.clearInvocations(spreadsheet);
        return editors;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Component> editorMapSent() {
        ArgumentCaptor<Map<String, Component>> sent = ArgumentCaptor
                .forClass(Map.class);
        verify(spreadsheet).setCellKeyToEditorMap(sent.capture());
        return sent.getValue();
    }

    @Test
    void load_editorOfACellNoLongerLoaded_isLetGo() {
        Map<String, Component> editors = seedEditors("col9 row9",
                "col20 row20");

        loader.load();

        assertEquals(Set.of("col9 row9"), editorMapSent().keySet());
        verify(spreadsheet)
                .unRegisterCustomComponent(editors.get("col20 row20"));
        verify(spreadsheet, never())
                .unRegisterCustomComponent(editors.get("col9 row9"));
    }

    @Test
    void load_editorOfTheSelectedCell_isKeptWhereverItsCellIs() {
        Map<String, Component> editors = seedEditors("col20 row20");
        CellSelectionManager selection = mock(CellSelectionManager.class);
        when(selection.getSelectedCellReference())
                .thenReturn(new CellReference(19, 19));
        when(spreadsheet.getCellSelectionManager()).thenReturn(selection);

        loader.load();

        assertEquals(Set.of("col20 row20"), editorMapSent().keySet());
        verify(spreadsheet, never())
                .unRegisterCustomComponent(editors.get("col20 row20"));
    }

    @Test
    void load_editorsOfCellsInEveryPaneShown_areKept() {
        // Rows 1-3 and columns A-B frozen; rows and columns 20-30 loaded.
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);
        when(spreadsheet.getFirstRow()).thenReturn(20);
        when(spreadsheet.getLastRow()).thenReturn(30);
        when(spreadsheet.getFirstColumn()).thenReturn(20);
        when(spreadsheet.getLastColumn()).thenReturn(30);
        seedEditors("col1 row2", "col25 row2", "col1 row25", "col25 row25",
                "col10 row10", "col25 row10", "col10 row25");

        loader.load();

        assertEquals(
                Set.of("col1 row2", "col25 row2", "col1 row25", "col25 row25"),
                editorMapSent().keySet());
    }

    @Test
    void load_beforeTheClientHasSaidWhatItShows_letsNoEditorGo() {
        when(spreadsheet.getFirstRow()).thenReturn(-1);
        when(spreadsheet.getLastRow()).thenReturn(-1);
        when(spreadsheet.getFirstColumn()).thenReturn(-1);
        when(spreadsheet.getLastColumn()).thenReturn(-1);
        Map<String, Component> editors = seedEditors("col20 row20");

        loader.load();

        assertEquals(Set.of("col20 row20"), editorMapSent().keySet());
        verify(spreadsheet, never())
                .unRegisterCustomComponent(editors.get("col20 row20"));
    }

    @Test
    void load_appliesRowHeights_onlyWhenRowsHaveComponents() {
        loader.setFactory(factory);
        // Stub scanner to add a row to rowsWithComponents.
        addRowOnScan(5);

        loader.load();

        verify(rowSizer).applyHeightsFor(
                org.mockito.ArgumentMatchers.argThat(rows -> rows.contains(5)));
    }

    @Test
    void load_doesNotApplyRowHeights_whenNoRowsHaveComponents() {
        loader.setFactory(factory);

        loader.load();

        verify(rowSizer, never()).applyHeightsFor(any());
    }

    @Test
    void load_pushesFreshMapsToSpreadsheet() {
        loader.setFactory(factory);

        loader.load();

        verify(spreadsheet).setCellKeyToEditorMap(any(Map.class));
        verify(spreadsheet).setComponentIDtoCellKeysMap(any(HashMap.class));
    }

    // ----- unloadAll --------------------------------------------------------

    @Test
    void unloadAll_clearsEditorMap_andUnregistersComponents() {
        seedOneComponent();

        loader.unloadAll();

        verify(spreadsheet).setCellKeyToEditorMap(isNull());
        verify(spreadsheet).unRegisterCustomComponent(comp1);
        assertTrue(loader.getCustomComponents().isEmpty());
    }

    @Test
    void unloadAll_doesNotTouchComponentIdMap_norRowHeights() {
        // Pin the asymmetry with load(factory == null): unloadAll touches
        // ONLY the editor map; not componentIDtoCellKeysMap and not the
        // row heights. Used when wiring a new factory pre-load.
        seedOneComponent();
        // Reset invocation counters so only the unloadAll call is counted.
        org.mockito.Mockito.clearInvocations(spreadsheet, rowSizer);

        loader.unloadAll();

        verify(spreadsheet, never()).setComponentIDtoCellKeysMap(any());
        verify(rowSizer, never()).applyHeightsFor(any());
    }

    @Test
    void unloadAll_noComponents_stillClearsEditorMap() {
        loader.unloadAll();
        verify(spreadsheet).setCellKeyToEditorMap(isNull());
        verify(spreadsheet, never()).unRegisterCustomComponent(any());
    }

    // ----- helpers ----------------------------------------------------------

    /** Seeds the loader with comp1 in customComponents via a fake scan. */
    @SuppressWarnings("unchecked")
    private void seedOneComponent() {
        loader.setFactory(factory);
        // Make the scanner add comp1 to the nextComponents set on its
        // first call (any area).
        org.mockito.Mockito.doAnswer((Answer<Void>) inv -> {
            HashSet<Component> next = inv.getArgument(2);
            next.add(comp1);
            return null;
        }).when(scanner).scan(any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyInt(), anyInt());
        loader.load();
        // Reset scanner for the next call so subsequent loads start fresh.
        org.mockito.Mockito.reset(scanner);
    }

    /** Configures the scanner to add the given row to rowsWithComponents. */
    private void addRowOnScan(int row) {
        org.mockito.Mockito.doAnswer((Answer<Void>) inv -> {
            Set<Integer> rows = inv.getArgument(3);
            rows.add(row);
            return null;
        }).when(scanner).scan(any(), any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyInt(), anyInt());
    }
}
