/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.Component;

/**
 * Orchestrates the load of custom components and custom editors for the
 * currently visible cells. Walks the four viewport panes formed by the freeze
 * split, asks {@link CustomComponentRangeScanner} for each range, then
 * reconciles the new component set against the previous registration so removed
 * components are unregistered while retained components keep their
 * {@code StateNode} identity.
 *
 * <p>
 * Owns the {@code customComponents} set and the active
 * {@link SpreadsheetComponentFactory}.
 */
final class CustomComponentLoader implements Serializable {

    private final Spreadsheet spreadsheet;
    private final CustomComponentRangeScanner scanner;
    private final CustomComponentRowSizer rowSizer;
    private Set<Component> customComponents = new HashSet<>();
    private SpreadsheetComponentFactory factory;

    CustomComponentLoader(Spreadsheet spreadsheet,
            CustomComponentRangeScanner scanner,
            CustomComponentRowSizer rowSizer) {
        this.spreadsheet = spreadsheet;
        this.scanner = scanner;
        this.rowSizer = rowSizer;
    }

    SpreadsheetComponentFactory getFactory() {
        return factory;
    }

    void setFactory(SpreadsheetComponentFactory factory) {
        this.factory = factory;
    }

    Set<Component> getCustomComponents() {
        return customComponents;
    }

    void load() {
        load(false);
    }

    /**
     * @param recreateEditors
     *            {@code true} to ask the factory for fresh editors — the
     *            application forced a refresh, swapped the factory, or moved
     *            the cells an editor is cached against; {@code false} to hand
     *            each cell the editor it already has, so what the user typed
     *            into it survives the re-render.
     */
    void load(boolean recreateEditors) {
        if (recreateEditors) {
            // Drop the cached editors (and the callback tracker) so the scan
            // below asks the factory for new ones and the callback fires again
            // for the selected cell.
            spreadsheet.setCellKeyToEditorMap(null);
        }
        if (factory == null) {
            // No factory -> clear all
            spreadsheet.setCellKeyToEditorMap(null);
            spreadsheet.setComponentIDtoCellKeysMap(null);
            if (!customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    spreadsheet.unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
            rowSizer.applyHeightsFor(new HashSet<>());
            return;
        }

        Map<String, Component> nextCellKeyToEditor = new HashMap<>(
                spreadsheet.getCellKeyToEditorMap());
        HashMap<String, String> nextComponentIDtoCellKeysMap = new HashMap<>();
        HashSet<Component> nextComponents = new HashSet<>();
        Set<Integer> rowsWithComponents = new HashSet<>();

        int verticalSplit = spreadsheet.getLastFrozenRow();
        int horizontalSplit = spreadsheet.getLastFrozenColumn();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();

        if (verticalSplit > 0 && horizontalSplit > 0) {
            scanner.scan(factory, customComponents, nextComponents,
                    rowsWithComponents, nextCellKeyToEditor,
                    nextComponentIDtoCellKeysMap, 1, 1, verticalSplit,
                    horizontalSplit);
        }
        if (verticalSplit > 0) {
            scanner.scan(factory, customComponents, nextComponents,
                    rowsWithComponents, nextCellKeyToEditor,
                    nextComponentIDtoCellKeysMap, 1, firstColumn, verticalSplit,
                    lastColumn);
        }
        if (horizontalSplit > 0) {
            scanner.scan(factory, customComponents, nextComponents,
                    rowsWithComponents, nextCellKeyToEditor,
                    nextComponentIDtoCellKeysMap, firstRow, 1, lastRow,
                    horizontalSplit);
        }
        scanner.scan(factory, customComponents, nextComponents,
                rowsWithComponents, nextCellKeyToEditor,
                nextComponentIDtoCellKeysMap, firstRow, firstColumn, lastRow,
                lastColumn);
        dropEditorsOfCellsNotLoaded(nextCellKeyToEditor, verticalSplit,
                horizontalSplit, firstRow, firstColumn, lastRow, lastColumn);

        // Keep registered custom editors that still claim a cell key, even
        // if they're not in the new scan output; unregister the rest.
        for (Iterator<Component> i = customComponents.iterator(); i
                .hasNext();) {
            Component c = i.next();
            if (nextComponents.contains(c)) {
                continue;
            }
            if (nextCellKeyToEditor.containsValue(c)) {
                nextComponents.add(c);
            } else {
                spreadsheet.unRegisterCustomComponent(c);
                nextComponentIDtoCellKeysMap
                        .remove(Spreadsheet.getComponentNodeId(c));
                i.remove();
            }
        }
        customComponents = nextComponents;
        spreadsheet.setCellKeyToEditorMap(nextCellKeyToEditor);
        spreadsheet.setComponentIDtoCellKeysMap(nextComponentIDtoCellKeysMap);

        if (!rowsWithComponents.isEmpty()) {
            rowSizer.applyHeightsFor(rowsWithComponents);
        }
    }

    /**
     * Forgets the editors of cells no longer loaded, so that they are let go
     * of: an editor is kept for its cell so that what the user typed into it
     * survives the cell being drawn again, and keeping the editor of every cell
     * ever loaded made the page grow without bound as the sheet scrolled - 161
     * MB after 600 rows of a sheet with an editor in every cell. The selected
     * cell keeps its editor wherever it is: what the user is typing into it is
     * in no cell yet.
     */
    private void dropEditorsOfCellsNotLoaded(Map<String, Component> editors,
            int verticalSplit, int horizontalSplit, int firstRow,
            int firstColumn, int lastRow, int lastColumn) {
        if (firstRow < 1 || firstColumn < 1 || lastRow < firstRow
                || lastColumn < firstColumn) {
            // Where the client is has not been reported yet.
            return;
        }
        String selected = selectedCellKey();
        editors.keySet().removeIf(key -> {
            if (key.equals(selected)) {
                return false;
            }
            int col = SpreadsheetUtil.getColumnIndexFromKey(key);
            int row = SpreadsheetUtil.getRowFromKey(key);
            boolean inLoadedRows = row >= firstRow && row <= lastRow;
            boolean inFrozenRows = row <= verticalSplit;
            boolean inLoadedColumns = col >= firstColumn && col <= lastColumn;
            boolean inFrozenColumns = col <= horizontalSplit;
            return !((inLoadedRows || inFrozenRows)
                    && (inLoadedColumns || inFrozenColumns));
        });
    }

    /** The selected cell's key, or null when no cell is selected. */
    private String selectedCellKey() {
        CellSelectionManager selection = spreadsheet.getCellSelectionManager();
        CellReference selected = selection == null ? null
                : selection.getSelectedCellReference();
        return selected == null ? null
                : SpreadsheetUtil.toKey(selected.getCol() + 1,
                        selected.getRow() + 1);
    }

    /**
     * Used by {@code Spreadsheet.setSpreadsheetComponentFactory} when the
     * spreadsheet has not been initialized yet (first row not set): drop the
     * editor map and detach any components that may have been registered by a
     * previous factory.
     */
    void unloadAll() {
        spreadsheet.setCellKeyToEditorMap(null);
        if (!customComponents.isEmpty()) {
            for (Component c : customComponents) {
                spreadsheet.unRegisterCustomComponent(c);
            }
            customComponents.clear();
        }
    }
}
