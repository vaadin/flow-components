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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Gives the cells shown the components and editors of the
 * {@link SpreadsheetComponentFactory}, and makes their rows tall enough for
 * them. A component kept from one load to the next keeps its {@code StateNode};
 * those no longer shown are taken off. A cell keeps the editor it has while it
 * is loaded, so that what the user typed into it survives the cell being drawn
 * again.
 */
final class CustomComponentLoader implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CustomComponentLoader.class);

    private final Spreadsheet spreadsheet;
    private Set<Component> customComponents = new HashSet<>();
    /** The rows grown for components on the last load, 0-based. */
    private Set<Integer> grownRows;

    CustomComponentLoader(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
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
        if (spreadsheet.getSpreadsheetComponentFactory() == null) {
            // No factory -> clear all
            spreadsheet.setCellKeyToEditorMap(null);
            spreadsheet.sheetModel().componentIDtoCellKeysMap.set(null);
            if (!customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    spreadsheet.unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
            applyHeightsFor(new HashSet<>());
            return;
        }

        Map<String, Component> nextCellKeyToEditor = new HashMap<>(
                spreadsheet.getCellKeyToEditorMap());
        HashMap<String, String> nextComponentIDtoCellKeysMap = new HashMap<>();
        HashSet<Component> nextComponents = new HashSet<>();
        Set<Integer> rowsWithComponents = new HashSet<>();

        spreadsheet.viewport()
                .forEachPane((r1, c1, r2, c2) -> scan(nextComponents,
                        rowsWithComponents, nextCellKeyToEditor,
                        nextComponentIDtoCellKeysMap, r1, c1, r2, c2));
        dropEditorsOfCellsNotLoaded(nextCellKeyToEditor);

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
        spreadsheet.sheetModel().componentIDtoCellKeysMap
                .set(nextComponentIDtoCellKeysMap);

        if (!rowsWithComponents.isEmpty()) {
            applyHeightsFor(rowsWithComponents);
        }
    }

    /**
     * Asks the factory for the components and editors of the cells of a range,
     * 1-based, leaving out the cells a merged cell covers but its first.
     */
    private void scan(HashSet<Component> newCustomComponents,
            Set<Integer> rowsWithComponents,
            Map<String, Component> cellKeyToEditor,
            HashMap<String, String> componentIDtoCellKeysMap, int row1,
            int col1, int row2, int col2) {
        SpreadsheetComponentFactory factory = spreadsheet
                .getSpreadsheetComponentFactory();
        for (int r = row1; r <= row2; r++) {
            final Row row = spreadsheet.getActiveSheet().getRow(r);
            for (int c = col1; c <= col2; c++) {
                MergedRegion region = spreadsheet.getMergedRegionContainer()
                        .getMergedRegion(c + 1, r + 1);
                if (region != null
                        && (region.col1 != (c + 1) || region.row1 != (r + 1))) {
                    continue;
                }
                Cell cell = row != null ? row.getCell(c) : null;
                try {
                    Component customComponent = factory
                            .getCustomComponentForCell(cell, r, c, spreadsheet,
                                    spreadsheet.getActiveSheet());
                    if (customComponent != null) {
                        final String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                        if (!customComponents.contains(customComponent)) {
                            spreadsheet
                                    .registerCustomComponent(customComponent);
                        }
                        componentIDtoCellKeysMap.put(
                                Spreadsheet.getComponentNodeId(customComponent),
                                key);
                        newCustomComponents.add(customComponent);
                        rowsWithComponents.add(r);
                    } else if (!spreadsheet
                            .isCellLocked(new CellAddress(r, c))) {
                        final String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                        // A cell that already has an editor keeps it, so
                        // whatever the user has typed into it survives the
                        // re-render. Only ask the factory for a cell that has
                        // none — a factory is free to hand back a new instance
                        // every call, and several do.
                        Component customEditor = cellKeyToEditor.get(key);
                        if (customEditor == null) {
                            customEditor = factory.getCustomEditorForCell(cell,
                                    r, c, spreadsheet,
                                    spreadsheet.getActiveSheet());
                        }
                        if (customEditor != null) {
                            if (!newCustomComponents.contains(customEditor)
                                    && !customComponents
                                            .contains(customEditor)) {
                                spreadsheet
                                        .registerCustomComponent(customEditor);
                            }
                            cellKeyToEditor.put(key, customEditor);
                            newCustomComponents.add(customEditor);
                            rowsWithComponents.add(r);
                        }
                    } else {
                        // Locked now, as when the sheet is protected: the
                        // editor it had goes.
                        cellKeyToEditor
                                .remove(SpreadsheetUtil.toKey(c + 1, r + 1));
                    }
                } catch (Exception e) {
                    LOGGER.warn(
                            "Error loading custom component/editor for cell ({}, {})",
                            c + 1, r + 1, e);
                }
            }
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
    private void dropEditorsOfCellsNotLoaded(Map<String, Component> editors) {
        Viewport viewport = spreadsheet.viewport();
        String selected = selectedCellKey();
        editors.keySet()
                .removeIf(key -> !key.equals(selected) && !viewport.shows(
                        SpreadsheetUtil.getRowFromKey(key) - 1,
                        SpreadsheetUtil.getColumnIndexFromKey(key) - 1));
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
     * Makes the rows with components tall enough for them, and gives the rows
     * that had components on the last load their own heights back. Rows are
     * 0-based.
     */
    private void applyHeightsFor(Set<Integer> rowsWithComponents) {
        // Components can be on rows setMaxRows added past the loaded ones.
        int lastRow = Math.max(lastOf(rowsWithComponents), lastOf(grownRows));
        float[] updatedHeights = spreadsheet.sizes().heightsCovering(lastRow);
        int minHeight = spreadsheet.getMinimumRowHeightForComponents();

        // Grow rows that now host a component
        for (Integer row : rowsWithComponents) {
            if (spreadsheet.isRowHidden(row)) {
                continue;
            }
            if (updatedHeights[row] < minHeight) {
                updatedHeights[row] = minHeight;
            }
        }
        // Restore rows that previously hosted a component but no longer do
        if (grownRows != null) {
            Sheet activeSheet = spreadsheet.getActiveSheet();
            for (Integer row : grownRows) {
                if (rowsWithComponents.contains(row)) {
                    continue;
                }
                if (spreadsheet.isRowHidden(row)) {
                    updatedHeights[row] = 0;
                } else {
                    Row r = activeSheet.getRow(row);
                    updatedHeights[row] = r == null
                            ? activeSheet.getDefaultRowHeightInPoints()
                            : r.getHeightInPoints();
                }
            }
        }
        spreadsheet.setRowH(updatedHeights);
        grownRows = rowsWithComponents;
    }

    /**
     * Makes the rows with components tall enough for them again, after their
     * heights were read anew from the sheet.
     */
    void regrowRows() {
        if (grownRows != null) {
            applyHeightsFor(grownRows);
        }
    }

    private static int lastOf(Set<Integer> rows) {
        return rows == null || rows.isEmpty() ? -1 : Collections.max(rows);
    }

    /**
     * Used by {@code Spreadsheet.setSpreadsheetComponentFactory} when the
     * spreadsheet has not been initialized yet (first row not set), and when
     * another sheet is shown: detaches the components registered so far. The
     * editor map has been dropped already then.
     */
    void unloadAll() {
        if (!customComponents.isEmpty()) {
            for (Component c : customComponents) {
                spreadsheet.unRegisterCustomComponent(c);
            }
            customComponents.clear();
        }
        // The rows are another sheet's, or not loaded yet: their heights are
        // read from the sheet, with no room for components in them.
        grownRows = null;
    }
}
