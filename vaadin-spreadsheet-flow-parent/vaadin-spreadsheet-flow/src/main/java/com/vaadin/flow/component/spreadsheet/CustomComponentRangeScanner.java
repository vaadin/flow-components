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
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Scans a single cell range and asks the configured
 * {@link SpreadsheetComponentFactory} for any custom components or editors to
 * display. Skips cells inside merged regions (except the top-left anchor).
 * Populates the four collections it receives — the new component set, the rows
 * that contain components, the editor of each cell and the cell of each
 * component — and registers any newly-discovered component as a virtual child
 * of the spreadsheet element.
 *
 * <p>
 * One range per call; the orchestration of viewport panes lives in
 * {@link CustomComponentLoader}.
 */
final class CustomComponentRangeScanner implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CustomComponentRangeScanner.class);

    private final Spreadsheet spreadsheet;

    CustomComponentRangeScanner(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void scan(SpreadsheetComponentFactory factory,
            Set<Component> existingComponents,
            HashSet<Component> newCustomComponents,
            Set<Integer> rowsWithComponents,
            Map<String, Component> cellKeyToEditor,
            HashMap<String, String> componentIDtoCellKeysMap, int row1,
            int col1, int row2, int col2) {
        for (int r = row1 - 1; r < row2; r++) {
            final Row row = spreadsheet.getActiveSheet().getRow(r);
            for (int c = col1 - 1; c < col2; c++) {
                MergedRegion region = spreadsheet.getMergedRegionContainer()
                        .getMergedRegion(c + 1, r + 1);
                if (region == null
                        || (region.col1 == (c + 1) && region.row1 == (r + 1))) {
                    Cell cell = row != null ? row.getCell(c) : null;
                    try {
                        Component customComponent = factory
                                .getCustomComponentForCell(cell, r, c,
                                        spreadsheet,
                                        spreadsheet.getActiveSheet());
                        if (customComponent != null) {
                            final String key = SpreadsheetUtil.toKey(c + 1,
                                    r + 1);
                            if (!existingComponents.contains(customComponent)) {
                                spreadsheet.registerCustomComponent(
                                        customComponent);
                            }
                            componentIDtoCellKeysMap.put(Spreadsheet
                                    .getComponentNodeId(customComponent), key);
                            newCustomComponents.add(customComponent);
                            rowsWithComponents.add(r);
                        } else if (!spreadsheet
                                .isCellLocked(new CellAddress(r, c))) {
                            final String key = SpreadsheetUtil.toKey(c + 1,
                                    r + 1);
                            // A cell that already has an editor keeps it, so
                            // whatever the user has typed into it survives the
                            // re-render. Only ask the factory for a cell that
                            // has none — a factory is free to hand back a new
                            // instance every call, and several do.
                            Component customEditor = cellKeyToEditor.get(key);
                            if (customEditor == null) {
                                customEditor = factory.getCustomEditorForCell(
                                        cell, r, c, spreadsheet,
                                        spreadsheet.getActiveSheet());
                            }
                            if (customEditor != null) {
                                if (!newCustomComponents.contains(customEditor)
                                        && !existingComponents
                                                .contains(customEditor)) {
                                    spreadsheet.registerCustomComponent(
                                            customEditor);
                                }
                                cellKeyToEditor.put(key, customEditor);
                                newCustomComponents.add(customEditor);
                                rowsWithComponents.add(r);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn(
                                "Error loading custom component/editor for cell ({}, {})",
                                c + 1, r + 1, e);
                    }
                }
                if (region != null) {
                    c = region.col2 - 1;
                }
            }
        }
    }
}
